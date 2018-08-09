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
import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.ChannelLinkResponseWrapper;
import androidtv.livetv.stb.entity.DvrLinkResponse;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.splash.SplashRepository;
import androidtv.livetv.stb.utils.ApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VideoPlayRepository {

    private static VideoPlayRepository mInstance;
    private ApiInterface videoPlayApiInterface;
    private MediatorLiveData<List<ChannelItem>> channelList;
    private CatChannelDao catChannelDao;


    public VideoPlayRepository(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        videoPlayApiInterface = retrofitInstance.create(ApiInterface.class);
        catChannelDao = db.catChannelDao();
        channelList = new MediatorLiveData<>();
        channelList.addSource(catChannelDao.getChannels(), channelItemList -> {
            channelList.removeSource(catChannelDao.getChannels());
            channelList.postValue(channelItemList);
        });


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

    public LiveData<List<ChannelItem>> getAllChannels() {
        return channelList;
    }

    public LiveData<ChannelLinkResponseWrapper> getChannelLink(String token, long utc, String userId, String hashValue, String macAddress, String channelId) {
        MediatorLiveData<ChannelLinkResponseWrapper> responseMediatorLiveData = new MediatorLiveData<>();
        responseMediatorLiveData.setValue(null);
        io.reactivex.Observable<Response<ChannelLinkResponse>> call = videoPlayApiInterface.getChannelLink(token, utc, userId, hashValue,macAddress, channelId);
        call.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<Response<ChannelLinkResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ChannelLinkResponse> channelLinkResponseResponse) {
                        ChannelLinkResponseWrapper wrapper = new ChannelLinkResponseWrapper();
                        if (channelLinkResponseResponse.code() == 200) {
                            ChannelLinkResponse response = channelLinkResponseResponse.body();
                             if(response.getChannel() != null){
                                wrapper.setChannelLinkResponse(response);
                             }
                        } else {
                            wrapper.setException(new Exception());
                        }

                        responseMediatorLiveData.postValue(wrapper);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ChannelLinkResponseWrapper wrapper = new ChannelLinkResponseWrapper();
                        wrapper.setException((Exception) e);
                        responseMediatorLiveData.postValue(wrapper);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return responseMediatorLiveData;
    }

    public LiveData<DvrLinkResponse> getDvrLink(String token, long utc, String userId, String hashValue, String channelId, String date, String startTime) {
        MediatorLiveData<DvrLinkResponse> responseMediatorLiveData = new MediatorLiveData<>();
        responseMediatorLiveData.setValue(null);
        io.reactivex.Observable<Response<DvrLinkResponse>> call = videoPlayApiInterface.getDvrLink(token, utc, userId, hashValue, channelId,date,startTime);
        call.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<Response<DvrLinkResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<DvrLinkResponse> channelLinkResponseResponse) {
                        if (channelLinkResponseResponse.code() == 200) {
                            DvrLinkResponse response = channelLinkResponseResponse.body();
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


    public LiveData<DvrLinkResponse> getNextDvrLink(String token, long utc, String userId, String hashValue, String channelId, String nextProgram) {
        MediatorLiveData<DvrLinkResponse> responseMediatorLiveData = new MediatorLiveData<>();
        responseMediatorLiveData.setValue(null);
        io.reactivex.Observable<Response<DvrLinkResponse>> call = videoPlayApiInterface.getNextDvrLink(token, utc, userId, hashValue, channelId,nextProgram);
        call.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<Response<DvrLinkResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<DvrLinkResponse> channelLinkResponseResponse) {
                        if (channelLinkResponseResponse.code() == 200) {
                            DvrLinkResponse response = channelLinkResponseResponse.body();
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




}
