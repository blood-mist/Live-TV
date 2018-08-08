package androidtv.livetv.stb.ui.videoplay.fragments.dvr;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.DvrStartDateTimeEntity;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgRepositary;

public class DvrViewModel extends AndroidViewModel {

    private final DvrRepositary dvrRepositary;
    private MediatorLiveData<List<ChannelItem>> channelListMediator;
    private MediatorLiveData<List<Epgs>> epLiveData;
    private MediatorLiveData<DvrStartDateTimeEntity> dvrStartDateTimeEntityMediatorLiveData;

    public DvrViewModel(@NonNull Application application) {
        super(application);
        dvrRepositary = DvrRepositary.getInstance(application);
        channelListMediator = new MediatorLiveData<>();
        epLiveData = new MediatorLiveData<>();
        dvrStartDateTimeEntityMediatorLiveData = new MediatorLiveData<>();
        channelListMediator.addSource(dvrRepositary.getAllChannels(), channelItems -> channelListMediator.postValue(channelItems));

    }

    public LiveData<List<ChannelItem>> getChannels() {
        return channelListMediator;
    }

    public LiveData<List<Epgs>> getEpgs(String token, long utc, String userId, String hashValue, String channelId) {
        epLiveData.addSource(dvrRepositary.getEpgs(token, utc, userId, hashValue, channelId), new Observer<List<Epgs>>() {
            @Override
            public void onChanged(@Nullable List<Epgs> epgs) {
                epLiveData.postValue(epgs);
            }
        });

        return epLiveData;
    }

    public LiveData<DvrStartDateTimeEntity> getStartTime(String token, long utc, String userId, String hashValue, int hasDvr, String channelId){
        dvrStartDateTimeEntityMediatorLiveData.addSource(dvrRepositary.getStartTime(token, utc, userId, hashValue, hasDvr, channelId), new Observer<DvrStartDateTimeEntity>() {
            @Override
            public void onChanged(@Nullable DvrStartDateTimeEntity dvrStartDateTimeEntity) {
                dvrStartDateTimeEntityMediatorLiveData.postValue(dvrStartDateTimeEntity);
            }
        });

        return dvrStartDateTimeEntityMediatorLiveData;
    }

}
