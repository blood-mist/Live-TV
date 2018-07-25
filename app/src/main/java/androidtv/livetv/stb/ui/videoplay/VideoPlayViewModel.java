package androidtv.livetv.stb.ui.videoplay;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;

public class VideoPlayViewModel extends AndroidViewModel {

    private VideoPlayRepository videoPlayRepository;

    public VideoPlayViewModel(@NonNull Application application) {
        super(application);
        videoPlayRepository = VideoPlayRepository.getInstance(application);

       }


}
