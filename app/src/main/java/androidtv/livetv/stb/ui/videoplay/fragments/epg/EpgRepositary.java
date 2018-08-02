package androidtv.livetv.stb.ui.videoplay.fragments.epg;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

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
import androidtv.livetv.stb.entity.EpgItem;
import androidtv.livetv.stb.entity.EpgResponse;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.MenuRepository;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.DateUtils;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
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

    public EpgRepositary(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        epgApiInterface = retrofitInstance.create(EpgApiInterface.class);
        catChannelDao = db.catChannelDao();
        channelList = new MediatorLiveData<>();
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

    public LiveData<List<Epgs>> getEpgs(String token, long utc, String userId, String hashValue, String channelId) {
        MediatorLiveData<List<Epgs>> responseMediatorLiveData = new MediatorLiveData<>();
        responseMediatorLiveData.setValue(null);
        List<Epgs> epgsList = new ArrayList<>();
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
                            if(response != null) {

                                List<Epgs> epgs = getEpgsListFrom(response.getEpg(), channelId);
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

    private List<Epgs> getEpgsListFrom(List<EpgItem> epgItemList, String channelId) {
        List<Epgs> epgsList = new ArrayList<>();
        if(epgItemList != null){
        for (EpgItem epgItem : epgItemList) {
            Epgs epgs = new Epgs();
            epgs.setId(String.valueOf(channelId) + epgItem.getStartTime());
            epgs.setDate(getParsedDate(epgItem.getStartTime()));
            epgs.setStartTime(getParsedDate(epgItem.getStartTime()));
            epgs.setEndTime(getParsedDate(epgItem.getEndTime()));
            epgs.setProgramTitle(epgItem.getProgramName());
            epgs.setChannelID(Integer.parseInt(channelId));
            epgs.setTimeZone(getTimeZone(epgItem.getStartTime()));
            epgsList.add(epgs);
         }
        }

        return epgsList;
    }


    private String getTimeZone(String startTime) {
        if(startTime.contains(" +")) {
            String temp[] = startTime.split(" +");
            return temp[1];
        }else
            return "";

    }


    private Date getParsedDate(String startTime) {
        Date d = null;
        try {
            d = DateUtils.convertStringToDate(startTime);
            System.out.print("date = "+d.toString());

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        return d;
    }



}
