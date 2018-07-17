package androidtv.livetv.stb.ui.splash;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckInfo;
import androidtv.livetv.stb.utils.ApiManager;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SplashRepository{
    private static SplashRepository sInstance;
    private MediatorLiveData<MacInfo> macInfoMediatorLiveData;
    private MediatorLiveData<GeoAccessInfo>geoAccessLiveData;
    private MediatorLiveData<List<AppVersionInfo>>appInfoLiveData;
    private SplashApiInterface splashApiInterface;
     private MediatorLiveData<UserCheckInfo> userCheckLiveData;



    SplashRepository(Application application) {
        Retrofit retrofitInstance = ApiManager.getAdapter();
       splashApiInterface  = retrofitInstance.create(SplashApiInterface.class);

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
        macInfoMediatorLiveData=new MediatorLiveData<>();
        macInfoMediatorLiveData.setValue(null);
        Observable<Response<MacInfo>> macObserver=splashApiInterface.checkMacValidation(macAddress);
        macObserver.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<MacInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<MacInfo> macInfoResponse) {
                        if(macInfoResponse.code()==200) {
                            MacInfo macInfo = macInfoResponse.body();
                            macInfo.setResponseCode(String.valueOf(macInfoResponse.code()));

                            macInfoMediatorLiveData.postValue(macInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MacInfo macInfo = new MacInfo();
                        macInfo.setResponseCode("selferror");
                        macInfo.setMacExists("false");
                        macInfoMediatorLiveData.postValue(macInfo);



                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return macInfoMediatorLiveData;

    }

    public LiveData<GeoAccessInfo> isGeoAccessEnabled() {
        geoAccessLiveData=new MediatorLiveData<>();
        geoAccessLiveData.setValue(null);
        Observable<Response<GeoAccessInfo>> geoAccess=splashApiInterface.checkGeoAccess();
        geoAccess.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<GeoAccessInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<GeoAccessInfo> geoAccessInfoResponse) {
                        if(geoAccessInfoResponse.code()==200) {
                            GeoAccessInfo geoAccessInfo = geoAccessInfoResponse.body();
                            geoAccessInfo.setResponseCode(String.valueOf(geoAccessInfoResponse.code()));

                            geoAccessLiveData.postValue(geoAccessInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        GeoAccessInfo geoAccessInfo = new GeoAccessInfo();
                        geoAccessInfo.setResponseCode("selferror");
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
        appInfoLiveData=new MediatorLiveData<>();
        appInfoLiveData.setValue(null);
        List<AppVersionInfo> appVersionInfoList=new ArrayList<>();
        Observable<Response<List<AppVersionInfo>>> checkVersion=splashApiInterface.checkForAppVersion(macAddress,versionCode,versionName,applicationId);
        checkVersion.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<List<AppVersionInfo>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<List<AppVersionInfo>> versionInfoResponse) {
                        if(versionInfoResponse.code()==200) {
                            appVersionInfoList.addAll(versionInfoResponse.body());
                            appInfoLiveData.postValue(appVersionInfoList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                       AppVersionInfo versionInfo=new AppVersionInfo();
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
        userCheckLiveData=new MediatorLiveData<>();
        userCheckLiveData.setValue(null);
        Observable<Response<UserCheckInfo>> userCheckObserver=splashApiInterface.checkUserStatus(macAddress);
        userCheckObserver.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<UserCheckInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<UserCheckInfo> userCheckInfoResponse) {
                        if(userCheckInfoResponse.code()==200) {
                           //setExtra codes here for error handeling

                            userCheckLiveData.postValue(userCheckInfoResponse.body());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //set Extra Code for error Handeling
                        UserCheckInfo userCheckInfo=new UserCheckInfo();
                        userCheckInfo.getData().setActivationStatus(-1);
                        userCheckLiveData.postValue(userCheckInfo);



                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return userCheckLiveData;

    }
}
