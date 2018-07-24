package androidtv.livetv.stb.ui.videoplay;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.splash.SplashRepository;
import androidtv.livetv.stb.utils.ApiManager;
import retrofit2.Retrofit;

public class VideoPlayRepository {

    private static VideoPlayRepository mInstance;
    private VideoPlayApiInterface videoPlayApiInterface;
    private MediatorLiveData<List<CategoryItem>> catChannelData;
    private MediatorLiveData<List<ChannelItem>> channelList;
    private CatChannelDao catChannelDao;

    public VideoPlayRepository(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        catChannelDao = db.catChannelDao();
        videoPlayApiInterface = retrofitInstance.create(VideoPlayApiInterface.class);
        catChannelData = new MediatorLiveData<>();
        channelList = new MediatorLiveData<>();


    }

    public static VideoPlayRepository getInstance(final Application application) {
        if (mInstance == null) {
            synchronized (SplashRepository.class) {
                if (mInstance == null) {
                    mInstance = new VideoPlayRepository(application);
                }
            }
        }
        return mInstance;
    }

    public MediatorLiveData<List<ChannelItem>> getChannelList(int id){
        Observer<List<ChannelItem>> observer = channelItems -> channelList.setValue(channelItems);
        if(id == -1){
            channelList.addSource(catChannelDao.getChannels(), observer);
        }else{
            channelList.addSource(catChannelDao.getChannels(id),observer);
        }
       return channelList;
    }

    public MediatorLiveData<List<CategoryItem>> getCatChannelData(){
        catChannelData.addSource(catChannelDao.getCategories(), categoryItems -> catChannelData.setValue(categoryItems));
        return catChannelData;
    }

}
