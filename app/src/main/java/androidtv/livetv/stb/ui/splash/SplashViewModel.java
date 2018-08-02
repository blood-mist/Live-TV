package androidtv.livetv.stb.ui.splash;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckInfo;

public class SplashViewModel extends AndroidViewModel {


    private SplashRepository splashRepository;

    private MediatorLiveData<MacInfo> mObservableMacInfo;

    private MediatorLiveData<GeoAccessInfo> geoAccessInfoLiveData;
    private LiveData<Login> loginLiveData;

    private LiveData<Integer> loginTableSizeData;

    private LiveData<Integer> channelTableSizeData;

    private MediatorLiveData<List<AppVersionInfo>> appInfoLiveData;

    private MediatorLiveData<UserCheckInfo> userCheckLiveData;
    private MediatorLiveData<CatChannelInfo> catChannelData;
    private MediatorLiveData<Integer> tableCountData;
    private LiveData<List<ChannelItem>> channelListData;


    private MediatorLiveData<Login> userCredentialData;

    public SplashViewModel(@NonNull Application application) {
        super(application);
        splashRepository = SplashRepository.getInstance(application);
        mObservableMacInfo = new MediatorLiveData<>();
        geoAccessInfoLiveData = new MediatorLiveData<>();
        appInfoLiveData = new MediatorLiveData<>();
        userCheckLiveData = new MediatorLiveData<>();
        userCredentialData = new MediatorLiveData<>();
        catChannelData = new MediatorLiveData<>();
        tableCountData = new MediatorLiveData<>();
        tableCountData.setValue(null);
        mObservableMacInfo.setValue(null);
        geoAccessInfoLiveData.setValue(null);
        appInfoLiveData.setValue(null);
        userCheckLiveData.setValue(null);
        userCredentialData.setValue(null);
        catChannelData.setValue(null);
        loginLiveData = splashRepository.getData();
        loginTableSizeData =splashRepository.getRowCount();
        channelTableSizeData=splashRepository.getChannelCount();
        channelListData=splashRepository.getChannelList();


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
        return splashRepository.isGeoAccessEnabled();
    }

    public LiveData<List<AppVersionInfo>> checkVersion(String macAddress, int versionCode, String versionName, String applicationId) {
        appInfoLiveData.addSource(getAppDetails(macAddress, versionCode, versionName, applicationId), appInfoLiveData::setValue);
        return appInfoLiveData;

    }

    private LiveData<List<AppVersionInfo>> getAppDetails(String macAddress, int versionCode, String versionName, String applicationId) {
        return splashRepository.isNewVersionAvailable(macAddress, versionCode, versionName, applicationId);
    }

    public LiveData<UserCheckInfo> checkIfUserRegistered(String macAddress) {
        userCheckLiveData.addSource(checkUserActivation(macAddress), userCheckLiveData::setValue);
        return userCheckLiveData;
    }

    private LiveData<UserCheckInfo> checkUserActivation(String macAddress) {
        return splashRepository.isUserRegistered(macAddress);
    }


    public LiveData<Login> checkDatainDb() {
        userCredentialData.addSource(loginLiveData, login -> {
            if(login!=null)
            userCredentialData.setValue(login);
        });
        return userCredentialData;
    }

    public LiveData<CatChannelInfo> fetchChannelDetails(String token, String utc, String userId, String hashValue) {
        catChannelData.addSource(splashRepository.getChannels(token, utc, userId, hashValue), catChannelInfo -> catChannelData.setValue(catChannelInfo));
        return catChannelData;

    }

    public LiveData<Integer> checkIfDataExists() {
        tableCountData.addSource(loginTableSizeData, integer -> {
            if(integer!=null) {
                tableCountData.setValue(integer);
            }
        });
        return tableCountData;


    }

    public LiveData<Integer> checkChannelsInDB() {
        return channelTableSizeData;
    }

    public void insertCatChannelToDB(CatChannelInfo catChannelInfo) {
        splashRepository.insertCatChannelToDB(catChannelInfo);
    }

    public LiveData<List<ChannelItem>> getAllChannelsInDBToCompare() {
        return channelListData;
    }

    public void insertChannelToDB(List<ChannelItem> channels) {
        splashRepository.insertChannelsToDB(channels);
    }
}
