package androidtv.livetv.stb.ui.videoplay;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponse;

public class VideoPlayViewModel extends AndroidViewModel {

    private VideoPlayRepository videoPlayRepository;
    private MediatorLiveData<ChannelLinkResponse> channelLinkResponseMediatorLiveData;
    private MediatorLiveData<List<ChannelItem>> channelListData;

    public VideoPlayViewModel(@NonNull Application application) {
        super(application);
        videoPlayRepository = VideoPlayRepository.getInstance(application);
        channelLinkResponseMediatorLiveData = new MediatorLiveData<>();
        channelLinkResponseMediatorLiveData.setValue(null);

        channelListData = new MediatorLiveData<>();
        channelListData.setValue(null);
        LiveData<List<ChannelItem>> allChannelsData = videoPlayRepository.getAllChannels();
        channelListData.addSource(allChannelsData, channelItemList -> channelListData.setValue(channelItemList));


    }


    public LiveData<ChannelLinkResponse> getChannelLink(String token, long utc, int id, String hashCode, String macAddress,int channelId) {
        LiveData<ChannelLinkResponse> fetchChannels = videoPlayRepository.getChannelLink(token, utc, String.valueOf(id), String.valueOf(hashCode),macAddress, String.valueOf(channelId));
        channelLinkResponseMediatorLiveData.addSource(fetchChannels, channelLinkResponse -> channelLinkResponseMediatorLiveData.setValue(channelLinkResponse));
        return channelLinkResponseMediatorLiveData;
    }

    public LiveData<List<ChannelItem>> getAllChannels() {
        return channelListData;
    }


}
