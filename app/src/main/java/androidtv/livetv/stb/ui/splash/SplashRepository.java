package androidtv.livetv.stb.ui.splash;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.Nullable;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckInfo;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.login.LoginDao;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.ApiInterface;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

import static androidtv.livetv.stb.utils.LinkConfig.NO_CONNECTION;

public class SplashRepository {
    private static SplashRepository sInstance;
    private MediatorLiveData<MacInfo> macInfoMediatorLiveData;
    private MediatorLiveData<GeoAccessInfo> geoAccessLiveData;
    private MediatorLiveData<List<AppVersionInfo>> appInfoLiveData;
    private ApiInterface apiInterface;
    private MediatorLiveData<UserCheckInfo> userCheckLiveData;
    private MediatorLiveData<Login> userCredentialData;
    private MediatorLiveData<Integer> rowCountData;
    private MediatorLiveData<Integer> channelCountData;
    private MediatorLiveData<CatChannelInfo> catChannelData;
    private LoginDao mLoginDao;
    private CatChannelDao catChannelDao;
    private MediatorLiveData<List<ChannelItem>> channelListData;


    SplashRepository(Application application) {
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        Retrofit retrofitInstance = ApiManager.getAdapter();
        mLoginDao = db.loginDao();
        catChannelDao = db.catChannelDao();
        userCredentialData = new MediatorLiveData<>();
        userCredentialData.setValue(null);
        apiInterface = retrofitInstance.create(ApiInterface.class);
        rowCountData = new MediatorLiveData<>();
        rowCountData.setValue(null);
        rowCountData.addSource(mLoginDao.getTableSize(), integer -> {
            if (integer != null) {
                rowCountData.removeSource(mLoginDao.getTableSize());
                rowCountData.postValue(integer);
            }


        });
        channelCountData = new MediatorLiveData<>();
        channelCountData.setValue(null);
        channelCountData.addSource(catChannelDao.getChannelTableSize(), integer -> {
            if (integer != null) {
                channelCountData.removeSource(catChannelDao.getChannelTableSize());
                channelCountData.postValue(integer);
            }
        });

        channelListData = new MediatorLiveData<>();
        channelListData.setValue(null);
        channelListData.addSource(catChannelDao.getChannels(), channelItemList -> {
            if (channelItemList != null) {
                channelListData.removeSource(catChannelDao.getChannels());
                channelListData.postValue(channelItemList);
            }
        });

    }

    public static SplashRepository getInstance(final Application application) {
        if (sInstance == null) {
            synchronized (SplashRepository.class) {
                if (sInstance == null) {
                    sInstance = new SplashRepository(application);
                }
            }
        }
        return sInstance;
    }

    public LiveData<MacInfo> isMacRegistered(String macAddress) {
        macInfoMediatorLiveData = new MediatorLiveData<>();
        macInfoMediatorLiveData.setValue(null);
        Observable<Response<MacInfo>> macObserver = apiInterface.checkMacValidation(macAddress);
        macObserver.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<MacInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<MacInfo> macInfoResponse) {
                        if (macInfoResponse.code() == 200) {
                            MacInfo macInfo = macInfoResponse.body();
                            macInfo.setResponseCode(macInfoResponse.code());
                            macInfoMediatorLiveData.postValue(macInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MacInfo macInfo = new MacInfo();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            macInfo.setResponseCode(NO_CONNECTION);
                        } else {
                            macInfo.setResponseCode(0);
                        }
                        macInfo.setMacExists("");
                        macInfo.setMessage(e.getMessage());
                        macInfoMediatorLiveData.postValue(macInfo);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return macInfoMediatorLiveData;

    }

    public LiveData<GeoAccessInfo> isGeoAccessEnabled() {
        geoAccessLiveData = new MediatorLiveData<>();
        geoAccessLiveData.setValue(null);
        Observable<Response<GeoAccessInfo>> geoAccess = apiInterface.checkGeoAccess();
        geoAccess.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<GeoAccessInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<GeoAccessInfo> geoAccessInfoResponse) {
                        if (geoAccessInfoResponse.code() == 200) {
                            GeoAccessInfo geoAccessInfo = geoAccessInfoResponse.body();
                            geoAccessInfo.setResponseCode(geoAccessInfoResponse.code());

                            geoAccessLiveData.postValue(geoAccessInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        GeoAccessInfo geoAccessInfo = new GeoAccessInfo();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            geoAccessInfo.setResponseCode(NO_CONNECTION);
                        } else {
                            geoAccessInfo.setResponseCode(0);
                        }
                        geoAccessInfo.getAllow().setAllow("false");
                        geoAccessLiveData.postValue(geoAccessInfo);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return geoAccessLiveData;
    }


    public LiveData<List<AppVersionInfo>> isNewVersionAvailable(String macAddress, int versionCode, String versionName, String applicationId) {
        appInfoLiveData = new MediatorLiveData<>();
        appInfoLiveData.setValue(null);
        List<AppVersionInfo> appVersionInfoList = new ArrayList<>();
        Observable<Response<List<AppVersionInfo>>> checkVersion = apiInterface.checkForAppVersion(macAddress, versionCode, versionName, applicationId);
        checkVersion.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<List<AppVersionInfo>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<List<AppVersionInfo>> versionInfoResponse) {
                        if (versionInfoResponse.code() == 200) {
                            appVersionInfoList.addAll(versionInfoResponse.body());
                            appInfoLiveData.postValue(appVersionInfoList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        AppVersionInfo versionInfo = new AppVersionInfo();
                        versionInfo.setErrorCode("ERR_001");
                        versionInfo.setErrorMessage(e.getLocalizedMessage());
                        appVersionInfoList.add(versionInfo);
                        appInfoLiveData.postValue(appVersionInfoList);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return appInfoLiveData;
    }

    public LiveData<UserCheckInfo> isUserRegistered(String macAddress) {
        userCheckLiveData = new MediatorLiveData<>();
        userCheckLiveData.setValue(null);
        Observable<Response<UserCheckInfo>> userCheckObserver = apiInterface.checkUserStatus(macAddress);
        userCheckObserver.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<UserCheckInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<UserCheckInfo> userCheckInfoResponse) {
                        if (userCheckInfoResponse.code() == 200) {
                            //setExtra codes here for error handeling

                            userCheckLiveData.postValue(userCheckInfoResponse.body());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //set Extra Code for error Handeling
                        UserCheckInfo userCheckInfo = new UserCheckInfo();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            userCheckInfo.setResponseCode(NO_CONNECTION);
                        } else {
                            userCheckInfo.setResponseCode(0);
                        }
                        userCheckInfo.getData().setActivationStatus(-1);
                        userCheckLiveData.postValue(userCheckInfo);


                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return userCheckLiveData;

    }

    public LiveData<Login> getData() {
        userCredentialData.addSource(mLoginDao.getLoginData(), login -> {
            if(login!=null) {
                userCredentialData.removeSource(mLoginDao.getLoginData());
                userCredentialData.postValue(login);
            }

        });
        return userCredentialData;
    }

    public LiveData<CatChannelInfo> getChannels(String token, String utc, String userId, String hashValue) {
        catChannelData = new MediatorLiveData<>();
        catChannelData.setValue(null);
        Observable<Response<CatChannelInfo>> catChannel = apiInterface.getCatChannel(token, Long.parseLong(utc), userId, hashValue);
        catChannel.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<CatChannelInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<CatChannelInfo> catChannelInfoResponse) {
                        if (catChannelInfoResponse.code() == 200) {
                            CatChannelInfo catChannelInfo = catChannelInfoResponse.body();
                            catChannelData.postValue(catChannelInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        CatChannelInfo catChannelInfo = new CatChannelInfo();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            catChannelData.addSource(catChannelDao.getCategories(), categoryItems -> {
                                catChannelInfo.setCategory(categoryItems);
                                catChannelData.postValue(catChannelInfo);
                                catChannelData.removeSource(catChannelDao.getCategories());
                            });
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return catChannelData;

    }

    private void insertChannelsintoDatabase(List<ChannelItem> channel) {
        Completable.fromRunnable(() -> catChannelDao.insertChannels(channel)).subscribeOn(Schedulers.io()).subscribe().dispose();
    }

    private void insertCategoryIntoDatabase(List<CategoryItem> category) {
        Completable.fromRunnable(() -> catChannelDao.insertCategory(category)).subscribeOn(Schedulers.io()).subscribe().dispose();
    }

    public LiveData<Integer> getRowCount() {
        return rowCountData;

    }

    public LiveData<Integer> getChannelCount() {
        return channelCountData;
    }

    public void insertCatChannelToDB(CatChannelInfo catChannelInfo) {
        insertCategoryIntoDatabase(catChannelInfo.getCategory());
        insertChannelsintoDatabase(catChannelInfo.getChannel());
    }

    public LiveData<List<ChannelItem>> getChannelList() {
        return channelListData;
    }

    public void insertChannelsToDB(List<ChannelItem> channels) {
        insertChannelsintoDatabase(channels);
    }
}
