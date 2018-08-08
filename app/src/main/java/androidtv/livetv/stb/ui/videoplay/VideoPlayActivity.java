package androidtv.livetv.stb.ui.videoplay;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.FragmentMenu;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;

public class VideoPlayActivity extends AppCompatActivity implements FragmentMenu.FragmentMenuInteraction, SurfaceHolder.Callback {

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
    private FragmentMenu menuFragment;
    private Fragment currentFragment;
    private List<CategoryItem> mCategoryList = new ArrayList<>();
    private List<ChannelItem> mChannelList = new ArrayList<>();
    private MediaPlayer player;
    private ChannelChangeObserver channelChangeObservable;

    private Handler hideMenuHandler;

    private SharedPreferences lastPlayedPrefs;

    private SharedPreferences.Editor editor;
    private int selectedChannelId;
    String macAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        lastPlayedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);
        hideMenuHandler = new Handler();
        macAddress = AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this);
        txtRandomDisplayBoxId.setText(macAddress);
        menuFragment = new FragmentMenu();
        channelChangeObservable = new ChannelChangeObserver();
        channelChangeObservable.addObserver(menuFragment);
        initSurafaceView();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    public void openErrorFragment() {
        Toast.makeText(this, "Last Playing Channel error ", Toast.LENGTH_LONG).show();
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
                showMenu();
                break;
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                changeToPreviousChannel();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (menuFrag == null || menuFrag.isHidden()) {
                    changeToPreviousChannel();
                    return true;
                } else {
                    return false;
                }

            case KeyEvent.KEYCODE_CHANNEL_UP:
                changeToNextChannel();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (menuFrag == null || menuFrag.isHidden()) {
                    changeToNextChannel();
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
            if (menuFrag.isHidden())
                getSupportFragmentManager().beginTransaction().show(menuFrag).commit();
            else
                getSupportFragmentManager().beginTransaction().hide(menuFrag).commit();


        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopCloseMenuHandler();
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
            params.setMargins(500, random.nextInt(500),
                    random.nextInt(200), random.nextInt(200));

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
        Timber.d("Played Channel:" + item.getName());
        //TODO play Channels
        showProgressBar();

        long utc = GetUtc.getInstance().getTimestamp().getUtc();
        Login login = GlobalVariables.login;
        videoPlayViewModel.getChannelLink(login.getToken(), utc, login.getId(),
                LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc),
                        login.getSession()), macAddress, item.getId()).observe(this, channelLinkResponse -> {
            if (channelLinkResponse != null) {
                playVideo(channelLinkResponse.getChannel().getLink());
            }
        });
        saveCurrentInPrefs(item);


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
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayViewModel = ViewModelProviders.of(this).get(VideoPlayViewModel.class);
        initSurafaceView();
        showMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            player.stop();
            player.reset();
            player.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        hideMacDisplayHandler();
        super.onDestroy();
    }

    private void playVideo(String channelLink) {
        Log.d("media", channelLink);
        player.reset();
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
                startCloseMenuHandler();
                randomDisplayMacAddress();


            });


            player.setOnErrorListener((mp, what, extra) -> {
                Timber.d("on error ");
                hideProgressBar();
                showMenuBg();
                hideMacDisplayHandler();
                try {
                    player.stop();
                } catch (Exception ignored) {
                }
                player.reset();
                StringBuilder sb = new StringBuilder().append("MEDIA_ERROR:\t").append("W").append(what).append("E").append(extra);
                Toast.makeText(VideoPlayActivity.this, "LoginError Playing Media" + sb.toString(), Toast.LENGTH_LONG).show();
                showMenu();
                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
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
}
