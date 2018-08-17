package androidtv.livetv.stb.ui.videoplay.fragments.epg;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.EpgEntity;
import androidtv.livetv.stb.entity.EpgItem;
import androidtv.livetv.stb.entity.EpgResponse;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.MenuRepository;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.DataUtils;
import androidtv.livetv.stb.utils.DateUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EpgRepositary {
    private static EpgRepositary mInstance;
    private final EpgApiInterface epgApiInterface;
    private MediatorLiveData<List<ChannelItem>> channelList;
    private MediatorLiveData<List<Epgs>> epLiveData;
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

    public LiveData<EpgEntity> getEpgs(String token, long utc, String userId, String hashValue, String channelId) {
        MediatorLiveData<EpgEntity> responseMediatorLiveData = new MediatorLiveData<>();
        responseMediatorLiveData.postValue(null);
         List<Epgs> epgsList = new ArrayList<>();
        responseMediatorLiveData.addSource(catChannelDao.getEpgs(Integer.parseInt(channelId)), new Observer<List<Epgs>>() {
            @Override
            public void onChanged(@Nullable List<Epgs> epgs) {
                EpgEntity epgEntity = new EpgEntity();
                if (epgs.size() > 0) {
                    epgEntity.setEpgsList(epgs);

                } else {
                    io.reactivex.Observable<Response<EpgResponse>> call = epgApiInterface.getEpgs(channelId, token, utc, userId, hashValue);
                    call.subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.newThread()).
                            unsubscribeOn(Schedulers.io()).
                            subscribe(new io.reactivex.Observer<Response<EpgResponse>>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Response<EpgResponse> epgResponseResponse) {
                                    if (epgResponseResponse.code() == 200) {
                                        EpgResponse response = epgResponseResponse.body();
                                        if (response != null) {
                                            EpgEntity epgEntity = new EpgEntity();
                                            if (response.getError_code() <= 0) {
                                                List<Epgs> epgs = DataUtils.getEpgsListFrom(response.getEpg(), channelId);
                                                if (epgs != null && epgs.size() > 0) {
                                                    insertToDb(epgs);
                                                }

                                            }
                                        }
                                    }


                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onComplete() {
                                }
                            });

                }
                responseMediatorLiveData.postValue(epgEntity);
            }
        });









        return responseMediatorLiveData;

    }

    private void insertToDb(List<Epgs> epgs) {
        Completable.fromRunnable(() -> catChannelDao.insertEpgs(epgs)).subscribeOn(Schedulers.io()).subscribe();
    }


    public LiveData<List<Epgs>> getAllEpgs() {
        return liveDateEpgs;
    }


}
