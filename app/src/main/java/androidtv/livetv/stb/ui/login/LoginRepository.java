package androidtv.livetv.stb.ui.login;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.splash.SplashApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginRepository {
    private static LoginRepository loginInstance;
    private SplashApiInterface loginApiInterface;
    private MediatorLiveData<LoginInfo> loginInfoLiveData;
    private  MediatorLiveData<Login> loginData;
    private LoginDao mLoginDao;
    private MediatorLiveData<CatChannelInfo> catChannelData;
    private CatChannelDao catChannelDao;

    private LoginRepository(Application application) {
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        mLoginDao = db.loginDao();
        catChannelDao=db.catChannelDao();
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

    public LiveData<CatChannelInfo> getChannels(String token, String utc, String userId , String hashValue) {
        catChannelData=new MediatorLiveData<>();
        catChannelData.setValue(null);
        Observable<Response<CatChannelInfo>> catChannel = loginApiInterface.getCatChannel(token, Long.parseLong(utc),userId,hashValue);
        catChannel.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<CatChannelInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<CatChannelInfo> catChannelInfoResponse) {
                        if (catChannelInfoResponse.code() == 200) {
                            CatChannelInfo catChannelInfo = catChannelInfoResponse.body();
                            if(catChannelInfo!=null) {
                                insertCategoryIntoDatabase(catChannelInfo.getCategory());
                                insertChannelsintoDatabase(catChannelInfo.getChannel());
                            }
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
        Completable.fromRunnable(() -> catChannelDao.insertChannels(channel)).subscribeOn(Schedulers.io()).subscribe();
    }

    private void insertCategoryIntoDatabase(List<CategoryItem> category) {
        Completable.fromRunnable(() -> catChannelDao.insertCategory(category)).subscribeOn(Schedulers.io()).subscribe();
    }
}
