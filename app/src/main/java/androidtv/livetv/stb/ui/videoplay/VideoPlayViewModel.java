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
import androidtv.livetv.stb.entity.ChannelLinkResponseWrapper;
import androidtv.livetv.stb.entity.DvrLinkResponse;

public class VideoPlayViewModel extends AndroidViewModel {

    private VideoPlayRepository videoPlayRepository;
    private MediatorLiveData<List<ChannelItem>> channelListData;

    public VideoPlayViewModel(@NonNull Application application) {
        super(application);
        videoPlayRepository = VideoPlayRepository.getInstance(application);
        channelListData = new MediatorLiveData<>();
        channelListData.setValue(null);
        LiveData<List<ChannelItem>> allChannelsData = videoPlayRepository.getAllChannels();
        channelListData.addSource(allChannelsData, channelItemList -> channelListData.setValue(channelItemList));


    }


    public LiveData<ChannelLinkResponseWrapper> getChannelLink(String token, long utc, int id, String hashCode, String macAddress, int channelId) {
       return videoPlayRepository.getChannelLink(token, utc, String.valueOf(id), String.valueOf(hashCode),macAddress, String.valueOf(channelId));
    }

    public LiveData<DvrLinkResponse> getDvrLink(String token, long utc, int id, String hashCode, int channelId, String date, String startTime) {
       return videoPlayRepository.getDvrLink(token, utc, String.valueOf(id), String.valueOf(hashCode), String.valueOf(channelId),date,startTime);
    }

    public LiveData<DvrLinkResponse> getNextDvrLink(String token, long utc, int id, String hashCode, int channelId, String nextprogram) {
        return videoPlayRepository.getNextDvrLink(token, utc, String.valueOf(id), String.valueOf(hashCode), String.valueOf(channelId),nextprogram);
    }

    public LiveData<List<ChannelItem>> getAllChannels() {
        return channelListData;
    }

    public void nukeLoginTable(){
        videoPlayRepository.deleteLoginLable();
    }


}
