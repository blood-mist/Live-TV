package androidtv.livetv.stb.ui.videoplay;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.Random;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DeviceUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoPlayActivity extends AppCompatActivity {

    @BindView(R.id.videoSurface)SurfaceView videoSurface;
    @BindView(R.id.videoView) VideoView videoView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        ButterKnife.bind(this);
        txtRandomDisplayBoxId.setText( AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        videoPlayViewModel = ViewModelProviders.of(this).get(VideoPlayViewModel.class);


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
}
