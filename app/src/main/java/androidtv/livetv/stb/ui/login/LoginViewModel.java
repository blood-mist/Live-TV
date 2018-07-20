package androidtv.livetv.stb.ui.login;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginInfo;

public class LoginViewModel extends AndroidViewModel {
    private  LoginRepository loginRepository;
    private MediatorLiveData<LoginInfo> loginLiveData;
    private MediatorLiveData<Login> loginData;
    public LoginViewModel(@NonNull Application application) {
        super(application);
        loginRepository=LoginRepository.getInstance(application);
        loginLiveData=new MediatorLiveData<>();
        loginData=new MediatorLiveData<>();
        loginData.setValue(null);
        loginLiveData.setValue(null);

    }
    public LiveData<LoginInfo> performLogin(String userEmail,String userPassword,String macAddress) {
        loginLiveData.addSource(getLoginResponse (userEmail,userPassword,macAddress), loginLiveData::setValue);
        return loginLiveData;

    }

    private LiveData<LoginInfo> getLoginResponse(String userEmail, String userPassword, String macAddress) {
       return loginRepository.signIn(userEmail,userPassword,macAddress);
    }

    public LiveData<Login> getLoginInfoFromDB() {
        loginData.addSource(loginRepository.getData(), login -> loginData.setValue(login));
        return  loginData;
    }


}
