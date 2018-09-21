package androidtv.livetv.stb.ui.videoplay.fragments.epg;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.EpgEntity;
import androidtv.livetv.stb.entity.Epgs;

public class EpgViewModel extends AndroidViewModel {

    public EpgRepositary getEpgRepositary() {
        return epgRepositary;
    }

    private EpgRepositary epgRepositary;
    private MediatorLiveData<List<ChannelItem>> channelListMediator;
    private MediatorLiveData<List<Epgs>> liveAllEpgs;


    public EpgViewModel(@NonNull Application application) {
        super(application);
        epgRepositary = EpgRepositary.getInstance(application);
        channelListMediator = new MediatorLiveData<>();
        liveAllEpgs = new MediatorLiveData<>();
        liveAllEpgs.addSource(epgRepositary.getAllEpgs(), epgs -> liveAllEpgs.postValue(epgs));
        channelListMediator.addSource(epgRepositary.getAllChannels(), channelItems -> channelListMediator.postValue(channelItems));
    }


    public LiveData<List<ChannelItem>> getChannels(){
        return channelListMediator;
    }

    public LiveData<Boolean> getEpgs(String token, long utc, String userId , String hashValue, String channelId){
        return epgRepositary.getEpgs(token, utc, userId, hashValue, channelId);

    }


    public LiveData<List<Epgs>> getEpgFromDB(int channel_id) {
        return epgRepositary.getEpgOfChannel(channel_id);
    }
}
