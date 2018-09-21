package androidtv.livetv.stb.ui.videoplay.fragments.menu;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
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
    private LiveData<List<CategoriesWithChannels>> catChannelData;
    private LiveData<List<ChannelItem>> liveFavItems;


    public MenuViewModel(@NonNull Application application) {
        super(application);
        menuRepository = MenuRepository.getInstance(application);
    }

    public LiveData<List<CategoriesWithChannels>> getCategoriesWithChannels() {
        catChannelData= null;
        catChannelData = menuRepository.getCategoriesWithChannels();
        return catChannelData;
    }

    public LiveData<ChannelLinkResponse> getPreviewLink(String token, long utc, String userId, String hashCode, int channelId) {
        return menuRepository.getPreviewLink(token, utc, String.valueOf(userId), String.valueOf(hashCode), String.valueOf(channelId));
    }

    public LiveData<ChannelItem> getLastPlayedChannel(int channel_id) {
        return menuRepository.getLastPlayedChannel(channel_id);
    }

    public void addChannelToFavorite(int favStatus, ChannelItem channel) {
        menuRepository.addChannelToFav(favStatus, channel);
    }

    public LiveData<List<ChannelItem>> getFavChannels() {
        liveFavItems =null;
        liveFavItems= menuRepository.getFavChannels();
        return liveFavItems;
    }

    public LiveData<ChannelItem> getFirstChannel() {
        return menuRepository.getFirstChannelFromDB();
    }

    public LiveData<List<ChannelItem>> getAllChannel() {
        return  menuRepository.getAlLChannels();
    }
}

