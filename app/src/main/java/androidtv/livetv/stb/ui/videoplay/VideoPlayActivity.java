package androidtv.livetv.stb.ui.videoplay;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.ChannelLinkResponseWrapper;
import androidtv.livetv.stb.entity.DvrLinkResponse;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.FavEvent;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginDataDelete;
import androidtv.livetv.stb.entity.PlayBackErrorEntity;
import androidtv.livetv.stb.ui.splash.SplashActivity;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.fragments.dvr.DvrFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.error.ErrorFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.FragmentMenu;
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

import timber.log.Timber;

import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;

public class VideoPlayActivity extends AppCompatActivity implements FragmentMenu.FragmentMenuInteraction,
        SurfaceHolder.Callback, EpgFragment.FragmentEpgInteraction, DvrFragment.FragmentDvrInteraction, MyVideoController.MediaPlayerLis {

    @BindView(R.id.img_play_pause)
    ImageView playPauseStatus;
    @BindView(R.id.videoStatus)
    ImageView recordedStatus;
    @BindView(R.id.progressBar1)
    AVLoadingIndicatorView progressBar;
    @BindView(R.id.tv_setchannelfrompriority)
    EditText txtChangeChannelFromPriority;
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


    private VideoPlayViewModel videoPlayViewModel;
    private Handler handlerToShowMac;
    private Handler handlerToHideMac;
    private Runnable runnableToHideMac;
    private Runnable runnableToShowMac;
    private FragmentMenu menuFragment = new FragmentMenu();
    private Fragment currentFragment;
    private List<CategoryItem> mCategoryList = new ArrayList<>();
    private List<ChannelItem> mChannelList = new ArrayList<>();
    private MediaPlayer player;
    private ChannelChangeObserver channelChangeObservable;
    private VideoControllerView mVideoController;
    private Handler hideMenuHandler;
    private SharedPreferences lastPlayedPrefs;
    private SharedPreferences.Editor editor;
    String macAddress;
    private ChannelItem currentDvrChannelItem;
    private String nextVideoNameDvr;
    private boolean isDvrPlaying;
    private ChannelItem currentPlayingChannel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        lastPlayedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);
        hideMenuHandler = new Handler();
        macAddress = AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this);
        txtRandomDisplayBoxId.setText(macAddress);
        txtRandomDisplayBoxId.setText(AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this));
        txtRandomDisplayBoxId.setText(AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this));
        channelChangeObservable = new ChannelChangeObserver();
        channelChangeObservable.addObserver(menuFragment);
        initSurafaceView();
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        Log.d("activity_state", "onStart");
        super.onStart();
        videoPlayViewModel = ViewModelProviders.of(this).get(VideoPlayViewModel.class);
        if (menuFragment == null) {
            menuFragment = new FragmentMenu();
        }
        openFragment(menuFragment);
    }

    private void initSurafaceView() {
        SurfaceHolder videoHolder = videoSurfaceView.getHolder();
        videoHolder.addCallback(this);
        player = new MediaPlayer();
    }

    private void openFragment(Fragment fragment) {
        Log.d("frag", "called from activity");
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.container_movie_player, currentFragment).commit();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (player.isPlaying()) {
                    if (currentFragment instanceof EpgFragment) {
                        getSupportFragmentManager().popBackStack();
                    } else {
                        if (currentFragment instanceof DvrFragment) {
                            if (!isDvrPlaying) {
                                getSupportFragmentManager().popBackStack();
                            }
                        }
                    }
                    showMenu();


                }
                break;
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                if (mVideoController == null) {
                    changeToPreviousChannel();
                    return true;

                } else {
                    return false;
                }
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (menuFrag == null || menuFrag.isHidden()) {
                    if (mVideoController == null)
                        changeToPreviousChannel();
                    return true;
                } else {
                    return false;
                }
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case 63:
                if (mVideoController != null) {
                    mVideoController.show();
                    return true;
                } else
                    return false;

            case KeyEvent.KEYCODE_CHANNEL_UP:
                changeToNextChannel();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (menuFrag == null || menuFrag.isHidden()) {
                    if (mVideoController == null) changeToNextChannel();
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
                if (txtChangeChannelFromPriority.getVisibility() == View.GONE) {
                    changeChannelfromPriority((char) event.getUnicodeChar());
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void changeChannelfromPriority(char keyNumber) {
        Handler chFrmPriorityHandler = new Handler();
        txtChangeChannelFromPriority.setVisibility(View.VISIBLE);
        txtChangeChannelFromPriority.requestFocus();
        txtChangeChannelFromPriority.setText(String.valueOf(keyNumber));
        txtChangeChannelFromPriority.setSelection(txtChangeChannelFromPriority.getText().length());
        startChChangeFrmPriority(chFrmPriorityHandler, txtChangeChannelFromPriority.getText().toString());
        txtChangeChannelFromPriority.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                startChChangeFrmPriority(chFrmPriorityHandler, String.valueOf(charSequence));
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                stopChPriorityHandler(chFrmPriorityHandler);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                stopChPriorityHandler(chFrmPriorityHandler);
                startChChangeFrmPriority(chFrmPriorityHandler, editable.toString());
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.container_movie_player, currentFragment).commit();

    }

    private void stopChPriorityHandler(Handler chFrmPriorityHandler) {
        chFrmPriorityHandler.removeCallbacksAndMessages(null);
    }

    private void startChChangeFrmPriority(Handler chFrmPriorityHandler, String priorityNumber) {
        chFrmPriorityHandler.postDelayed(() -> videoPlayViewModel.getAllChannels().observe(this, channelItemList -> {
            if (channelItemList != null) {
                Optional<ChannelItem> result =
                        channelItemList.stream().filter(channelItem -> String.valueOf(channelItem.getChannelPriority()).equals(priorityNumber)).findFirst();
                try {
                    if (result.isPresent())
                        playChannel(result.get());
                    else {
                        Toast.makeText(this, "Sorry no associated channel", Toast.LENGTH_SHORT).show();
                    }
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Sorry no associated channel ", Toast.LENGTH_SHORT).show();
                }
            }
            txtChangeChannelFromPriority.setVisibility(View.GONE);
        }), TimeUnit.SECONDS.toMillis(5));
    }

    private void changeToNextChannel() {
        channelChangeObservable.setChannelNext(true);
        channelChangeObservable.notifyObservers();

    }

    private void changeToPreviousChannel() {
        channelChangeObservable.setChannelNext(false);
        channelChangeObservable.notifyObservers();
    }

    private void openFragmentWithBackStack(Fragment fragment, String tag) {
        hideProgressBar();
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.container_movie_player, fragment).addToBackStack(tag).commit();

    }

    @Override
    public void onBackPressed() {
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag != null)
            getSupportFragmentManager().beginTransaction().hide(menuFrag).commit();
        else
            finish();
        super.onBackPressed();
    }

    private void showMenu() {
        Fragment menuFrag = getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag == null)
            openFragment(menuFragment);
        else {
            if (menuFrag.isHidden()) {
                getSupportFragmentManager().beginTransaction().show(menuFrag).commit();
            } else {
                getSupportFragmentManager().beginTransaction().hide(menuFrag).commit();
            }
        }
    }

    private void showMenuFrag(ErrorFragment errorFragment) {
        Fragment menuFrag = (FragmentMenu) getSupportFragmentManager().findFragmentById(R.id.container_movie_player);
        if (menuFrag == null)
            openFragment(menuFragment);
        else {

            getSupportFragmentManager().beginTransaction().show(menuFrag).commit();
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
        if (player.isPlaying())
            startCloseMenuHandler();
    }

    private void randomDisplayMacAddress() {
        final Random random = new Random();
        handlerToShowMac = new Handler();
        handlerToHideMac = new Handler();


        final FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) txtRandomDisplayBoxId.getLayoutParams();

        runnableToHideMac = () -> {


            params.setMargins(500, random.nextInt(500),
                    random.nextInt(200), random.nextInt(200));

            txtRandomDisplayBoxId.setLayoutParams(params);
            txtRandomDisplayBoxId.setVisibility(View.INVISIBLE);
            System.out.println("box is invisible");

            handlerToShowMac.postDelayed(runnableToShowMac, TimeUnit.SECONDS.toMillis(5));
        };
        runnableToShowMac = () -> {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
//            params.setMargins(10,random.nextInt(height-20),random.nextInt(width-20),10);

            params.setMargins(random.nextInt(width - txtRandomDisplayBoxId.getWidth() + 200), random.nextInt(height - txtRandomDisplayBoxId.getHeight() + 200),
                    random.nextInt(500), random.nextInt(500));

            txtRandomDisplayBoxId.bringToFront();
            txtRandomDisplayBoxId.setLayoutParams(params);
            txtRandomDisplayBoxId.setVisibility(View.VISIBLE);
            System.out.println("box is shown");

            handlerToHideMac.postDelayed(runnableToHideMac, TimeUnit.SECONDS.toMillis(7));
        };

        handlerToShowMac.postDelayed(runnableToShowMac, TimeUnit.SECONDS.toMillis(7));

    }

    void hideMacDisplayHandler() {
        if (handlerToShowMac != null)
            handlerToShowMac.removeCallbacks(null);
    }


    @Override
    public void playChannel(ChannelItem item) {
        if (currentPlayingChannel != null && currentPlayingChannel.getId() == item.getId() && player.isPlaying()) {
        } else {
            saveCurrentInPrefs(item);
            mVideoController = null;
            currentPlayingChannel = item;
            Timber.d("Played Channel:" + item.getName());
            Log.d("activity_state", "played channel");

            showProgressBar();
            long utc = GetUtc.getInstance().getTimestamp().getUtc();
            Login login = GlobalVariables.login;
            videoPlayViewModel.getChannelLink(login.getToken(), utc, login.getId(),
                    LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc),
                            login.getSession()), macAddress, item.getId()).observe(this, new Observer<ChannelLinkResponseWrapper>() {
                @Override
                public void onChanged(@Nullable ChannelLinkResponseWrapper channelLinkResponse) {
                    if (channelLinkResponse != null) {
                        if (channelLinkResponse.getChannelLinkResponse() != null) {
                            VideoPlayActivity.this.playVideo(channelLinkResponse.getChannelLinkResponse().getChannel().getLink(), false);
                        } else if (channelLinkResponse.getException() != null) {
                            setErrorFragment(channelLinkResponse.getException(), 0, 0);
                        }
                    }
                }
            });
        }

    }


    public void setErrorFragment(Exception exception, int what, int extra) {
        PlayBackErrorEntity errorEntity = null;
        if (exception != null) {
            if(exception instanceof MaxTvUnhandledException){
                {
                    loadSplash();
                    return;
                }

            }else
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
        if (menuFragment.isVisible()) {
            menuFragment.showErrorFrag(errorFragment);
        } else {
            menuFragment.setErrorFragMent(errorFragment);
            showDvrMenu();
        }
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
        if (player.isPlaying()) {
            player.pause();
        }
        super.onPause();
        Log.d("activity_state", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("activity_state", "onResume");
        menuFragment.playLastPlayedChannel();


    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        try {
            player.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            player.release();
        } catch (Exception e) {

        }
        hideMacDisplayHandler();
        super.onDestroy();
    }

    private void playVideo(String channelLink, boolean isDvr) {
        Log.d("media", channelLink);
        if (player == null) {
            player = new MediaPlayer();
        } else player.reset();
//        String link = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(channelLink));
            player.prepareAsync();
            player.setOnPreparedListener(mp -> {
                hideMenuBg();
                hideProgressBar();
                player.setScreenOnWhilePlaying(true);
                player.start();
                if (isDvr) {
                    MyVideoController controller = new MyVideoController(this, player, currentDvrChannelItem, this);
                    mVideoController = new VideoControllerView(this, true);
                    mVideoController.setAnchorView(videoSurfaceContainer);
                    mVideoController.setMediaPlayer(controller, player);
                    mVideoController.show();
                    recordedStatus.setVisibility(View.VISIBLE);
                    isDvrPlaying = true;
                } else {
                    recordedStatus.setVisibility(View.GONE);
                }

                if (isDvr) {
                    hideMenuUI();
                } else {
                    startCloseMenuHandler();

                    if (menuFragment.isVisible()) {
                        menuFragment.hideErrorFrag();
                    } else {
                        menuFragment.setErrorFragMent(null);
                    }
                }
                randomDisplayMacAddress();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (isDvr) {
                            loadNextDvr();
                        }
                    }
                });


            });


            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Timber.d("on error ");
                    VideoPlayActivity.this.hideProgressBar();
                    VideoPlayActivity.this.hideMacDisplayHandler();
                    VideoPlayActivity.this.stopCloseMenuHandler();
                    VideoPlayActivity.this.showDvrMenu();
                    try {
                        player.stop();
                    } catch (Exception ignored) {
                    }
                    player.reset();
                    StringBuilder sb = new StringBuilder().append("MEDIA_ERROR:\t").append("W").append(what).append("E").append(extra);
                    //Toast.makeText(VideoPlayActivity.this, "PlayBack Error:: Playing Media" + sb.toString(), Toast.LENGTH_LONG).show();
                    if (isDvr) {
                        isDvrPlaying = false;
                        Toast.makeText(VideoPlayActivity.this, "PlayBack Error:: Playing Media" + sb.toString(), Toast.LENGTH_LONG).show();
                        VideoPlayActivity.this.stopCloseMenuHandler();

                    } else {
                        VideoPlayActivity.this.setErrorFragment(null, what, extra);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
            openFragment(menuFragment);
        else {
            getSupportFragmentManager().beginTransaction().show(menuFrag).commit();


        }
    }

    private void startCloseMenuHandler() {
        hideMenuHandler.postDelayed(closeFragmentRunnable, TimeUnit.SECONDS.toMillis(10));

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
            getSupportFragmentManager().beginTransaction().hide(menuFrag).commit();
    }

    private void showMenuBg() {
        menuBackground.setVisibility(View.VISIBLE);
    }

    private void hideMenuBg() {
        menuBackground.setVisibility(View.GONE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        player.setDisplay(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        player.setDisplay(surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private void hideProgressBar() {
        progressBar.smoothToHide();
    }

    private void showProgressBar() {
        progressBar.smoothToShow();
    }

    @Override
    public void playChannelFromOnAir(ChannelItem channel, boolean onAir) {
        getSupportFragmentManager().popBackStack();
        playChannel(channel);
    }

    @Override
    public void playDvr(Epgs epgs, ChannelItem item) {
        showProgressBar();
        long utc = GetUtc.getInstance().getTimestamp().getUtc();
        Login login = GlobalVariables.login;
        String date = DateUtils.dateAndTime.format(epgs.getStartTime());
        String startTime = DateUtils._24HrsTimeFormat.format(epgs.getStartTime());
        videoPlayViewModel.getDvrLink(login.getToken(), utc, login.getId(),
                LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc), login.getSession()),
                epgs.getChannelID(), date, startTime).observe(this, new Observer<DvrLinkResponse>() {
            @Override
            public void onChanged(@Nullable DvrLinkResponse channelLinkResponse) {
                if (channelLinkResponse != null) {
                    if (channelLinkResponse.getErrorCode() > 0) {
                        Log.d("dvr", "error");
                        Toast.makeText(VideoPlayActivity.this, "Dvr couldn't be played", Toast.LENGTH_SHORT).show();
                        showDvrMenu();
                        hideProgressBar();
                    } else {
                        setUpVideoController(item, channelLinkResponse.getLink(), channelLinkResponse.getNextVideoName());
                    }
                }
            }
        });


    }

    private void setUpVideoController(ChannelItem item, String link, String nextVideoName) {
        currentDvrChannelItem = item;
        nextVideoNameDvr = nextVideoName;
        VideoPlayActivity.this.playVideo(link, true);


    }


    @Override
    public void onDvrStart() {
        playPauseStatus.setVisibility(View.GONE);
        player.start();
    }

    @Override
    public void onDvrPause() {
        playPauseStatus.setVisibility(View.VISIBLE);
        playPauseStatus.bringToFront();
        player.pause();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginDataDelete event) {
       int vav= event.getLongVa();
       Intent i = new Intent(VideoPlayActivity.this,SplashActivity.class);
       i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(i);
       finish();

    }
}
