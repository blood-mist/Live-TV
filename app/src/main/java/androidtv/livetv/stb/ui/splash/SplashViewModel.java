package androidtv.livetv.stb.ui.splash;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckInfo;

public class SplashViewModel extends AndroidViewModel {


    private SplashRepository splashRepository;

    private  MediatorLiveData<MacInfo> mObservableMacInfo;

    private MediatorLiveData<GeoAccessInfo> geoAccessInfoLiveData;

    private MediatorLiveData<List<AppVersionInfo>> appInfoLiveData;

    private MediatorLiveData<UserCheckInfo> userCheckLiveData;
    public SplashViewModel(@NonNull Application application) {
        super(application);
        splashRepository = SplashRepository.getInstance(application);
        mObservableMacInfo = new MediatorLiveData<>();
        geoAccessInfoLiveData =new MediatorLiveData<>();
        appInfoLiveData=new MediatorLiveData<>();
        userCheckLiveData=new MediatorLiveData<>();
        mObservableMacInfo.setValue(null);
        geoAccessInfoLiveData.setValue(null);
        appInfoLiveData.setValue(null);
        userCheckLiveData.setValue(null);


    }

    private LiveData<MacInfo> getMacInfo(String macAddress) {

        return splashRepository.isMacRegistered(macAddress);
    }

    public LiveData<MacInfo> checkIfValidMacAddress(String macAddress) {
        mObservableMacInfo.addSource(getMacInfo(macAddress), mObservableMacInfo::setValue);
        return mObservableMacInfo;

    }


    public LiveData<GeoAccessInfo> checkIfGeoAccessEnabled() {
        geoAccessInfoLiveData.addSource(getGeoAccessStatus(), geoAccessInfoLiveData::setValue);
        return geoAccessInfoLiveData;
    }

    private LiveData<GeoAccessInfo> getGeoAccessStatus() {
        return  splashRepository.isGeoAccessEnabled();
    }

    public LiveData<List<AppVersionInfo>> checkVersion(String macAddress, int versionCode, String versionName, String applicationId) {
        appInfoLiveData.addSource(getAppDetails(macAddress,versionCode,versionName,applicationId),appInfoLiveData::setValue);
        return  appInfoLiveData;

    }

    private LiveData<List<AppVersionInfo>> getAppDetails(String macAddress, int versionCode, String versionName, String applicationId) {
       return splashRepository.isNewVersionAvailable(macAddress,versionCode,versionName,applicationId);
    }

    public LiveData<UserCheckInfo> checkIfUserRegistered(String macAddress) {
        userCheckLiveData.addSource(checkUserActivation(macAddress),userCheckLiveData::setValue);
        return userCheckLiveData;
    }

    private LiveData<UserCheckInfo> checkUserActivation(String macAddress) {
        return splashRepository.isUserRegistered(macAddress);
    }

    private void  insertData(MacInfo macInfo){

    }

    private class MySyn extends AsyncTask<Object,Object,Object >{
        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }
    }
}
