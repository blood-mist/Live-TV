package androidtv.livetv.stb.ui.login;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CatChannelWrapper;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.entity.LoginResponseWrapper;

public class LoginViewModel extends AndroidViewModel {
    private  LoginRepository loginRepository;
    private MediatorLiveData<Login> loginData;
    private LiveData<Login>loginDBData;
    private MediatorLiveData<Integer> chSizeMediatorData;
    private LiveData<Integer> channelTableSizeData;
    private MediatorLiveData<List<ChannelItem>> channelListData;
    private LiveData<List<ChannelItem>> channelLiveData;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        loginRepository=LoginRepository.getInstance(application);
        loginData=new MediatorLiveData<>();
        chSizeMediatorData = new MediatorLiveData<>();
        loginData.setValue(null);
        chSizeMediatorData.setValue(null);
        loginDBData =loginRepository.getData();
        channelTableSizeData = loginRepository.getChannelCount();
        channelListData = new MediatorLiveData<>();
        channelListData.setValue(null);
        channelLiveData = loginRepository.getChannelList();
        loginData.addSource(loginDBData, login -> loginData.setValue(login));
        chSizeMediatorData.addSource(channelTableSizeData, integer -> {
            if (integer != null) {
                chSizeMediatorData.setValue(integer);
            }
        });
        channelListData.addSource(channelLiveData, channelItemList -> {
            if (channelItemList != null) {
                channelListData.setValue(channelItemList);

            }
        });



    }
    public LiveData<LoginResponseWrapper> performLogin(String userEmail, String userPassword, String macAddress) {
       return loginRepository.signIn(userEmail,userPassword,macAddress);

    }

    public LiveData<Login> getLoginInfoFromDB() {
        return  loginData;
    }
    public LiveData<CatChannelWrapper> fetchChannelDetails(String token, String utc, String userId, String hashValue) {
       return loginRepository.getChannels(token, utc, userId, hashValue);
    }

    public LiveData<Integer> checkChannelsInDB() {
        return chSizeMediatorData;
    }
    public LiveData<List<ChannelItem>> getAllChannelsInDBToCompare() {
        return channelListData;
    }
    public void insertCatChannelToDB(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        loginRepository.insertCatChannelToDB(categoryList, channelList);
    }
}
