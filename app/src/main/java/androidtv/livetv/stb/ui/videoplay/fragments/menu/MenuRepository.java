package androidtv.livetv.stb.ui.videoplay.fragments.menu;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.videoplay.VideoPlayApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import retrofit2.Retrofit;

public class MenuRepository {
    private static MenuRepository mInstance;
    private VideoPlayApiInterface videoPlayApiInterface;
    private MediatorLiveData<List<CategoryItem>> categoryData;
    private MediatorLiveData<List<ChannelItem>> channelList;
    private MediatorLiveData<List<CategoriesWithChannels>> catChannelData;
    private CatChannelDao catChannelDao;

    public MenuRepository(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        catChannelDao = db.catChannelDao();
        videoPlayApiInterface = retrofitInstance.create(VideoPlayApiInterface.class);
        categoryData = new MediatorLiveData<>();
        channelList = new MediatorLiveData<>();
        catChannelData=new MediatorLiveData<>();
        catChannelData.addSource(catChannelDao.getCategoriesWithChannels(), categoriesWithChannels -> catChannelData.postValue(categoriesWithChannels));
        categoryData.addSource(catChannelDao.getCategories(), categoryItems -> categoryData.postValue(categoryItems));


    }

    public static MenuRepository getInstance(final Application application) {
        if (mInstance == null) {
            synchronized (MenuRepository.class) {
                if (mInstance == null) {
                    mInstance = new MenuRepository(application);
                }
            }
        }
        return mInstance;
    }

    public LiveData<List<CategoryItem>> getAllCategory() {
        return categoryData;
    }




    public LiveData<List<CategoriesWithChannels>>getCategoriesWithChannels() {
        return catChannelData;
    }
//    public LiveData<List<ChannelItem>> getChannels(int id) {
//        if (id == -1) {
//            channelList.addSource(catChannelDao.getChannels(), channelItems -> channelList.postValue(channelItems));
//
//        } else {
//            channelList.addSource(catChannelDao.getChannels(id), channelItems -> channelList.postValue(channelItems));
//
//        }
//        return channelList;
//>>>>>>> issue
//    }
}
