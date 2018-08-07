package androidtv.livetv.stb.ui.videoplay.fragments.menu;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.FavoriteResponse;

public class MenuViewModel extends AndroidViewModel {
    private MenuRepository menuRepository;
    private MediatorLiveData<List<CategoriesWithChannels>> catChannelData;
    private MediatorLiveData<ChannelLinkResponse> previewData;
    private MediatorLiveData<ChannelItem> lastPlayedData;
    private MediatorLiveData<FavoriteResponse> favoriteData;


    public MenuViewModel(@NonNull Application application) {
        super(application);
        menuRepository = MenuRepository.getInstance(application);
        catChannelData=new MediatorLiveData<>();
        previewData=new MediatorLiveData<>();
        previewData.setValue(null);
        catChannelData.setValue(null);
        LiveData<List<CategoriesWithChannels>> categoriesWithChannelsData=menuRepository.getCategoriesWithChannels();
        catChannelData.addSource(categoriesWithChannelsData, categoriesWithChannels -> catChannelData.setValue(categoriesWithChannels));
        lastPlayedData = new MediatorLiveData<>();
        lastPlayedData.setValue(null);

        favoriteData=new MediatorLiveData<>();
        favoriteData.setValue(null);

    }

    public LiveData<List<CategoriesWithChannels>> getCategoriesWithChannels() {
        return catChannelData;
    }

    public LiveData<ChannelLinkResponse> getPreviewLink(String token,long utc,String userId,String hashCode,int channelId) {
        previewData.addSource(menuRepository.getPreviewLink(token, utc, String.valueOf(userId), String.valueOf(hashCode), String.valueOf(channelId)),
                channelLinkResponse -> previewData.setValue(channelLinkResponse));
        return previewData;
    }
    public LiveData<ChannelItem> getLastPlayedChannel(int channel_id) {
        lastPlayedData.addSource(menuRepository.getLastPlayedChannel(channel_id), channelItem -> lastPlayedData.setValue(channelItem));
        return lastPlayedData;
    }

    public void addChannelToFavorite( int favStatus,int channel_id) {
        menuRepository.addChannelToFav(favStatus,channel_id);
    }
}

