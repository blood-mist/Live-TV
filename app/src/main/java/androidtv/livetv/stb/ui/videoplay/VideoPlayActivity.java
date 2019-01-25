package androidtv.livetv.stb.ui.videoplay;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponseWrapper;
import androidtv.livetv.stb.entity.DvrLinkResponse;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginDataDelete;
import androidtv.livetv.stb.entity.PlayBackErrorEntity;
import androidtv.livetv.stb.ui.custom_views.CustomTextView;
import androidtv.livetv.stb.ui.splash.SplashActivity;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.fragments.dvr.DvrFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.error.ErrorFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.FragmentMenu;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.GridMenuFragment;
import androidtv.livetv.stb.ui.videoplay.videocontroller.MyVideoController;
import androidtv.livetv.stb.ui.videoplay.videocontroller.VideoControllerView;
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
import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;
import static androidtv.livetv.stb.utils.LinkConfig.PLAYED_CATEGORY_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.SELECTED_CATEGORY_NAME;
import static java.lang.Thread.sleep;

public class VideoPlayActivity extends AppCompatActivity implements FragmentMenu.FragmentMenuInteraction,
        EpgFragment.FragmentEpgInteraction, DvrFragment.FragmentDvrInteraction, MyVideoController.MediaPlayerLis, IVLCVout.Callback,GridMenuFragment.OnFragmentInteractionListener{

    @BindView(R.id.img_play_pause)
    ImageView playPauseStatus;
    @BindView(R.id.videoStatus)
    ImageView recordedStatus;
    @BindView(R.id.progressBar1)
    AVLoadingIndicatorView progressBar;
    @BindView(R.id.tv_setchannelfrompriority)
    TextView txtChangeChannelFromPriority;
    @BindView(R.id.tvboxID)
    TextView txtRandomDisplayBoxId;
    @BindView(R.id.mainImage)
    ImageView mainImage;
    @BindView(R.id.transparentLoadingBackground)
    LinearLayout backgroundTransparent;
    @BindView(R.id.container_movie_player)
    FrameLayout errorFrame;
    @BindView(R.id.videoSurfaceContainer)
    FrameLayout videoSurfaceContainer;
    @BindView(R.id.videoSurface)
    SurfaceView videoSurfaceView;
    @BindView(R.id.menu_bg)
    ImageView menuBackground;
    @BindView(R.id.priority_view)
    CustomTextView priorityView;


    private VideoPlayViewModel videoPlayViewModel;
    private GridMenuFragment gridMenuFragment = new GridMenuFragment();
    private FragmentMenu menuFragment = new FragmentMenu();
    private DvrFragment dvrFragment = new DvrFragment();
    private Fragment currentFragment;
    private MediaPlayer player;
    private ChannelChangeObserver channelChangeObservable;
    private VideoControllerView mVideoController;
    private Handler hideMenuHandler;
    private SharedPreferences lastPlayedPrefs;
    private SharedPreferences.Editor editor;
    String macAddress;
    LibVLC libVLC;
    private ChannelItem currentDvrChannelItem;
    private String nextVideoNameDvr;
    private boolean isDvrPlaying;
    private ChannelItem currentPlayingChannel;
    private Handler handlerToHidePriority, chFrmPriorityHandler;
    private List<ChannelItem> allChannelList;
    private int selectedDvrPosition = 0;
    private int selectedDvrDate = 0;
    private Epgs currentPlayedEpg;
    private int lastPlayedId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_video_play);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        chFrmPriorityHandler = new Handler();
        lastPlayedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);
        hideMenuHandler = new Handler();
        macAddress = AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this);
        txtRandomDisplayBoxId.setText(macAddress);
        initSurafaceView();

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
    private void initSurafaceView() {
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
        /*ArrayList<String> options = new ArrayList<String>();
        options.add("--subsdec-encoding <encoding>");
        options.add("--aout=opensles");
        options.add("--audio-time-stretch"); // time stretching
        options.add("-vvv"); // verbosity*/
        libVLC = new LibVLC(this);
        SurfaceHolder videoHolder = videoSurfaceView.getHolder();
        videoHolder.setFixedSize(w,h);
       /* ViewGroup.LayoutParams lp = videoSurfaceView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        videoSurfaceView.setLayoutParams(lp);
        videoSurfaceView.invalidate();*/
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (currentFragment instanceof EpgFragment) {
                    removeOptionalFragments();

                } else {
                    try {
                        if (currentFragment instanceof DvrFragment) {
                            if (mVideoController == null) {
                                removeOptionalFragments();
                            } else {
                                if (currentFragment.isHidden())
                                    showDvrMenu();
                                else hideMenuUI();
                            }
                        } else if (currentFragment instanceof FragmentMenu) {

                            if (mVideoController != null && currentFragment.isHidden()) {
                                dvrFragment.setCurrentPlayedEpg(currentPlayedEpg);
                                dvrFragment.setGetSelectedDatePosition(selectedDvrDate);
                                dvrFragment.setSelectedDvrPosition(selectedDvrPosition);
                                load(dvrFragment, "dvr");
                            } else if (player != null && player.isPlaying()) {
                                if (currentFragment != null && currentFragment.isVisible())
                                    hideMenuUI();
                                else {
                                    showMenu();
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }


                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                try {
                    if (mVideoController != null && !mVideoController.isShowing() && menuFrag.isHidden() && player.getTime() > 0) {
                        player.setTime(player.getTime() + 15000);
                        return true;
                    } else
                        return false;
                } catch (Exception ignored) {
                    break;
                }


            case KeyEvent.KEYCODE_DPAD_LEFT:
                try {
                    if (mVideoController != null && !mVideoController.isShowing() && menuFrag.isHidden() && player.getTime() > 0) {
                        player.setTime(player.getTime() - 15000);
                        return true;
                    } else
                        return false;
                } catch (Exception ignored) {
                    break;
                }

            case KeyEvent.KEYCODE_MEDIA_REWIND:
                player.setTime(player.getTime() - 5 * 60 * 1000);
                return true;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                player.setTime(player.getTime() + 5 * 60 * 1000);
                return true;


            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (mVideoController == null) {
                    hideMenuUI();
                    changeChannel(false);
                    return true;
                } else {
                    return false;
                }

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (mVideoController == null) {
                    hideMenuUI();
                    changeChannel(true);
                    return true;
                } else {
                    return false;
                }

            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                hideMenuUI();
                changeChannel(false);
                return true;


            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (menuFrag == null || menuFrag.isHidden()) {
                    if (mVideoController == null)
                        changeChannel(false);
                    return true;
                } else {
                    return false;
                }
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case 63:
                if (mVideoController != null) {
                    mVideoController.show();
                    return true;
                } else if ((menuFrag == null || menuFrag.isHidden()) && txtChangeChannelFromPriority.getVisibility() == View.GONE) {
                    showMenu();
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
                    if (mVideoController == null)
                        changeChannel(true);
                    return true;
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
            showMenu();
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
                    if ((isNext)) {
                        if (currentChannelPosition < channelItemList.size() - 1)
                            currentChannelPosition++;
                        else
                            currentChannelPosition = 0;

                    } else if (!isNext) {
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

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof EpgFragment) {
            removeOptionalFragments();
        } else {
            if (currentFragment instanceof DvrFragment) {
                if (!isDvrPlaying) {
                    removeOptionalFragments();
                } else {
                    if (currentFragment.isHidden()) showDvrMenu();
                    else removeOptionalFragments();
                }
            } else if ((currentFragment instanceof FragmentMenu || currentFragment instanceof GridMenuFragment) && currentFragment.isVisible()) {
                releasePlayer();
                getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
                finish();
                super.onBackPressed();
            } else {
                showMenu();
            }
        }

    }

    private void releasePlayer() {
        if (libVLC == null)
            return;
        player.stop();
        final IVLCVout vout = player.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        libVLC.release();
        libVLC = null;
    }

    private void removeOptionalFragments() {
        getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
        currentFragment = menuFragment;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(menuFragment).commit();

    }

    private void showMenu() {
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag == null)
//            openFragment(menuFragment);
            openFragment(gridMenuFragment);
        else {
            if (menuFrag instanceof GridMenuFragment)
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(menuFrag).commit();
            else {
                if (gridMenuFragment == null) {
                    gridMenuFragment = new GridMenuFragment();
                }
                removeOptionalFragments();
            }

        }
    }

    private void showMenuFrag(ErrorFragment errorFragment) {
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

    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopCloseMenuHandler();
        try {
            if (player.getTime() > 0 && player.isPlaying())
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
        mVideoController = null;
        selectedDvrDate = 0;
        selectedDvrPosition = 0;
        currentPlayedEpg = null;
        Timber.d("Played Channel:" + item.getName());
        //TODO play Channels
        try {
            if (currentPlayingChannel != null && currentPlayingChannel.getId() == item.getId() && player != null && player.isPlaying() && player.getTime() > 0 && !isDvrPlaying) {
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
                                setErrorFragment(channelLinkResponse.getException(), 0, 0);
                            }
                            videoLinkData.removeObserver(this);
                        }
                    }
                });
            }
        } catch (Exception ignored) {
            showMenu();
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
                errorEntity = errorEntity = DataUtils.getErrorEntity(this, exception);
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
        Log.d("activity_state", "onPause");
        releasePlayer();
        try {
            getSupportFragmentManager().beginTransaction().remove(menuFragment).commit();
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
        releasePlayer();
        try {
            getSupportFragmentManager().beginTransaction().remove(menuFragment).commit();
            menuFragment = null;
        } catch (Exception ignored) {
        }
        ;
        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        hideMacDisplayHandler();
        super.onDestroy();
    }

    private void playVideo(String channelLink, boolean isDvr) {
        Log.d("media", channelLink);
        if (player != null) {
           player.release();
        }
       player=new MediaPlayer(libVLC);
        String link = "udp://@239.1.20.1:8002";
        try {
            player.setEventListener(new MediaPlayer.EventListener() {
                @Override
                public void onEvent(MediaPlayer.Event event) {
                    switch (event.type) {
                        case MediaPlayer.Event.EndReached:
                            if (isDvr) {
                                showDvrMenu();
                                releasePlayer();
                                Toast.makeText(VideoPlayActivity.this, "Play back completed.Please choose another video to play", Toast.LENGTH_SHORT).show();
                                //loadNextDvr();
                            }
                            break;
                        case MediaPlayer.Event.Playing:
                            hideMenuBg();
                            hideProgressBar();

                            if (threadToDisplayBoxId.getState() == Thread.State.NEW) {
                                threadToDisplayBoxId.start();
                            }

                            startCloseMenuHandler();
                            if (isDvr) {
                                MyVideoController controller = new MyVideoController(VideoPlayActivity.this, player, currentDvrChannelItem, VideoPlayActivity.this);
                                mVideoController = new VideoControllerView(VideoPlayActivity.this, true);
                                mVideoController.setAnchorView(videoSurfaceContainer);
                                mVideoController.setMediaPlayer(controller, player);
                                mVideoController.show();
                                recordedStatus.setVisibility(View.VISIBLE);
                                isDvrPlaying = true;
//                                menuFragment.setDvr(true);
                            } else {
                                isDvrPlaying = false;
                                recordedStatus.setVisibility(View.GONE);
//                                menuFragment.setDvr(false);
//                                menuFragment.setDvrPlayedChannel(null);


                            }

                            if (isDvr) {
                                hideMenuUI();
                            } else {
                                VideoPlayActivity.this.startCloseMenuHandler();
                                /*if (!menuFragment.isVisible())
                                    VideoPlayActivity.this.showPriorityNo();

                                if (menuFragment != null && menuFragment.isAdded()&& menuFragment.isVisible()) {
                                    menuFragment.hideErrorFrag();
                                } else {
                                    menuFragment.setErrorFragMent(null);
                                }*/
                            }

                            break;
                        case MediaPlayer.Event.EncounteredError:
                            Timber.d("on error ");
                            hideProgressBar();
                            hideMacDisplayHandler();
                            stopCloseMenuHandler();
                            showDvrMenu();
//                            StringBuilder sb = new StringBuilder().append("MEDIA_ERROR:\t").append("W").append(event.).append("E").append(extra);
                            //Toast.makeText(VideoPlayActivity.this, "PlayBack Error:: Playing Media" + sb.toString(), Toast.LENGTH_LONG).show();
                            if (isDvr) {
                                isDvrPlaying = false;
                                Toast.makeText(VideoPlayActivity.this, "PlayBack Error:: Playing Media", Toast.LENGTH_LONG).show();
                                VideoPlayActivity.this.stopCloseMenuHandler();

                            } else {
                                VideoPlayActivity.this.setErrorFragment(null, 404, 1);
                            }
                            break;
                        case MediaPlayer.Event.Paused:
                        case MediaPlayer.Event.Stopped:
                        default:
                            break;
                    }
                }
            });
            final IVLCVout vout = player.getVLCVout();
            vout.setVideoView(videoSurfaceView);
            vout.addCallback(this);
            vout.attachViews();
            Media m = new Media(libVLC,Uri.parse(link));
            player.setMedia(m);
            player.play();

            } catch(Exception e){
                e.printStackTrace();
            }

        }


        private void loadNextDvr () {
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

        private void showDvrMenu () {
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

        private void startCloseMenuHandler () {
            hideMenuHandler.postDelayed(closeFragmentRunnable, 15 * 1000);

        }

        public void stopCloseMenuHandler () {
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

        private void hideMenuUI () {
            Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
            if (menuFrag != null)
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).hide(menuFrag).commit();
        }

        private void showMenuBg () {
            menuBackground.setVisibility(View.VISIBLE);
        }

        private void hideMenuBg () {
            menuBackground.setVisibility(View.GONE);
        }


        private void hideProgressBar () {
            progressBar.smoothToHide();
        }

        private void showProgressBar () {
            progressBar.smoothToShow();
            progressBar.bringToFront();
        }

        @Override
        public void playChannelFromOnAir (ChannelItem channel,int channelPositionById, boolean onAir)
        {
            getSupportFragmentManager().popBackStack();
            menuFragment.onClickChannel(getString(R.string.all_channels), 0, channelPositionById, allChannelList);
//        playChannel(channel);
        }

        @Override
        public void playDvr (Epgs epgs, ChannelItem item,int selectedPosition,
        int selectedDatePosition){
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
        /*LiveData<ChannelLinkResponseWrapper> videoLinkData = videoPlayViewModel.getChannelLink(login.getToken(), utc, login.getId(),
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
                        String part2 = parts[1];
                        long milis = epgs.getStartTime().getTime();
                        milis = milis/1000;
                        long diff = epgs.getEndTime().getTime() - epgs.getStartTime().getTime();
                        long seconds = diff / 1000;
                        String buildUrl = part1+"-"+String.valueOf(milis)+"-"+String.valueOf(seconds)+".m3u8"+part2;
                        Log.d("buiFldurl",buildUrl);
                        setUpVideoController(item, buildUrl,"");
                    } else if (channelLinkResponse.getException() != null) {
                        Toast.makeText(VideoPlayActivity.this, "Dvr cannot be played", Toast.LENGTH_SHORT).show();
                    }
                    videoLinkData.removeObserver(this);
                }
            }
        });*/
            LiveData<DvrLinkResponse> dvrData = videoPlayViewModel.getDvrLink(login.getToken(), utc, login.getId(),
                    LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc), login.getSession()),
                    epgs.getChannelID(), date, startTime);
            dvrData.observe(this, new Observer<DvrLinkResponse>() {
                @Override
                public void onChanged(@Nullable DvrLinkResponse channelLinkResponse) {
                    if (channelLinkResponse != null) {
                        if (channelLinkResponse.getLink() != null) {
                            String url = channelLinkResponse.getLink();
                            String[] parts = url.split(".m3u8");
                            String part1 = parts[0];
                            String part2 = parts[1];
                            long milis = epgs.getStartTime().getTime();
                            milis = milis / 1000;
                            long diff = epgs.getEndTime().getTime() - epgs.getStartTime().getTime();
                            long seconds = diff / 1000;
                            String buildUrl = part1 + "-" + String.valueOf(milis) + "-" + String.valueOf(seconds) + ".m3u8" + part2;
                            Log.d("buiFldurl", buildUrl);
                            setUpVideoController(item, buildUrl, "");
                        } else {
                            Toast.makeText(VideoPlayActivity.this, "Dvr cannot be played", Toast.LENGTH_SHORT).show();
                        }
                        dvrData.removeObserver(this);

                    }
                }
            });


        }

        private void setUpVideoController (ChannelItem item, String link, String nextVideoName){
            currentDvrChannelItem = item;
            nextVideoNameDvr = nextVideoName;
            VideoPlayActivity.this.playVideo(link, true);


        }


        @Override
        public void onDvrStart () {
            playPauseStatus.setVisibility(View.GONE);
            player.play();
        }

        @Override
        public void onDvrPause () {
            playPauseStatus.setVisibility(View.VISIBLE);
            playPauseStatus.bringToFront();
            player.pause();

        }


        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onMessageEvent (LoginDataDelete event){
            int vav = event.getLongVa();
            Intent i = new Intent(VideoPlayActivity.this, SplashActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();

        }

        @Override
        public void onSurfacesCreated (IVLCVout vlcVout){
        }

        @Override
        public void onSurfacesDestroyed (IVLCVout vlcVout){

        }


    }
