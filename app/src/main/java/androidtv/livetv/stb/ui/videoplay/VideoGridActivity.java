package androidtv.livetv.stb.ui.videoplay;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import androidtv.livetv.stb.R;

public class VideoGridActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        View decorView = getWindow().getDecorView();
    }
}
