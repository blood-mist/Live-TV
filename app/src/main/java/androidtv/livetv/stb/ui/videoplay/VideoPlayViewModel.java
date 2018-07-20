package androidtv.livetv.stb.ui.videoplay;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class VideoPlayViewModel extends AndroidViewModel {

    private VideoPlayRepository videoPlayRepository;

    public VideoPlayViewModel(@NonNull Application application) {
        super(application);
        videoPlayRepository = VideoPlayRepository.getInstance(application);
    }


}
