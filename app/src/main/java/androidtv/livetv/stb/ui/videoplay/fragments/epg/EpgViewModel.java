package androidtv.livetv.stb.ui.videoplay.fragments.epg;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.EpgEntity;
import androidtv.livetv.stb.entity.Epgs;

public class EpgViewModel extends AndroidViewModel {

    private EpgRepositary epgRepositary;
    private MediatorLiveData<List<ChannelItem>> channelListMediator;
    private MediatorLiveData<EpgEntity> epLiveData;

    public EpgViewModel(@NonNull Application application) {
        super(application);
        epgRepositary = EpgRepositary.getInstance(application);
        channelListMediator = new MediatorLiveData<>();
        epLiveData = new MediatorLiveData<>();
        channelListMediator.addSource(epgRepositary.getAllChannels(), channelItems -> channelListMediator.postValue(channelItems));
    }


    public LiveData<List<ChannelItem>> getChannels(){
        return channelListMediator;
    }

    public LiveData<EpgEntity> getEpgs(String token, long utc, String userId , String hashValue, String channelId){
        epLiveData.addSource(epgRepositary.getEpgs(token, utc, userId, hashValue, channelId), new Observer<EpgEntity>() {
            @Override
            public void onChanged(@Nullable EpgEntity epgs) {
                epLiveData.postValue(epgs);
            }
        });

        return epLiveData;
    }


}
