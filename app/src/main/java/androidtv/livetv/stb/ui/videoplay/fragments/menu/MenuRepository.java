package androidtv.livetv.stb.ui.videoplay.fragments.menu;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;



import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.FavoriteResponse;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.utils.ApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MenuRepository {
    private static MenuRepository mInstance;
    private ApiInterface videoPlayApiInterface;
    private MediatorLiveData<List<CategoryItem>> categoryData;
    private MediatorLiveData<List<CategoriesWithChannels>> catChannelData;
    private CatChannelDao catChannelDao;
    private MediatorLiveData<ChannelItem> lastChannelData;

    public MenuRepository(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        catChannelDao = db.catChannelDao();
        videoPlayApiInterface = retrofitInstance.create(ApiInterface.class);
        categoryData = new MediatorLiveData<>();
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

    public LiveData<ChannelLinkResponse> getPreviewLink(String token, long utc, String userId, String hash, String channelId) {
        MediatorLiveData<ChannelLinkResponse> responseMediatorLiveData = new MediatorLiveData<>();
        responseMediatorLiveData.setValue(null);
       Observable<Response<ChannelLinkResponse>> call = videoPlayApiInterface.getPreviewLink(token,utc,userId,hash,channelId);
        call.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<Response<ChannelLinkResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ChannelLinkResponse> channelLinkResponseResponse) {
                        if (channelLinkResponseResponse.code() == 200) {
                            ChannelLinkResponse response = channelLinkResponseResponse.body();
                            responseMediatorLiveData.postValue(response);
                        } else {
                            responseMediatorLiveData.postValue(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        responseMediatorLiveData.postValue(null);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return responseMediatorLiveData;
    }
    public LiveData<ChannelItem> getLastPlayedChannel(int channel_id) {
        lastChannelData = new MediatorLiveData<>();
        lastChannelData.addSource(catChannelDao.getLastPlayedChannel(channel_id), channelItem -> {
            if (channelItem != null) {
                lastChannelData.removeSource(catChannelDao.getLastPlayedChannel(channel_id));
                lastChannelData.postValue(channelItem);
            }
        });
        return lastChannelData;

    }

    public void addChannelToFav(int favStatus,int channel_id) {
        Completable.fromRunnable(() -> catChannelDao.updateFav(favStatus,channel_id)).subscribeOn(Schedulers.io()).subscribe().dispose();
    }
}
