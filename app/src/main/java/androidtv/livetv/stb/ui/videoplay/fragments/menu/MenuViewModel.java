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
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;

public class MenuViewModel extends AndroidViewModel {
    private MediatorLiveData<List<CategoryItem>> categoryItemData;
    private MediatorLiveData<List<ChannelItem>> channelItemData;
    private MenuRepository menuRepository;
    private MediatorLiveData<List<CategoriesWithChannels>> catChannelData;

    public MenuViewModel(@NonNull Application application) {
        super(application);
        menuRepository = MenuRepository.getInstance(application);
        categoryItemData = new MediatorLiveData<>();
        channelItemData = new MediatorLiveData<>();
        catChannelData=new MediatorLiveData<>();
        channelItemData.setValue(null);
        categoryItemData.setValue(null);
        catChannelData.setValue(null);
//        LiveData<List<CategoryItem>> allCategory = menuRepository.getAllCategory();
//        categoryItemData.addSource(allCategory, categoryItems -> categoryItemData.setValue(categoryItems));

        LiveData<List<CategoriesWithChannels>> categoriesWithChannels=menuRepository.getCategoriesWithChannels();
        catChannelData.addSource(categoriesWithChannels, categoriesWithChannels1 -> catChannelData.setValue(categoriesWithChannels1));


    }

    public LiveData<List<CategoryItem>> getCategoryData() {
        return categoryItemData;
    }

    public LiveData<List<CategoriesWithChannels>> getCategoriesWithChannels() {
        return catChannelData;
    }


//
//    public LiveData<List<ChannelItem>> getChannels(int id){
//        LiveData<List<ChannelItem>> channels = menuRepository.getChannels(id);
//        channelItemData.addSource(channels, channelItems -> {
//            channelItemData.setValue(channelItems);
//            channelItemData.removeSource(channels);
//        });
//        return channelItemData;
//>>>>>>> issue
//    }
}

