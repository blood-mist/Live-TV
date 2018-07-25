package androidtv.livetv.stb.ui.videoplay;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.FragmentMenu;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoPlayActivity extends AppCompatActivity implements FragmentMenu.FragmentMenuInteraction {

    @BindView(R.id.videoSurface)SurfaceView videoSurface;
    //@BindView(R.id.videoView) VideoView videoView;
    @BindView(R.id.video_frame) RelativeLayout videoFrame;
    @BindView(R.id.img_play_pause) ImageView playPauseStatus;
    @BindView(R.id.videoStatus) ImageView recordedStatus;
    @BindView(R.id.progressBar1) AVLoadingIndicatorView progressBar;
    @BindView(R.id.tv_setchannelfrompriority) TextView txtChangeChannelFromPriority;
    @BindView(R.id.tvboxID) TextView txtRandomDisplayBoxId;
    @BindView(R.id.mainImage) ImageView mainImage;
    @BindView(R.id.transparentLoadingBackground) LinearLayout backgroundTransparent;
    @BindView(R.id.container_movie_player) FrameLayout errorFrame;
    @BindView(R.id.scrollViewImage) ImageView scrollImg;
    @BindView(R.id.videoSurfaceContainer) FrameLayout videoSurfaceContainer;

    private VideoPlayViewModel videoPlayViewModel;
    private Handler handlerToShowMac;
    private Handler handlerToHideMac;
    private Runnable runnableToHideMac;
    private Runnable runnableToShowMac;
    private FragmentMenu menuFragment;
    private Fragment currentFragment;
    private List<CategoryItem> mCategoryList = new ArrayList<>();
    private List<ChannelItem> mChannelList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        ButterKnife.bind(this);
        txtRandomDisplayBoxId.setText( AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this));

        menuFragment = new FragmentMenu();

    }

    @Override
    protected void onStart() {
        super.onStart();
        videoPlayViewModel = ViewModelProviders.of(this).get(VideoPlayViewModel.class);
        openFragment(menuFragment);
    }

    private void openFragment(Fragment fragment) {
        Log.d("frag","called from activity");
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.container_movie_player,currentFragment).commit();



    }

    private void openFragmentWithBackStack(Fragment fragment,String tag) {
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.container_movie_player,fragment).addToBackStack(tag).commit();

    }


    private void randomDisplayMacAddress() {
        final Random random = new Random();
        handlerToShowMac = new Handler();
        handlerToHideMac = new Handler();


        final FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) txtRandomDisplayBoxId.getLayoutParams();

        runnableToHideMac = new Runnable() {

            @Override
            public void run() {
                params.setMargins(500, random.nextInt(500),
                        random.nextInt(200), random.nextInt(200));
                txtRandomDisplayBoxId.setLayoutParams(params);
                txtRandomDisplayBoxId.setVisibility(View.INVISIBLE);
                System.out.println("box is invisible");

                handlerToShowMac.postDelayed(runnableToShowMac, 5 * 1000 * 10);
            }
        };
        runnableToShowMac = new Runnable() {

            @Override
            public void run() {
                params.setMargins(500, random.nextInt(500),
                        random.nextInt(200), random.nextInt(200));

                txtRandomDisplayBoxId.bringToFront();
                txtRandomDisplayBoxId.setLayoutParams(params);
                txtRandomDisplayBoxId.setVisibility(View.VISIBLE);
                System.out.println("box is shown");

                handlerToHideMac.postDelayed(runnableToHideMac, 1000 * 7);
            }
        };

        handlerToShowMac.postDelayed(runnableToShowMac, 7 * 1000 * 1);

    }


    @Override
    public void playChannel(ChannelItem item) {
        //TODO play Channels

      long utc = GetUtc.getInstance().getTimestamp().getUtc();
      Login login = GlobalVariables.login;
      videoPlayViewModel.getChannelLink(login.getToken(),utc,login.getId(),
              LinkConfig.getHashCode(String.valueOf(login.getId()),String.valueOf(utc),login.getSession()),
              item.getId()).observe(this, new Observer<ChannelLinkResponse>() {
          @Override
          public void onChanged(@Nullable ChannelLinkResponse channelLinkResponse) {
              if(channelLinkResponse != null){
                  playVideo(channelLinkResponse.getChannel().getLink());
              }
          }
      });


    }

    private void playVideo(String channel) {
        Log.d("media",channel);
        MediaPlayer player = new  MediaPlayer();

    }
}
