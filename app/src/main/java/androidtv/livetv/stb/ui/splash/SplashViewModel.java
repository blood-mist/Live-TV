package androidtv.livetv.stb.ui.splash;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CatChannelWrapper;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.entity.LoginResponseWrapper;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckWrapper;
import androidtv.livetv.stb.entity.VersionResponseWrapper;

public class SplashViewModel extends AndroidViewModel {


    private SplashRepository splashRepository;

    private MediatorLiveData<MacInfo> mObservableMacInfo;

    private MediatorLiveData<GeoAccessInfo> geoAccessInfoLiveData;
    private MediatorLiveData<LoginResponseWrapper> fileLoginData;
    private LiveData<Login> loginLiveData;

    private LiveData<Integer> loginTableSizeData;

    private LiveData<Integer> channelTableSizeData;
    private LiveData<List<ChannelItem>> channelLiveData;

    private MediatorLiveData<VersionResponseWrapper> appInfoLiveData;

    private MediatorLiveData<UserCheckWrapper> userCheckLiveData;
    private MediatorLiveData<CatChannelWrapper> catChannelData;
    private MediatorLiveData<Integer> tableCountData;
    private MediatorLiveData<List<ChannelItem>> channelListData;


    private MediatorLiveData<Login> userCredentialData;
    private MediatorLiveData<Integer> chSizeMediatorData;

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
        fileLoginData = new MediatorLiveData<>();
        chSizeMediatorData = new MediatorLiveData<>();
        channelListData = new MediatorLiveData<>();
        chSizeMediatorData.setValue(null);
        tableCountData.setValue(null);
        mObservableMacInfo.setValue(null);
        geoAccessInfoLiveData.setValue(null);
        appInfoLiveData.setValue(null);
        userCheckLiveData.setValue(null);
        userCredentialData.setValue(null);
        fileLoginData.setValue(null);
        catChannelData.setValue(null);
        channelListData.setValue(null);
        loginLiveData = splashRepository.getData();
        loginTableSizeData = splashRepository.getRowCount();
        channelTableSizeData = splashRepository.getChannelCount();
        channelLiveData = splashRepository.getChannelList();


    }


    public LiveData<MacInfo> checkIfValidMacAddress(String macAddress) {
        LiveData<MacInfo> getMacInfo = splashRepository.isMacRegistered(macAddress);
        mObservableMacInfo.addSource(getMacInfo, mObservableMacInfo::setValue);
        return mObservableMacInfo;

    }


    public LiveData<GeoAccessInfo> checkIfGeoAccessEnabled() {
        LiveData<GeoAccessInfo> getGeoAccessStatus = splashRepository.isGeoAccessEnabled();
        geoAccessInfoLiveData.addSource(getGeoAccessStatus, geoAccessInfoLiveData::setValue);
        return geoAccessInfoLiveData;
    }

    public LiveData<VersionResponseWrapper> checkVersion(String macAddress, int versionCode, String versionName, String applicationId) {
        LiveData<VersionResponseWrapper> getAppDetails = splashRepository.isNewVersionAvailable(macAddress, versionCode, versionName, applicationId);
        appInfoLiveData.addSource(getAppDetails, versionResponseWrapper -> appInfoLiveData.setValue(versionResponseWrapper));
        return appInfoLiveData;

    }


    public LiveData<UserCheckWrapper> checkIfUserRegistered(String macAddress) {
        LiveData<UserCheckWrapper> checkUserActivation = splashRepository.isUserRegistered(macAddress);
        userCheckLiveData.addSource(checkUserActivation, userCheckWrapper -> {
            if (userCheckWrapper != null) {
                userCheckLiveData.setValue(userCheckWrapper);
            }
        });
        return userCheckLiveData;
    }


    public LiveData<Login> checkDatainDb() {
        userCredentialData.addSource(loginLiveData, login -> {
            if (login != null)
                userCredentialData.setValue(login);
        });
        return userCredentialData;
    }

    public LiveData<CatChannelWrapper> fetchChannelDetails(String token, String utc, String userId, String hashValue) {
        LiveData<CatChannelWrapper> catChannelInfoLiveData = splashRepository.getChannels(token, utc, userId, hashValue);
        catChannelData.addSource(catChannelInfoLiveData, catChannelWrapper -> catChannelData.setValue(catChannelWrapper));
        return catChannelData;

    }

    public LiveData<Integer> checkIfDataExists() {
        tableCountData.addSource(loginTableSizeData, integer -> {
            if (integer != null) {
                tableCountData.setValue(integer);
            }
        });
        return tableCountData;


    }

    public LiveData<Integer> checkChannelsInDB() {
        chSizeMediatorData.addSource(channelTableSizeData, integer -> {
            if(integer!=null) {
                chSizeMediatorData.setValue(integer);
                chSizeMediatorData.removeSource(channelTableSizeData);
            }
        });
        return chSizeMediatorData;
    }

    public void insertCatChannelToDB(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        splashRepository.insertCatChannelToDB(categoryList, channelList);
    }

    public LiveData<List<ChannelItem>> getAllChannelsInDBToCompare() {
        channelListData.addSource(channelLiveData, channelItemList -> {
            if (channelItemList != null) {
                channelListData.removeSource(channelLiveData);
                channelListData.setValue(channelItemList);

            }
        });
        return channelListData;
    }


    public LiveData<LoginResponseWrapper> loginFromFile(String userEmail, String userPassword, String macAddress) {
        LiveData<LoginResponseWrapper> loginFromFileData = splashRepository.getLoginResponse(userEmail, userPassword, macAddress);
        fileLoginData.addSource(loginFromFileData, fileLoginData::setValue);
        return fileLoginData;

    }

    public void deleteloginData() {
        splashRepository.deleteLoginFromDB();
    }

    public void deleteLoginFile() {
        splashRepository.deleteLoginFile();
    }
}
