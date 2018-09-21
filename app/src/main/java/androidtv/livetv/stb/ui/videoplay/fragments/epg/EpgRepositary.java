package androidtv.livetv.stb.ui.videoplay.fragments.epg;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.EpgEntity;
import androidtv.livetv.stb.entity.EpgResponse;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.MenuRepository;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.DataUtils;
import androidtv.livetv.stb.utils.DisposableManager;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EpgRepositary {
    private static EpgRepositary mInstance;
    private final EpgApiInterface epgApiInterface;
    private MediatorLiveData<List<ChannelItem>> channelList;
    private MediatorLiveData<Boolean> epLiveData;
    private CatChannelDao catChannelDao;
    private MediatorLiveData<List<Epgs>> liveDateEpgs;

    public EpgRepositary(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        epgApiInterface = retrofitInstance.create(EpgApiInterface.class);
        catChannelDao = db.catChannelDao();
        channelList = new MediatorLiveData<>();
        liveDateEpgs = new MediatorLiveData<>();
        channelList.addSource(catChannelDao.getChannels(), channelItems -> channelList.postValue(channelItems));

    }


    public static EpgRepositary getInstance(final Application application) {
        if (mInstance == null) {
            synchronized (MenuRepository.class) {
                if (mInstance == null) {
                    mInstance = new EpgRepositary(application);
                }
            }
        }
        return mInstance;
    }

    public LiveData<List<ChannelItem>> getAllChannels() {
        return channelList;
    }

    public LiveData<Boolean> getEpgs(String token, long utc, String userId, String hashValue, String channelId) {
        epLiveData=new MediatorLiveData<>();
        epLiveData.setValue(null);
        io.reactivex.Observable<Response<EpgResponse>> call = epgApiInterface.getEpgs(channelId, token, utc, userId, hashValue);
        call.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread()).
                unsubscribeOn(Schedulers.io()).
                subscribe(new io.reactivex.Observer<Response<EpgResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        DisposableManager.addEpgDisposable(d);
                    }

                    @Override
                    public void onNext(Response<EpgResponse> epgResponseResponse) {
                        if (epgResponseResponse.code() == 200) {
                            EpgResponse response = epgResponseResponse.body();
                            if (response != null) {
                                if (response.getError_code() <= 0) {
                                    List<Epgs> epgs = DataUtils.getEpgsListFrom(response.getEpg(), channelId);
                                    if (epgs != null && epgs.size() > 0) {
                                        insertToDb(epgs).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onComplete() {
                                                epLiveData.postValue(true);
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                epLiveData.postValue(true);
                                            }
                                        });
                                    }

                                }else{
                                    epLiveData.postValue(true);
                                }
                            }
                        }


                    }

                    @Override
                    public void onError(Throwable e) {
                        epLiveData.postValue(true);
                    }

                    @Override
                    public void onComplete() {
                    }
                });


        return epLiveData;

    }

    private Completable insertToDb(List<Epgs> epgs) {
      Completable insertionTaskObservable= Completable.fromRunnable(() -> catChannelDao.insertEpgs(epgs));
      return insertionTaskObservable;
    }


    public LiveData<List<Epgs>> getAllEpgs() {
        return liveDateEpgs;
    }


    public LiveData<List<Epgs>> getEpgOfChannel(int channel_id) {
        return  catChannelDao.getEpgs(channel_id);
    }
}
