package androidtv.livetv.stb.ui.videoplay;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidtv.livetv.stb.ApplicationMain;
import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponseWrapper;
import androidtv.livetv.stb.entity.DvrLinkResponse;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginDataDelete;
import androidtv.livetv.stb.entity.NewDvrEntity;
import androidtv.livetv.stb.entity.PlayBackErrorEntity;
import androidtv.livetv.stb.ui.custom_views.CustomTextView;
import androidtv.livetv.stb.ui.splash.SplashActivity;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.fragments.dvr.DvrFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.error.ErrorFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.FragmentMenu;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.GridMenuFragment;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DataUtils;
import androidtv.livetv.stb.utils.DateUtils;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import androidtv.livetv.stb.utils.MaxTvUnhandledException;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;
import static androidtv.livetv.stb.utils.LinkConfig.PLAYED_CATEGORY_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.SELECTED_CATEGORY_NAME;
import static java.lang.Thread.sleep;

public class VideoPlayActivity extends AppCompatActivity implements FragmentMenu.FragmentMenuInteraction, EpgFragment.FragmentEpgInteraction,
        DvrFragment.FragmentDvrInteraction, PlayerControlView.VisibilityListener, GridMenuFragment.OnFragmentInteractionListener, AspectRatioFrameLayout.AspectRatioListener {

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    @BindView(R.id.txt_channel_name)
    TextView currentPlayingDvrChannel;
    @BindView(R.id.img_play_pause)
    ImageView playPauseStatus;
    @BindView(R.id.videoStatus)
    ImageView recordedStatus;
    @BindView(R.id.progressBar1)
    AVLoadingIndicatorView progressBar;
    @BindView(R.id.tv_setchannelfrompriority)
    TextView txtChangeChannelFromPriority;

    @BindView(R.id.exo_pause)
    ImageButton controllerPause;

    @BindView(R.id.exo_progress)
    DefaultTimeBar controllerTimebar;

    @BindView(R.id.exo_play)
    ImageButton controllerPlay;

    @BindView(R.id.exo_ffwd)
    ImageButton controllerFwd;

    @BindView(R.id.exo_rew)
    ImageButton controllerRew;

    @BindView(R.id.tvboxID)
    TextView txtRandomDisplayBoxId;
    @BindView(R.id.transparentLoadingBackground)
    LinearLayout backgroundTransparent;
    @BindView(R.id.container_movie_player)
    FrameLayout errorFrame;
    @BindView(R.id.videoSurfaceContainer)
    FrameLayout videoSurfaceContainer;
    @BindView(R.id.video_view)
    PlayerView videoSurfaceView;
    @BindView(R.id.menu_bg)
    ImageView menuBackground;
    @BindView(R.id.priority_view)
    CustomTextView priorityView;

    private VideoPlayViewModel videoPlayViewModel;
    private GridMenuFragment gridMenuFragment = new GridMenuFragment();
    private FragmentMenu menuFragment = new FragmentMenu();
    private DvrFragment dvrFragment = new DvrFragment();
    private Fragment currentFragment;
    private SimpleExoPlayer player;
    private Handler hideMenuHandler;
    private SharedPreferences lastPlayedPrefs;
    private SharedPreferences.Editor editor;
    String macAddress;
    boolean toExit = false;
    private ChannelItem currentDvrChannelItem;
    private String nextVideoNameDvr;
    private boolean isDvrPlaying;
    private ChannelItem currentPlayingChannel;
    private Handler handlerToHidePriority, chFrmPriorityHandler;
    private List<ChannelItem> allChannelList;
    private int selectedDvrPosition = 0;
    private int selectedDvrDate = 0;
    private Epgs currentPlayedEpg;

    private DataSource.Factory dataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;

    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;
    private boolean inErrorState;
    PlayerEventListener playerEventListener;
    private int lastPlayedId;
    Handler toExitHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_video_play);
        try {
            InetAddress group1 = InetAddress.getByName("239.0.0.0");
            InetAddress group2 = InetAddress.getByName("224.0.0.0");
            MulticastSocket multicastSocket = new MulticastSocket();
            multicastSocket.joinGroup(group1);
            multicastSocket.joinGroup(group2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataSourceFactory = buildDataSourceFactory(true);
        trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        clearStartPosition();
        chFrmPriorityHandler = new Handler();
        lastPlayedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);
        hideMenuHandler = new Handler();
        macAddress = AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this);
        txtRandomDisplayBoxId.setText(macAddress);

        controllerTimebar.setOnFocusChangeListener((view, b) -> {
            if (b) {
                controllerTimebar.setScrubberColor(Color.RED);
            } else {
                controllerTimebar.setScrubberColor(Color.WHITE);
            }
        });

    }


    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    @OnClick(R.id.tv_setchannelfrompriority)
    public void onChEditTxtClicked() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(txtChangeChannelFromPriority.getWindowToken(), 0);
        }
        stopHandlerChangeChannelFromNumbers();
        searchChannelWithgivenPriorityNumber(txtChangeChannelFromPriority.getText().toString());
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandWidthMeter) {
        return ((ApplicationMain) getApplication()).buildDataSourceFactory(useBandWidthMeter ? BANDWIDTH_METER : null);
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        Log.d("activity_state", "onStart");
        super.onStart();
        videoPlayViewModel = ViewModelProviders.of(this).get(VideoPlayViewModel.class);
        playLastPlayedChannel();
        getAllChannelListfromDB();
        if (gridMenuFragment == null) {
            gridMenuFragment = new GridMenuFragment();
        }
    }

    private void playLastPlayedChannel() {
        lastPlayedId = lastPlayedPrefs.getInt(CHANNEL_ID, -1);
        if (lastPlayedId != -1) {
            LiveData<ChannelItem> lastPlayedChData = videoPlayViewModel.getLastPlayedChannel(lastPlayedId);
            lastPlayedChData.observe(this, new android.arch.lifecycle.Observer<ChannelItem>() {
                @Override
                public void onChanged(@Nullable ChannelItem channelItem) {
                    if (channelItem != null) {
                        playChannel(channelItem);
                        lastPlayedChData.removeObserver(this);
                    }
                }
            });
        } else {
            LiveData<ChannelItem> firstPlayedData = videoPlayViewModel.getFirstChannel();
            firstPlayedData.observe(this, new android.arch.lifecycle.Observer<ChannelItem>() {
                @Override
                public void onChanged(@Nullable ChannelItem channelItem) {
                    if (channelItem != null) {
                        playChannel(channelItem);
                        firstPlayedData.removeObserver(this);
                    }
                }
            });
//            openErrorFragment();
        }
    }

    private void openFragment(Fragment fragment) {
        Log.d("frag", "called from activity");
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.container_movie_player, currentFragment).commit();
    }

    public void openListMenu() {
        if (menuFragment != null)
            currentFragment = menuFragment;
        else
            currentFragment = new FragmentMenu();

        showChannelMenu(currentFragment);
    }

    private void showChannelMenu(Fragment currentFragment) {
        if (currentFragment instanceof FragmentMenu && !currentFragment.isAdded())
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).add(R.id.container_movie_player, currentFragment).commit();
        else
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(currentFragment).commit();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        currentFragment = menuFrag;
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (currentFragment == null) {
                    showGridMenu();
                } else if (currentFragment instanceof EpgFragment) {
                    if(currentFragment.isAdded()&& currentFragment.isVisible())
                        hideMenuUI();
                    else {
                        removeOptionalFragments();
                        showGridMenu();
                    }

                } else {
                    try {
                        if (currentFragment instanceof DvrFragment) {
                            if (!isDvrPlaying) {
                                removeOptionalFragments();
                                showGridMenu();
                            } else {
                                if (currentFragment.isHidden())
                                    showDvrMenu();
                                else
                                    hideMenuUI();
                            }
                        } else if (currentFragment instanceof FragmentMenu || currentFragment instanceof GridMenuFragment) {

                            if (videoSurfaceView.getUseController() && currentFragment.isHidden()) {
                                dvrFragment.setCurrentPlayedEpg(currentPlayedEpg);
                                dvrFragment.setGetSelectedDatePosition(selectedDvrDate);
                                dvrFragment.setSelectedDvrPosition(selectedDvrPosition);
                                load(dvrFragment, "dvr");
                            } else if (player != null && (player.getPlayWhenReady())) {
                                if (currentFragment != null && currentFragment.isVisible())
                                    hideMenuUI();
                                else {
                                    showGridMenu();
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }


                }
                break;


            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (!videoSurfaceView.getUseController()) {
                    hideMenuUI();
                    changeChannel(false);
                    return true;
                } else {
                    return false;
                }
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (dvrFragment != null && dvrFragment.isHidden() && isDvrPlaying && !videoSurfaceView.isControllerVisible()) {
                    player.seekTo(player.getCurrentPosition() - 15000);
                    return true;
                } else
                    return false;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (dvrFragment != null && dvrFragment.isHidden() && isDvrPlaying && !videoSurfaceView.isControllerVisible()) {
                    player.seekTo(player.getCurrentPosition() + 15000);
                    return true;
                } else
                    return false;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (!videoSurfaceView.getUseController()) {
                    hideMenuUI();
                    changeChannel(true);
                    return true;
                } else {
                    return false;
                }

            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (isDvrPlaying) {
                    if (player.getPlayWhenReady())
                        player.setPlayWhenReady(false);
                    else
                        player.setPlayWhenReady(true);
                }
                return true;

            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                hideMenuUI();
                changeChannel(false);
                return true;


            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (menuFrag == null || menuFrag.isHidden()) {
                    if (!videoSurfaceView.getUseController()) {
                        changeChannel(false);
                        return true;
                    } else {
                        if (controllerTimebar.hasFocus()) {
                            controllerPause.requestFocus();
                            return true;
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case 63:
                if (isDvrPlaying) {
                    videoSurfaceView.showController();
                    return true;
                } else if ((menuFrag == null || menuFrag.isHidden()) && txtChangeChannelFromPriority.getVisibility() == View.GONE) {
                    removeOptionalFragments();
                    openListMenu();
                    return true;
                } else if (txtChangeChannelFromPriority.getVisibility() == View.VISIBLE) {
                    stopHandlerChangeChannelFromNumbers();
                    searchChannelWithgivenPriorityNumber(txtChangeChannelFromPriority.getText().toString());
                    return true;
                } else
                    return false;

            case KeyEvent.KEYCODE_CHANNEL_UP:
                hideMenuUI();
                changeChannel(true);
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (menuFrag == null || menuFrag.isHidden()) {
                    if (!videoSurfaceView.getUseController()) {
                        changeChannel(true);
                        return true;
                    } else {
                        if (controllerPause.hasFocus() || controllerPlay.hasFocus()
                                || controllerRew.hasFocus() || controllerFwd.hasFocus()) {
                            controllerTimebar.requestFocus();
                            return true;
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                return numberKeyCodeEvent(keyCode);
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean numberKeyCodeEvent(int keyCode) {
        stopHandlerChangeChannelFromNumbers();
        if (menuFragment != null && menuFragment.isAdded()) {
            menuFragment.hideErrorFrag();
        }
        if (gridMenuFragment != null) {
            gridMenuFragment.hideErrorFrag();
        }
        hideMenuUI();
        switch (keyCode) {
            case 7:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority.getText() + "0");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }

                return true;

            case 8:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority.getText() + "1");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 9:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority
                            .getText() + "2");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 10:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority.getText() + "3");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 11:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority
                            .getText() + "4");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 12:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority
                            .getText() + "5");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 13:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority
                            .getText() + "6");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 14:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority
                            .getText() + "7");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 15:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority
                            .getText() + "8");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case 16:
                txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
                txtChangeChannelFromPriority.bringToFront();
                if (txtChangeChannelFromPriority.getText().length() < 4) {
                    txtChangeChannelFromPriority.setText(txtChangeChannelFromPriority
                            .getText() + "9");
                    startHandlerChangeChannelFromNumbers();
                    // condnsetchannelfrompriority();
                } else {
                    txtChangeChannelFromPriority.setText("");
                    txtChangeChannelFromPriority.setVisibility(View.GONE);
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                return false;
        }

    }

    private Runnable runnableChangeChannelFromNumbers = new Runnable() {
        @Override
        public void run() {
            searchChannelWithgivenPriorityNumber(txtChangeChannelFromPriority.getText().toString());
        }
    };

    private void stopHandlerChangeChannelFromNumbers() {
        chFrmPriorityHandler.removeCallbacks(runnableChangeChannelFromNumbers);
    }

    private void startHandlerChangeChannelFromNumbers() {
        chFrmPriorityHandler.postDelayed(runnableChangeChannelFromNumbers, 2000);
    }

    private void getAllChannelListfromDB() {
        LiveData<List<ChannelItem>> listLiveData = videoPlayViewModel.getAllChannels();
        listLiveData.observe(this, new Observer<List<ChannelItem>>() {
            @Override
            public void onChanged(@Nullable List<ChannelItem> channelItemList) {
                if (channelItemList != null) {
                    allChannelList = channelItemList;
                    listLiveData.removeObserver(this);
                }
            }
        });
    }

    private void searchChannelWithgivenPriorityNumber(String priorityNumber) {
        checkIfChannelExistsAndPlay(allChannelList, priorityNumber);
        txtChangeChannelFromPriority.setText("");
        txtChangeChannelFromPriority.setVisibility(View.GONE);
    }

    private void checkIfChannelExistsAndPlay(List<ChannelItem> channelItemList, String priorityNumber) {
        boolean channelExists = false;
        ChannelItem foundChannel = null;
        for (ChannelItem searchChannel : channelItemList) {
            if (String.valueOf(searchChannel.getChannelPriority()).equals(priorityNumber)) {
                foundChannel = searchChannel;
                channelExists = true;
                break;
            }
        }
        if (channelExists) {
            editor = lastPlayedPrefs.edit();
            editor.putString(PLAYED_CATEGORY_NAME, getString(R.string.all_channels));
            editor.apply();
            playChannel(channelItemList.get(channelItemList.indexOf(foundChannel)));
        } else {
            Toast.makeText(VideoPlayActivity.this, getString(R.string.no_channel_found), Toast.LENGTH_SHORT).show();
            hideProgressBar();
            showGridMenu();
        }
    }


    private void changeChannel(boolean isNext) {
        LiveData<List<ChannelItem>> channelsInCat = videoPlayViewModel.getChannelsOfCategory(lastPlayedPrefs.getString(SELECTED_CATEGORY_NAME, getString(R.string.all_channels)));
        channelsInCat.observe(this, new Observer<List<ChannelItem>>() {
            @Override
            public void onChanged(@Nullable List<ChannelItem> channelItemList) {
                if (channelItemList != null) {
                    channelsInCat.removeObserver(this);
                    int currentChannelPosition = channelItemList.indexOf(currentPlayingChannel);
                    //put condition here so no IoB Exception occurs
                    if (isNext) {
                        if (currentChannelPosition < channelItemList.size() - 1)
                            currentChannelPosition++;
                        else
                            currentChannelPosition = 0;

                    } else {
                        if (currentChannelPosition > 0)
                            currentChannelPosition--;
                        else
                            currentChannelPosition = channelItemList.size() - 1;

                    }
                    playChannel(channelItemList.get(currentChannelPosition));
                }
            }
        });


    }


    private void openFragmentWithBackStack(Fragment fragment, String tag) {
        hideProgressBar();
        hideMenuUI();
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).add(R.id.container_movie_player, fragment).addToBackStack(tag).commit();

    }

    private void releasePlayer() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof EpgFragment) {
            removeOptionalFragments();
            openListMenu();
        } else {
            if (currentFragment instanceof DvrFragment) {
                if (!isDvrPlaying) {
                    removeOptionalFragments();
                    openListMenu();
                } else {
                    if (currentFragment.isHidden()) showDvrMenu();
                    else {
                        removeOptionalFragments();
                        openListMenu();
                    }
                }
            } else if ((currentFragment instanceof FragmentMenu || currentFragment instanceof GridMenuFragment) && currentFragment.isVisible()) {
                try {
                    releasePlayer();
                } catch (Exception ignored) {
                }
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).hide(currentFragment).commit();
            } else {

                if (toExit) {
                    toExitHandler.removeCallbacks(null);
                    finish();
                    super.onBackPressed();
                    return;
                }
                toExit = true;
                Toast.makeText(this, getResources().getString(R.string.press_back_to_exit), Toast.LENGTH_SHORT).show();
                toExitHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toExit = false;
                    }
                }, 2000);

            }
        }

    }


    private void removeOptionalFragments() {
        currentFragment=  getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if(currentFragment!=null) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).remove(currentFragment).commit();
            currentFragment=null;
        }
    }

    private void showGridMenu() {
        if (gridMenuFragment == null) {
            gridMenuFragment = new GridMenuFragment();
        }
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag == null)
//            openFragment(menuFragment);
            openFragment(gridMenuFragment);
        else {
            if (menuFrag instanceof GridMenuFragment && menuFrag.isHidden())
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(menuFrag).commit();
            else {
                removeOptionalFragments();
                currentFragment = gridMenuFragment;
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(currentFragment).commit();
            }

        }
    }

   /* private void showMenuFrag(ErrorFragment errorFragment) {
        Fragment menuFrag = (FragmentMenu) getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag == null)
            openFragment(menuFragment);
        else {

            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(menuFrag).commit();
        }
    }

    private void showMenuFragWithError(ErrorFragment errorFragment) {
        menuFragment.setErrorFragMent(errorFragment);
        openFragment(menuFragment);

    }*/

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopCloseMenuHandler();
        try {
            if (player.getCurrentPosition() > 0 && player.getPlayWhenReady())
                startCloseMenuHandler();
        } catch (Exception ignored) {
        }
    }

    public void showPriorityNo() {
        priorityView.setText(String.valueOf(currentPlayingChannel.getChannelPriority()));
        priorityView.setVisibility(View.VISIBLE);
        priorityView.bringToFront();
        handlerToHidePriority = new Handler();
        Runnable hidePriority = new Runnable() {
            @Override
            public void run() {
                priorityView.setVisibility(View.INVISIBLE);
            }
        };
        handlerToHidePriority.postDelayed(hidePriority, 3 * 1000);

    }

    Thread threadToDisplayBoxId = new Thread(new Runnable() {
        @Override
        public void run() {
            Random random = new Random();
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) txtRandomDisplayBoxId.getLayoutParams();
            while (true) {
                if (txtRandomDisplayBoxId.getVisibility() == View.VISIBLE) {
                    runOnUiThread(() -> txtRandomDisplayBoxId.setVisibility(GONE));
                    try {
                        sleep(2000 + (new Random().nextInt(30 * 60 * 1000)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    VideoPlayActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;
                    params.setMargins(random.nextInt(width - txtRandomDisplayBoxId.getWidth() - 200), random.nextInt(height - txtRandomDisplayBoxId.getHeight() - 200),
                            random.nextInt(100), random.nextInt(100));

                    runOnUiThread(() -> {
                        txtRandomDisplayBoxId.setText((AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(VideoPlayActivity.this)));
                        txtRandomDisplayBoxId.bringToFront();
                        txtRandomDisplayBoxId.setLayoutParams(params);
                        txtRandomDisplayBoxId.setVisibility(View.VISIBLE);
                    });
                    try {
                        sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });


    void hideMacDisplayHandler() {
        threadToDisplayBoxId.interrupt();
    }


    @Override
    public void playChannel(ChannelItem item) {
        saveCurrentInPrefs(item);
        selectedDvrDate = 0;
        selectedDvrPosition = 0;
        currentPlayedEpg = null;
        Timber.d("Played Channel:" + item.getName());
        //TODO play Channels
        try {
            if (currentPlayingChannel != null && currentPlayingChannel.getId() == item.getId() && player != null && player.getPlayWhenReady() && player.getCurrentPosition() > 0 && !isDvrPlaying) {
                hideMenuUI();
            } else {
                showProgressBar();
                long utc = GetUtc.getInstance().getTimestamp().getUtc();
                Login login = GlobalVariables.login;
                currentPlayingChannel = item;
                LiveData<ChannelLinkResponseWrapper> videoLinkData = videoPlayViewModel.getChannelLink(login.getToken(), utc, login.getId(),
                        LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc),
                                login.getSession()), macAddress, item.getId());
                videoLinkData.observe(VideoPlayActivity.this, new Observer<ChannelLinkResponseWrapper>() {
                    @Override
                    public void onChanged(@Nullable ChannelLinkResponseWrapper channelLinkResponse) {
                        if (channelLinkResponse != null) {
                            if (channelLinkResponse.getChannelLinkResponse() != null) {
                                playVideo(channelLinkResponse.getChannelLinkResponse().getChannel().getLink(), false);
                            } else if (channelLinkResponse.getException() != null) {

//                                setErrorFragment(channelLinkResponse.getException(), 0, 0);
                                playVideo("", false);
                            }
                            videoLinkData.removeObserver(this);
                        }
                    }
                });
            }
        } catch (Exception ignored) {
            showGridMenu();
        }
    }

    @Override
    public void loadDvr(ChannelItem current, List<ChannelItem> allChannelItems, String dvr) {
        dvrFragment = new DvrFragment();
        dvrFragment.setSelectedDvrPosition(selectedDvrPosition);
        dvrFragment.setGetSelectedDatePosition(selectedDvrDate);
        dvrFragment.setCurrentPlayedEpg(currentPlayedEpg);
        dvrFragment.setCurrentChannel(current);
        dvrFragment.setAllChannelList(allChannelList);
        load(dvrFragment, dvr);

    }


    public void setErrorFragment(Exception exception, int what, int extra) {
        PlayBackErrorEntity errorEntity = null;
        if (exception != null) {
            if (exception instanceof MaxTvUnhandledException) {
                {
                    loadSplash();
                    return;
                }

            } else
                errorEntity = DataUtils.getErrorEntity(this, exception);
        } else {
            StringBuilder sb = new StringBuilder().append("MEDIA_ERROR:\t").append("W").append(what).append("E").append(extra);
            errorEntity = new PlayBackErrorEntity(2, sb.toString(), getString(R.string.err_media_error));
        }

        ErrorFragment errorFragment = new ErrorFragment();
        errorFragment.setPlayBackErrorEntity(errorEntity);
        showMenuBg();
        stopCloseMenuHandler();
        hideProgressBar();
        if (currentFragment instanceof FragmentMenu) {
            if (menuFragment.isVisible()) {
                menuFragment.showErrorFrag(errorFragment);
            } else {
                menuFragment.setErrorFragMent(errorFragment);
            }
        } else if (currentFragment instanceof GridMenuFragment) {
            if (gridMenuFragment.isVisible()) {
                gridMenuFragment.showErrorFrag(errorFragment);
            } else {
                gridMenuFragment.setErrorFragMent(errorFragment);
            }
        } else

            showDvrMenu();
    }

    private void loadSplash() {
        videoPlayViewModel.nukeLoginTable();
    }

    private void saveCurrentInPrefs(ChannelItem item) {
        lastPlayedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = lastPlayedPrefs.edit();
        editor.putInt(CHANNEL_ID, item.getId());
        editor.apply();
    }

    @Override
    public void load(Fragment epgFragment, String tag) {
        openFragmentWithBackStack(epgFragment, tag);
    }


    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        toExitHandler.removeCallbacks(null);
        Log.d("activity_state", "onPause");
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
        try {
            getSupportFragmentManager().beginTransaction().remove(menuFragment).commit();
           menuFragment=null;
        } catch (Exception ignored) {
        }
        finish();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d("activity_state", "onStop");
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
        try {
            getSupportFragmentManager().beginTransaction().remove(gridMenuFragment).commit();
            gridMenuFragment = null;
        } catch (Exception ignored) {
        }
        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        hideMacDisplayHandler();
        super.onDestroy();
    }

    private void playVideo(String channelLink, boolean isDvr) {
        Log.d("media", channelLink);
        startAutoPlay = true;
        inErrorState = false;
        if (!isDvr) {
            if (currentFragment instanceof FragmentMenu)
                menuFragment.hideErrorFrag();
            else if (currentFragment instanceof GridMenuFragment)
                gridMenuFragment.hideErrorFrag();
//            releasePlayer();
        }

        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory();
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            trackSelector.setParameters(trackSelectorParameters);
            DefaultAllocator allocator = new DefaultAllocator(true, 64 * 1024);
            DefaultLoadControl defaultLoadControl = new DefaultLoadControl();
            DefaultLoadControl.Builder loadControl = new DefaultLoadControl.Builder().setAllocator(allocator).setBufferDurationsMs(5000, 60000, 1000, 1000);
            defaultLoadControl = loadControl.createDefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(this, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
            player = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector, defaultLoadControl);
        }
        if (playerEventListener != null)
            player.removeListener(playerEventListener);
        videoSurfaceView.setAspectRatioListener(this);
        playerEventListener = new PlayerEventListener();
        player.addListener(playerEventListener);
        player.setPlayWhenReady(true);
        videoSurfaceView.setPlayer(player);
        videoSurfaceView.setKeepScreenOn(true);
        SurfaceView surfaceView = (SurfaceView) videoSurfaceView.getVideoSurfaceView();
        surfaceView.setZOrderMediaOverlay(true);
        player.setVideoSurfaceView((SurfaceView) videoSurfaceView.getVideoSurfaceView());
        final float scale = getResources().getDisplayMetrics().density;
        /*if (scale > 1.4)
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);*/
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        videoSurfaceView.setControllerVisibilityListener(this);
        if (isDvr) {
            videoSurfaceView.setUseController(true);
            recordedStatus.setVisibility(View.VISIBLE);
            videoSurfaceView.showController();
            isDvrPlaying = true;
            menuFragment.setDvr(true);
        } else {
            videoSurfaceView.setUseController(false);
            isDvrPlaying = false;
            recordedStatus.setVisibility(View.GONE);
            menuFragment.setDvr(false);
            videoSurfaceView.hideController();
            menuFragment.setDvrPlayedChannel(null);
        }
//        String splitUrl = "udp://@224.0.0.2:8002";
//        String splitUrl = "udp://@237.1.1.9:8002";
//        String splitUrl = "udp://@239.1.9.53:8002";
//        String splitUrl="udp://@239.1.1.3:8222";
//        String splitUrl = "http://103.115.207.22/testind/reptest.stream/playlist.m3u8";
//        String splitUrl = "http://103.115.207.22/testind/channel7hd.stream/playlist.m3u8";
//        String spliturl="https://mnmott.nettvnepal.com.np/test01/sample_movie.mp4/playlist.m3u8";
//        String splitUrl = "http://mnmott.nettvnepal.com.np:81/test01/sample_movie.mp4/playlist.m3u8";

        MediaSource mediaSource = buildMediaSource(Uri.parse(channelLink), isDvr);

        boolean haveResumePosition = startWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            player.seekTo(startWindow, startPosition);
        }
        player.prepare(mediaSource, !haveResumePosition, false);
        inErrorState = false;

//        String link = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    }

    private MediaSource buildMediaSource(
            Uri uri, boolean isDvr) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(dataSourceFactory),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(dataSourceFactory),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                if (isDvr) {
                    return new HlsMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(uri);
                } else {
                    return new HlsMediaSource.Factory(dataSourceFactory)
                            .setAllowChunklessPreparation(true)
                            .createMediaSource(uri);
                }
            case C.TYPE_OTHER:
               /* try {
                    UdpDataSource.Factory udpDataSource = dataSourceFactory;
                    return new ExtractorMediaSource.Factory(udpDataSource)
                            .setExtractorsFactory(new DefaultExtractorsFactory().setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
                                    | DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS).setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
                                    .setFragmentedMp4ExtractorFlags(FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS))
                            .createMediaSource(uri);
                } catch (Exception e) {*/
                return new ExtractorMediaSource.Factory(dataSourceFactory)
                        .setExtractorsFactory(new DefaultExtractorsFactory().setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
                                | DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS))
                        .createMediaSource(uri);
//                }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private void loadNextDvr() {
        showProgressBar();
        long utc = GetUtc.getInstance().getTimestamp().getUtc();
        Login login = GlobalVariables.login;
        videoPlayViewModel.getNextDvrLink(login.getToken(), utc, login.getId(),
                LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc), login.getSession()),
                currentDvrChannelItem.getId(), nextVideoNameDvr).observe(this, new Observer<DvrLinkResponse>() {
            @Override
            public void onChanged(@Nullable DvrLinkResponse channelLinkResponse) {
                if (channelLinkResponse != null) {
                    if (channelLinkResponse.getErrorCode() > 0) {
                        Log.d("dvr", "error");
                        Toast.makeText(VideoPlayActivity.this, "Dvr couldn't be played", Toast.LENGTH_SHORT).show();
                        showDvrMenu();
                        hideProgressBar();
                    } else {
                        setUpVideoController(currentDvrChannelItem, channelLinkResponse.getLink(), channelLinkResponse.getNextVideoName());
                    }
                }
            }
        });

    }

    private void showDvrMenu() {
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag == null)
//            openFragment(menuFragment);
            openFragment(gridMenuFragment);
        else {
            try {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(menuFrag).commit();
            } catch (Exception ignored) {
            }


        }
    }

    private void startCloseMenuHandler() {
        hideMenuHandler.postDelayed(closeFragmentRunnable, 15 * 1000);

    }

    public void stopCloseMenuHandler() {
        hideMenuHandler.removeCallbacks(closeFragmentRunnable);
    }

    private Runnable closeFragmentRunnable = new Runnable() {
        @Override
        public void run() {
            try {

                hideMenuUI();


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private void hideMenuUI() {
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag != null)
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).hide(menuFrag).commit();
    }

    private void showMenuBg() {
        menuBackground.setVisibility(View.VISIBLE);
    }

    private void hideMenuBg() {
        menuBackground.setVisibility(View.GONE);
    }


    private void hideProgressBar() {
        progressBar.smoothToHide();
    }

    private void showProgressBar() {
        progressBar.smoothToShow();
        progressBar.bringToFront();
    }

    @Override
    public void playChannelFromOnAir(ChannelItem channel, int channelPositionById, boolean onAir) {
        getSupportFragmentManager().popBackStack();
        menuFragment.onClickChannel(getString(R.string.all_channels), 0, channelPositionById, allChannelList);
//        playChannel(channel);
    }

    @Override
    public void playDvr(Epgs epgs, ChannelItem item, int selectedPosition, int selectedDatePosition) {
        selectedDvrPosition = selectedPosition;
        selectedDvrDate = selectedDatePosition;
        currentPlayedEpg = epgs;
        saveCurrentInPrefs(item);
        menuFragment.setDvrPlayedChannel(item);
        showProgressBar();
        long utc = GetUtc.getInstance().getTimestamp().getUtc();
        Login login = GlobalVariables.login;
        String date = DateUtils.dateAndTime.format(epgs.getStartTime());
        String startTime = DateUtils._24HrsTimeFormat.format(epgs.getStartTime());
       /* LiveData<ChannelLinkResponseWrapper> videoLinkData = videoPlayViewModel.getChannelLink(login.getToken(), utc, login.getId(),
                LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc),
                        login.getSession()), macAddress, item.getId());
        videoLinkData.observe(VideoPlayActivity.this, new Observer<ChannelLinkResponseWrapper>() {
            @Override
            public void onChanged(@Nullable ChannelLinkResponseWrapper channelLinkResponse) {
                if (channelLinkResponse != null) {
                    if (channelLinkResponse.getChannelLinkResponse() != null) {
                        String url = channelLinkResponse.getChannelLinkResponse().getChannel().getLink();
                        String[] parts = url.split(".m3u8");
                        String part1 = parts[0];
                        String part2 = "";
                        try {
                            part2 = parts[1];
                        } catch (Exception ignored) {
                        }
//                        long milis = epgs.getStartTime().getTime();
                        long millis = Calendar.getInstance().getTimeInMillis() - TimeUnit.MINUTES.toMillis(10);
                        long diff = Calendar.getInstance().getTimeInMillis() - millis;
                        millis = millis / 1000;

//                        milis = milis / 1000;
//                        long diff = epgs.getEndTime().getTime() - epgs.getStartTime().getTime();
                        long seconds = diff / 1000;
                        String buildUrl = part1 + "_dvr_" + String.valueOf(millis) + "-" + String.valueOf(seconds) + ".m3u8" + part2;
                        Log.d("buildurl", buildUrl);
                        setUpVideoController(item, buildUrl, "");
                    } else if (channelLinkResponse.getException() != null) {
                        hideProgressBar();
                        Toast.makeText(VideoPlayActivity.this, "Dvr cannot be played", Toast.LENGTH_SHORT).show();
                    }
                    videoLinkData.removeObserver(this);
                }
            }
        });*/
        LiveData<NewDvrEntity> dvrData = videoPlayViewModel.getDvrLink(login.getToken(), utc, login.getId(),
                LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc), login.getSession()),
                epgs.getChannelID(), date, startTime);
        dvrData.observe(this, new Observer<NewDvrEntity>() {
            @Override
            public void onChanged(@Nullable NewDvrEntity channelLinkResponse) {
             /*   String buildUrl="http://www.streambox.fr/playlists/test_001/stream.m3u8";
                setUpVideoController(item, buildUrl, "");*/
                if (channelLinkResponse != null) {
                    if (channelLinkResponse.getDvrPath() != null && !channelLinkResponse.getDvrPath().isEmpty()) {
                        String url = channelLinkResponse.getDvrPath();
                        String[] parts = url.split(".m3u8");
                        String part1 = parts[0];
                        String part2 = "";
                        try {
                            part2 = parts[1];
                        } catch (Exception ignored) {
                        }
//                        long milis = epgs.getStartTime().getTime();
                        long millis = Calendar.getInstance().getTimeInMillis() - TimeUnit.MINUTES.toMillis(20);
                        long diff = Calendar.getInstance().getTimeInMillis() - millis;
                        millis = millis / 1000;

//                        milis = milis / 1000;
//                        long diff = epgs.getEndTime().getTime() - epgs.getStartTime().getTime();
                        long seconds = diff / 1000;
//                        String buildUrl = part1 + "-" + String.valueOf(millis) + "-" + String.valueOf(seconds) + ".m3u8" + part2;
//                        String buildUrl = part1 + "-" + String.valueOf(millis) + "-" + String.valueOf(seconds) + ".m3u8";
                        String buildUrl = part1 + "_range-" + String.valueOf(millis) + "-" + String.valueOf(seconds) + ".m3u8" + part2;

                        Log.d("buildurl", buildUrl);
                        setUpVideoController(item, buildUrl, "");
                    } else if (channelLinkResponse.getDvrPath() == null) {
                        hideProgressBar();
                        Toast.makeText(VideoPlayActivity.this, "Dvr cannot be played", Toast.LENGTH_SHORT).show();
                    }
                    dvrData.removeObserver(this);
                }
            }
            });



    }

    private void setUpVideoController(ChannelItem item, String link, String nextVideoName) {
        currentDvrChannelItem = item;
        nextVideoNameDvr = nextVideoName;
        VideoPlayActivity.this.playVideo(link, true);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginDataDelete event) {
        int vav = event.getLongVa();
        Intent i = new Intent(VideoPlayActivity.this, SplashActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();

    }

    @Override
    public void onVisibilityChange(int visibility) {
        if (visibility == VISIBLE) {
            currentPlayingDvrChannel.setText(currentDvrChannelItem.getName());
            currentPlayingDvrChannel.setVisibility(VISIBLE);
        } else {
            currentPlayingDvrChannel.setVisibility(GONE);
        }

    }

    @Override
    public void onAspectRatioUpdated(float targetAspectRatio, float naturalAspectRatio, boolean aspectRatioMismatch) {

    }

    private class PlayerEventListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
//                showControls();
                if (isDvrPlaying) {
                    player.seekTo(0);
                    player.setPlayWhenReady(false);
                    videoSurfaceView.setUseController(false);
                    Toast.makeText(VideoPlayActivity.this, "Play back completed.Please choose another video to play", Toast.LENGTH_SHORT).show();
                    showDvrMenu();
                    stopCloseMenuHandler();
                    player.removeListener(this);


                }
            } else if (playWhenReady && playbackState == Player.STATE_READY) {
                hideProgressBar();
                hideMenuBg();
                if (threadToDisplayBoxId.getState() == Thread.State.NEW) {
                    threadToDisplayBoxId.start();
                }
                if (currentFragment != null && currentFragment.isVisible()) {
                    if (currentFragment instanceof FragmentMenu)
                        menuFragment.hideErrorFrag();
                    else if (currentFragment instanceof GridMenuFragment)
                        gridMenuFragment.hideErrorFrag();
                } else {
                    menuFragment.setErrorFragMent(null);
                    gridMenuFragment.setErrorFragMent(null);
                }
                startCloseMenuHandler();
                if (isDvrPlaying) {
                    playPauseStatus.setVisibility(View.GONE);
                    player.setPlayWhenReady(true);
                    hideMenuUI();
                } else {
                    VideoPlayActivity.this.startCloseMenuHandler();
                    if (!menuFragment.isVisible())
                        VideoPlayActivity.this.showPriorityNo();
                }
            } else if (!playWhenReady && playbackState == Player.STATE_READY) {
                if (isDvrPlaying) {
                    playPauseStatus.setVisibility(View.VISIBLE);
                    playPauseStatus.bringToFront();
                }

            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (inErrorState) {
                // This will only occur if the user has performed a seek whilst in the error state. Update
                // the resume position so that if the user then retries, playback will resume from the
                // position to which they seeked.
                updateResumePosition();
                hideProgressBar();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            String errorString = null;
            switch (e.type) {
                case ExoPlaybackException.TYPE_RENDERER:
                    Exception cause = e.getRendererException();
                    if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                        // Special case for decoder initialization failures.
                        MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                                (MediaCodecRenderer.DecoderInitializationException) cause;
                        if (decoderInitializationException.decoderName == null) {
                            if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                                errorString = getString(R.string.error_querying_decoders);
                            } else if (decoderInitializationException.secureDecoderRequired) {
                                errorString = getString(R.string.error_no_secure_decoder,
                                        decoderInitializationException.mimeType);
                            } else {
                                errorString = getString(R.string.error_no_decoder,
                                        decoderInitializationException.mimeType);
                            }
                        } else {
                            errorString = getString(R.string.error_instantiating_decoder,
                                    decoderInitializationException.decoderName);
                        }
                    } else {
                        errorString = e.getRendererException().getMessage();
                    }
                    break;
                case ExoPlaybackException.TYPE_SOURCE:
                    errorString = e.getSourceException().getMessage();
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    errorString = e.getUnexpectedException().getMessage();
                    break;
            }


            if (errorString != null) {
                Timber.d("on error ");
                hideProgressBar();
                hideMacDisplayHandler();
                stopCloseMenuHandler();
                showDvrMenu();
                //Toast.makeText(VideoPlayActivity.this, "PlayBack Error:: Playing Media" + sb.toString(), Toast.LENGTH_LONG).show();
                if (isDvrPlaying) {
                    videoSurfaceView.setUseController(false);
                    isDvrPlaying = false;
                    Toast.makeText(VideoPlayActivity.this, "PlayBack Error:: Playing Media" + errorString, Toast.LENGTH_LONG).show();
                } else {
                    player.setPlayWhenReady(false);
                    player.removeListener(playerEventListener);
                    VideoPlayActivity.this.setErrorFragment(null, e.type, e.rendererIndex);
                }

            }
            inErrorState = true;
            if (isBehindLiveWindow(e)) {
                clearResumePosition();
            } else {
                updateResumePosition();
//                showControls();
            }
        }

        private void showToast(int messageId) {
            showToast(getString(messageId));
        }

        private void showToast(String message) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            if (trackGroups != lastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }

    }

    private void updateResumePosition() {
        startWindow = player.getCurrentWindowIndex();
        startPosition = Math.max(0, player.getContentPosition());
    }

    private void clearResumePosition() {
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
