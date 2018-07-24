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
    private MediatorLiveData<List<CategoryItem>> catChannelData;
    private MediatorLiveData<List<ChannelItem>> channelList;

    public VideoPlayViewModel(@NonNull Application application) {
        super(application);
        videoPlayRepository = VideoPlayRepository.getInstance(application);
        catChannelData = new MediatorLiveData<>();
        channelList = new MediatorLiveData<>();
        channelList.setValue(null);
        catChannelData.setValue(null);


    }

    public MediatorLiveData<List<ChannelItem>> getChannelList(int id) {
        channelList.addSource(videoPlayRepository.getChannelList(id), channelItems -> channelList.postValue(channelItems));
        return channelList;
    }

    public MediatorLiveData<List<CategoryItem>> getCatChannelData() {
        catChannelData.addSource(videoPlayRepository.getCatChannelData(), categoryItems -> catChannelData.postValue(categoryItems));
        return catChannelData;
    }
}
