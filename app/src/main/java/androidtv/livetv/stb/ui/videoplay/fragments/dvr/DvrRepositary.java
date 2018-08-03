package androidtv.livetv.stb.ui.videoplay.fragments.dvr;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.DvrStartDateTimeEntity;
import androidtv.livetv.stb.entity.EpgResponse;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.MenuRepository;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.DataUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DvrRepositary {
    private static DvrRepositary mInstance;
    private final DvrApiInterface dvrApiInterface;
    private final CatChannelDao catChannelDao;
    private final MediatorLiveData<List<ChannelItem>> channelList;
    private final MediatorLiveData<DvrStartDateTimeEntity> dvrStartDateTimeEntityMediatorLiveData;

    public DvrRepositary(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        dvrApiInterface =  retrofitInstance.create(DvrApiInterface.class);
        catChannelDao = db.catChannelDao();
        channelList = new MediatorLiveData<>();
        dvrStartDateTimeEntityMediatorLiveData = new MediatorLiveData<>();
        channelList.addSource(catChannelDao.getChannels(), channelItems -> channelList.postValue(channelItems));



    }


    public static DvrRepositary getInstance(final Application application) {
        if (mInstance == null) {
            synchronized (MenuRepository.class) {
                if (mInstance == null) {
                    mInstance = new DvrRepositary(application);
                }
            }
        }
        return mInstance;
    }

    public LiveData<List<ChannelItem>> getAllChannels() {
        return channelList;
    }

    public LiveData<List<Epgs>> getEpgs(String token, long utc, String userId, String hashValue, String channelId) {
        MediatorLiveData<List<Epgs>> responseMediatorLiveData = new MediatorLiveData<>();
        responseMediatorLiveData.setValue(null);
        List<Epgs> epgsList = new ArrayList<>();
        io.reactivex.Observable<Response<EpgResponse>> call = dvrApiInterface.getEpgs(channelId, token, utc, userId, hashValue);
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
                            if(response != null) {

                                List<Epgs> epgs = DataUtils.getEpgsListFrom(response.getEpg(), channelId);
                                if (epgs.size() > 0) {
                                    insertToDb(epgs);
                                }
                                responseMediatorLiveData.postValue(epgs);
                            }

                        } else {
                            responseMediatorLiveData.postValue(null);
                        }


                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            responseMediatorLiveData.addSource(catChannelDao.getEpgs(Integer.parseInt(channelId)), new Observer<List<Epgs>>() {
                                @Override
                                public void onChanged(@Nullable List<Epgs> epgs) {
                                    responseMediatorLiveData.postValue(epgs);
                                }
                            });
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        return responseMediatorLiveData;

    }

    private void insertToDb(List<Epgs> epgs) {
        Completable.fromRunnable(() -> catChannelDao.insertEpgs(epgs)).subscribeOn(Schedulers.io()).subscribe();
    }

    public LiveData<DvrStartDateTimeEntity> getStartTime(String token, long utc, String userId, String hashValue, int hasDvr, String channelId){
        MediatorLiveData<DvrStartDateTimeEntity> dvrStartDate = new MediatorLiveData<>();
        Observable<Response<DvrStartDateTimeEntity>> call = dvrApiInterface.getStartTime(token,utc,userId,hashValue,hasDvr,Integer.parseInt(channelId));
        call.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread()).
                unsubscribeOn(Schedulers.io()).
                subscribe(new io.reactivex.Observer<Response<DvrStartDateTimeEntity>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<DvrStartDateTimeEntity> dvrStartDateTimeEntityResponse) {
                       if(dvrStartDateTimeEntityResponse.code() == 200){
                           dvrStartDate.postValue(dvrStartDateTimeEntityResponse.body());
                       }
                    }

                    @Override
                    public void onError(Throwable e) {
                      dvrStartDate.postValue(null);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return dvrStartDate;
    }


}
