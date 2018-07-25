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


    public VideoPlayRepository(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        videoPlayApiInterface = retrofitInstance.create(VideoPlayApiInterface.class);



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





}
