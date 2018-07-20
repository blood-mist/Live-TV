package androidtv.livetv.stb.ui.login;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.ui.splash.SplashApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginRepository {
    private static LoginRepository loginInstance;
    private SplashApiInterface loginApiInterface;
    private MediatorLiveData<LoginInfo> loginInfoLiveData;
    private  MediatorLiveData<Login> loginData;
    private LoginDao mLoginDao;

    private LoginRepository(Application application) {
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        mLoginDao = db.loginDao();
        Retrofit retrofitInstance = ApiManager.getAdapter();
        loginApiInterface  = retrofitInstance.create(SplashApiInterface.class);
        loginData=new MediatorLiveData<>();

    }
    public static LoginRepository getInstance(final Application application) {
        if (loginInstance == null) {
            synchronized (LoginRepository.class) {
                if (loginInstance == null) {
                    loginInstance = new LoginRepository(application);
                }
            }
        }
        return loginInstance;
    }

    public LiveData<LoginInfo> signIn(String userEmail, String userPassword,String macAddress) {
        loginInfoLiveData=new MediatorLiveData<>();
        loginInfoLiveData.setValue(null);
        Observable<Response<LoginInfo>> geoAccess=loginApiInterface.signIn(userEmail,userPassword,macAddress);
        geoAccess.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<LoginInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<LoginInfo> loginInfoResponse) {
                        if(loginInfoResponse.code()==200) {
                            LoginInfo loginInfo = loginInfoResponse.body();
                            if (loginInfo != null) {
                                loginInfo.setResponseCode(String.valueOf(loginInfoResponse.code()));
                                insertLoginData(loginInfo.getLogin());
                            }
                            loginInfoLiveData.postValue(loginInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LoginInfo logiInfo = new LoginInfo();
                        logiInfo.setResponseCode("selferror");
                        loginInfoLiveData.postValue(logiInfo);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return loginInfoLiveData;
    }

    /**
     * Insert login data from api response into database
     * @param login
     */
    private void insertLoginData(Login login){
        //TODO insert into database
//        new insertAsyncTask(mLoginDao).execute(login);
        Completable.fromRunnable(() -> mLoginDao.insert(login)).subscribeOn(Schedulers.io()).subscribe();


    }

    public LiveData<Login> getData() {
        loginData.addSource(mLoginDao.getLoginData(), login -> loginData.postValue(login));
        return  loginData;
    }

    private static  class insertAsyncTask extends AsyncTask<Login,Void,Void> {
        private LoginDao mLoginDao;
        public insertAsyncTask(LoginDao mLoginDao) {
            this.mLoginDao=mLoginDao;
        }

        @Override
        protected Void doInBackground(Login... login) {
            mLoginDao.insert(login[0]);
            return null;
        }
    }
}
