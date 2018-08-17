package androidtv.livetv.stb.ui.login;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.CatChannelError;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CatChannelWrapper;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelInserted;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginError;
import androidtv.livetv.stb.entity.LoginErrorResponse;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.entity.LoginInvalidResponse;
import androidtv.livetv.stb.entity.LoginResponseWrapper;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.splash.SplashRepository;
import androidtv.livetv.stb.utils.ApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import androidtv.livetv.stb.utils.LoginFileUtils;
import androidtv.livetv.stb.utils.MyEncryption;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

import static androidtv.livetv.stb.ui.splash.SplashRepository.KEY_CATEGORY;
import static androidtv.livetv.stb.utils.LinkConfig.DATA_INSERTION_FAILED;
import static androidtv.livetv.stb.utils.LinkConfig.NO_CONNECTION;

public class LoginRepository {
    private static final String KEY_LOGIN_SUCCESS = "login";
    private static LoginRepository loginInstance;
    private ApiInterface loginApiInterface;
    private MediatorLiveData<LoginResponseWrapper> loginInfoLiveData;
    private MediatorLiveData<Login> loginData;
    private LoginDao mLoginDao;
    private MediatorLiveData<CatChannelWrapper> catChannelData;
    private CatChannelDao catChannelDao;
    private static final String KEY_MAC_INVALID = "error_code";
    private MediatorLiveData<Integer> channelCountData;
    private MediatorLiveData<List<ChannelItem>> channelListData;


    private LoginRepository(Application application) {
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        mLoginDao = db.loginDao();
        catChannelDao = db.catChannelDao();
        Retrofit retrofitInstance = ApiManager.getAdapter();
        loginApiInterface = retrofitInstance.create(ApiInterface.class);
        loginData = new MediatorLiveData<>();
        channelCountData = new MediatorLiveData<>();
        channelCountData.setValue(null);
        channelCountData.addSource(catChannelDao.getChannelTableSize(), integer -> {
            channelCountData.removeSource(catChannelDao.getCatTableSize());
            channelCountData.postValue(integer);
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
    public void insertCatChannelToDB(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        insertCatChannelsToDatabase(categoryList, channelList);
    }
    public LiveData<LoginResponseWrapper> signIn(String userEmail, String userPassword, String macAddress) {
        loginInfoLiveData = new MediatorLiveData<>();
        loginInfoLiveData.setValue(null);
        LoginResponseWrapper loginResponseWrapper = new LoginResponseWrapper();
        Gson gson = new Gson();
        Observable<Response<ResponseBody>> geoAccess = loginApiInterface.signIn(userEmail, userPassword, macAddress);
        geoAccess.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> loginInfoResponse) {
                        String json = null;
                        try {
                            json = loginInfoResponse.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if (jsonObject.has(KEY_LOGIN_SUCCESS)) {
                                JSONObject loginobject=jsonObject.getJSONObject(KEY_LOGIN_SUCCESS);
                                if(loginobject.has(KEY_MAC_INVALID)){
                                    LoginInvalidResponse loginInvalid=gson.fromJson(json,LoginInvalidResponse.class);
                                    loginResponseWrapper.setLoginInvalidResponse(loginInvalid);
                                }else {
                                    LoginInfo loginInfo = gson.fromJson(json, LoginInfo.class);
                                    loginResponseWrapper.setLoginInfo(loginInfo);
                                    insertLoginData(loginInfo.getLogin(), userPassword, macAddress);
                                }
                            } else {
                                LoginErrorResponse loginErrorResponse = gson.fromJson(json, LoginErrorResponse.class);
                                loginResponseWrapper.setLoginErrorResponse(loginErrorResponse);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        loginInfoLiveData.postValue(loginResponseWrapper);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LoginErrorResponse loginErrorResponse = new LoginErrorResponse();
                        LoginError loginError = new LoginError();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            loginError.setErrorCode(NO_CONNECTION);
                            loginError.setMessage(e.getLocalizedMessage());
                            loginErrorResponse.setError(loginError);
                        } else {
                            loginError.setErrorCode(500);
                            loginError.setMessage(e.getLocalizedMessage());
                        }
                        loginResponseWrapper.setLoginErrorResponse(loginErrorResponse);
                        loginInfoLiveData.postValue(loginResponseWrapper);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return loginInfoLiveData;
    }

    /**
     * Insert login data from api response into database
     *
     * @param login
     * @param userPassword
     */
    private void insertLoginData(Login login, String userPassword, String macAddress) {
        //TODO insert into database
//        new insertAsyncTask(mLoginDao).execute(login);
        Completable.fromRunnable(() -> {
            mLoginDao.insert(login);
            writeLoginDataToFile(login, userPassword, macAddress);
            writeAuthTokenToFile(login);
        }).subscribeOn(Schedulers.io()).subscribe();


    }

    private void writeAuthTokenToFile(Login login) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Timber.d("Token:" + login.getToken());

            File externalStorageDir = Environment.getExternalStorageDirectory();
            File myFile = new File(externalStorageDir, LinkConfig.TOKEN_CONFIG_FILE_NAME);

            try {
                FileOutputStream fOut1 = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut1);
                myOutWriter.append(login.getToken());
                myOutWriter.close();
                fOut1.close();
            } catch (Exception e) {
                Timber.wtf(e);
            }

        }
    }

    private void writeLoginDataToFile(Login login, String userPassword, String macAddress) {
        LoginFileUtils.reWriteLoginDetailsToFile(macAddress,
                login.getEmail(), new MyEncryption().getEncryptedToken(userPassword), login.getSession(), String.valueOf(login.getId()));
    }

    public LiveData<Login> getData() {
        loginData.addSource(mLoginDao.getLoginData(), login -> loginData.postValue(login));
        return loginData;
    }

    public LiveData<CatChannelWrapper> getChannels(String token, String utc, String userId, String hashValue) {
        catChannelData = new MediatorLiveData<>();
        catChannelData.setValue(null);
        CatChannelWrapper catChannelWrapper = new CatChannelWrapper();
        Gson gson = new Gson();
        Observable<Response<ResponseBody>> catChannel = loginApiInterface.getCatChannel(token, Long.parseLong(utc), userId, hashValue);
        catChannel.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> catChannelInfoResponse) {
                        String json = null;
                        try {
                            json = catChannelInfoResponse.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if (jsonObject.has(KEY_CATEGORY)) {
                                CatChannelInfo catChannelInfo = gson.fromJson(json, CatChannelInfo.class);
                                catChannelWrapper.setCatChannelInfo(catChannelInfo);
                                insertCatChannelsToDatabase(catChannelInfo.getCategory(), catChannelInfo.getChannel());
                            } else {
                                CatChannelError catChannelError = gson.fromJson(json, CatChannelError.class);
                                catChannelWrapper.setCatChannelError(catChannelError);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        catChannelData.postValue(catChannelWrapper);
                    }

                    @Override
                    public void onError(Throwable e) {
                        CatChannelInfo catChannelInfo = new CatChannelInfo();
                        CatChannelError catChannelError = new CatChannelError();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            catChannelData.addSource(catChannelDao.getCategories(), categoryItems -> {
                                if (categoryItems != null) {
                                    catChannelInfo.setCategory(categoryItems);
                                    catChannelData.removeSource(catChannelDao.getCategories());
                                    catChannelData.addSource(catChannelDao.getChannels(), channelItemList -> {
                                        if (channelItemList != null) {
                                            catChannelInfo.setChannel(channelItemList);
                                            catChannelWrapper.setCatChannelInfo(catChannelInfo);
                                        }
                                    });
                                } else {
                                    catChannelError.setStatus(NO_CONNECTION);
                                    catChannelError.setErrorMessage(e.getLocalizedMessage());
                                    catChannelWrapper.setCatChannelError(catChannelError);
                                }
                            });

                        } else {
                            catChannelError.setStatus(500);
                            catChannelError.setErrorMessage(e.getLocalizedMessage());
                            catChannelWrapper.setCatChannelError(catChannelError);
                        }
                        catChannelData.postValue(catChannelWrapper);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return catChannelData;

    }

    private void insertCatChannelsToDatabase(List<CategoryItem> categoryList, List<ChannelItem> channels) {
        new insertChannelDataTask(categoryList,channels,catChannelDao).execute();
    }

    public LiveData<Integer> getChannelCount() {
        return channelCountData;
    }

    public LiveData<List<ChannelItem>> getChannelList() {
        return channelListData;
    }

    private static class insertChannelDataTask extends AsyncTask<Void, Void, Boolean> {

        private  List<CategoryItem> categoryItemList;
        private List<ChannelItem> channelItemList;
        private CatChannelDao dao;
        boolean inserted=false;

        insertChannelDataTask(List<CategoryItem> categoryItemList,List<ChannelItem> channelItemList,CatChannelDao dao) {
            this.categoryItemList=categoryItemList;
            this.channelItemList=channelItemList;
            this.dao=dao;

        }

        @Override
        protected Boolean doInBackground(Void... updateResult) {
            long[] categoryResult=dao.insertCategory(categoryItemList);
            long[] channelResult=dao.insertChannels(channelItemList);
            return Arrays.asList(categoryResult).contains(DATA_INSERTION_FAILED) || Arrays.asList(channelResult).contains(DATA_INSERTION_FAILED);
        }

        @Override
        protected void onPostExecute(Boolean isInserted) {
            super.onPostExecute(isInserted);
            EventBus.getDefault().post(new ChannelInserted(isInserted));
        }
    }
}
