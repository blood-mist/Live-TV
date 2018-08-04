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
import androidtv.livetv.stb.entity.DvrLinkResponse;

public class VideoPlayViewModel extends AndroidViewModel {

    private VideoPlayRepository videoPlayRepository;
    private MediatorLiveData<ChannelLinkResponse> channelLinkResponseMediatorLiveData;
    private MediatorLiveData<DvrLinkResponse> dvrLinkResponseMediatorLiveData;
    private MediatorLiveData<List<ChannelItem>> channelListData;

    public VideoPlayViewModel(@NonNull Application application) {
        super(application);
        videoPlayRepository = VideoPlayRepository.getInstance(application);
        channelLinkResponseMediatorLiveData = new MediatorLiveData<>();
        dvrLinkResponseMediatorLiveData = new MediatorLiveData<>();
        channelLinkResponseMediatorLiveData.setValue(null);

        channelListData = new MediatorLiveData<>();
        channelListData.setValue(null);
        channelListData.addSource(videoPlayRepository.getAllChannels(), channelItemList -> channelListData.setValue(channelItemList));


    }


    public LiveData<ChannelLinkResponse> getChannelLink(String token, long utc, int id, String hashCode, int channelId) {
        channelLinkResponseMediatorLiveData.addSource(videoPlayRepository.getChannelLink(token, utc, String.valueOf(id), String.valueOf(hashCode), String.valueOf(channelId)),
                channelLinkResponse -> channelLinkResponseMediatorLiveData.setValue(channelLinkResponse));
        return channelLinkResponseMediatorLiveData;
    }

    public LiveData<DvrLinkResponse> getDvrLink(String token, long utc, int id, String hashCode, int channelId, String date, String startTime) {
        dvrLinkResponseMediatorLiveData.addSource(videoPlayRepository.getDvrLink(token, utc, String.valueOf(id), String.valueOf(hashCode), String.valueOf(channelId),date,startTime),
                new Observer<DvrLinkResponse>() {
                    @Override
                    public void onChanged(@Nullable DvrLinkResponse channelLinkResponse) {
                        dvrLinkResponseMediatorLiveData.setValue(channelLinkResponse);
                    }
                });
        return dvrLinkResponseMediatorLiveData;
    }

    public LiveData<List<ChannelItem>> getAllChannels() {
        return channelListData;
    }


}
