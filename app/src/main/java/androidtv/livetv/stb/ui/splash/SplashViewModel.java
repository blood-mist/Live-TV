package androidtv.livetv.stb.ui.splash;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import androidtv.livetv.stb.entity.CatChannelWrapper;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginResponseWrapper;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckWrapper;
import androidtv.livetv.stb.entity.VersionResponseWrapper;

public class SplashViewModel extends AndroidViewModel {


    private SplashRepository splashRepository;
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
    private MediatorLiveData<List<Epgs>> liveAllEpgs;

    public SplashViewModel(@NonNull Application application) {
        super(application);
        splashRepository = SplashRepository.getInstance(application);
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
        tableCountData.addSource(loginTableSizeData, integer -> {
            if (integer != null) {
                tableCountData.setValue(integer);
            }
        });
        userCredentialData.addSource(loginLiveData, login -> {
            if (login != null)
                userCredentialData.setValue(login);
        });
        chSizeMediatorData.addSource(channelTableSizeData, integer -> {
            if (integer != null) {
                chSizeMediatorData.setValue(integer);
                chSizeMediatorData.removeSource(channelTableSizeData);
            }
        });

        channelListData.addSource(channelLiveData, channelItemList -> {
            if (channelItemList != null) {
                channelListData.removeSource(channelLiveData);
                channelListData.setValue(channelItemList);
            }
        });
        liveAllEpgs = new MediatorLiveData<>();
        liveAllEpgs.setValue(null);
        liveAllEpgs.addSource(splashRepository.getAllEpg(), epgs -> liveAllEpgs.setValue(epgs));

    }


    public LiveData<MacInfo> checkIfValidMacAddress(String macAddress) {
        return splashRepository.isMacRegistered(macAddress);

    }


    public LiveData<GeoAccessInfo> checkIfGeoAccessEnabled() {
        return splashRepository.isGeoAccessEnabled();
    }

    public LiveData<VersionResponseWrapper> checkVersion(String macAddress, int versionCode, String versionName, String applicationId) {
        return splashRepository.isNewVersionAvailable(macAddress, versionCode, versionName, applicationId);

    }


    public LiveData<UserCheckWrapper> checkIfUserRegistered(String macAddress) {
        return splashRepository.isUserRegistered(macAddress);
    }


    public LiveData<Login> checkDatainDb() {
        return userCredentialData;
    }

    public LiveData<CatChannelWrapper> fetchChannelDetails(String token, String utc, String userId, String hashValue) {
        return splashRepository.getChannels(token, utc, userId, hashValue);


    }

    public LiveData<Integer> checkIfDataExists() {
        return tableCountData;


    }

    public LiveData<Integer> checkChannelsInDB() {
        return chSizeMediatorData;
    }

    public void insertCatChannelToDB(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        splashRepository.insertCatChannelToDB(categoryList, channelList);
    }

    public LiveData<List<ChannelItem>> getAllChannelsInDBToCompare() {
        return channelListData;
    }


    public LiveData<LoginResponseWrapper> loginFromFile(String userEmail, String userPassword, String macAddress) {
        return splashRepository.getLoginResponse(userEmail, userPassword, macAddress);

    }

    public void deleteloginData() {
        splashRepository.deleteLoginFromDB();
    }

    public void deleteLoginFile() {
        splashRepository.deleteLoginFile();
    }


    public LiveData<List<Epgs>> getAllEpgs() {
        return liveAllEpgs;

    }

    public void deleteEpg(String id) {
        splashRepository.deleteEpg(id);
    }
}
