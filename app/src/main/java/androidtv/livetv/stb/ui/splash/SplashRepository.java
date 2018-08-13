package androidtv.livetv.stb.ui.splash;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.Environment;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidtv.livetv.stb.db.AndroidTvDatabase;
import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.CatChannelError;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CatChannelWrapper;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginError;
import androidtv.livetv.stb.entity.LoginErrorResponse;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.entity.LoginInvalidResponse;
import androidtv.livetv.stb.entity.LoginResponseWrapper;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckInfo;
import androidtv.livetv.stb.entity.UserCheckWrapper;
import androidtv.livetv.stb.entity.UserErrorInfo;
import androidtv.livetv.stb.entity.VersionErrorResponse;
import androidtv.livetv.stb.entity.VersionResponseWrapper;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.login.LoginDao;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.ApiInterface;
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

import static androidtv.livetv.stb.utils.LinkConfig.NO_CONNECTION;

public class SplashRepository {
    private static final String IS_JSON_ARRAY = "is_json_array";
    private static final String IS_JSON_OBJECT = "is_json_object";
    private static final String UNEXPECTED_JSON = "json_unexpected";
    private static final String ON_SUCCESS_KEY = "data";
    private static final String KEY_LOGIN_SUCCESS = "login";
    public static final String KEY_CATEGORY = "category";
    private static final String KEY_MAC_INVALID = "error_code";
    private static SplashRepository sInstance;
    private MediatorLiveData<MacInfo> macInfoMediatorLiveData;
    private MediatorLiveData<GeoAccessInfo> geoAccessLiveData;
    private MediatorLiveData<VersionResponseWrapper> appInfoLiveData;
    private ApiInterface apiInterface;
    private MediatorLiveData<UserCheckWrapper> userCheckLiveData;
    private MediatorLiveData<Login> userCredentialData;
    private MediatorLiveData<Integer> rowCountData;
    private MediatorLiveData<Integer> channelCountData;
    private MediatorLiveData<CatChannelWrapper> catChannelData;
    private LoginDao mLoginDao;
    private CatChannelDao catChannelDao;
    private MediatorLiveData<List<ChannelItem>> channelListData;
    private MediatorLiveData<LoginResponseWrapper> loginInfoLiveData;

    SplashRepository(Application application) {
        AndroidTvDatabase db = AndroidTvDatabase.getDatabase(application);
        Retrofit retrofitInstance = ApiManager.getAdapter();
        mLoginDao = db.loginDao();
        catChannelDao = db.catChannelDao();
        userCredentialData = new MediatorLiveData<>();
        userCredentialData.setValue(null);
        apiInterface = retrofitInstance.create(ApiInterface.class);
        rowCountData = new MediatorLiveData<>();
        rowCountData.setValue(null);
        rowCountData.addSource(mLoginDao.getTableSize(), integer -> {
            if (integer != null) {
                rowCountData.postValue(integer);
            }


        });
        channelCountData = new MediatorLiveData<>();
        channelCountData.setValue(null);
        channelCountData.addSource(catChannelDao.getChannelTableSize(), integer -> channelCountData.postValue(integer));

        channelListData = new MediatorLiveData<>();
        channelListData.setValue(null);
        channelListData.addSource(catChannelDao.getChannels(), channelItemList -> {
            if (channelItemList != null) {
                channelListData.postValue(channelItemList);
            }
        });

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
        macInfoMediatorLiveData = new MediatorLiveData<>();
        macInfoMediatorLiveData.setValue(null);
        Observable<Response<MacInfo>> macObserver = apiInterface.checkMacValidation(macAddress);
        macObserver.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<MacInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<MacInfo> macInfoResponse) {
                        if (macInfoResponse.code() == 200) {
                            MacInfo macInfo = macInfoResponse.body();
                            macInfo.setResponseCode(macInfoResponse.code());
                            macInfoMediatorLiveData.postValue(macInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MacInfo macInfo = new MacInfo();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            macInfo.setResponseCode(NO_CONNECTION);
                        } else {
                            macInfo.setResponseCode(0);
                        }
                        macInfo.setMacExists("");
                        macInfo.setMessage(e.getMessage());
                        macInfoMediatorLiveData.postValue(macInfo);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return macInfoMediatorLiveData;

    }

    public LiveData<GeoAccessInfo> isGeoAccessEnabled() {
        geoAccessLiveData = new MediatorLiveData<>();
        geoAccessLiveData.setValue(null);
        Observable<Response<GeoAccessInfo>> geoAccess = apiInterface.checkGeoAccess();
        geoAccess.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<GeoAccessInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<GeoAccessInfo> geoAccessInfoResponse) {
                        if (geoAccessInfoResponse.code() == 200) {
                            GeoAccessInfo geoAccessInfo = geoAccessInfoResponse.body();
                            geoAccessInfo.setResponseCode(geoAccessInfoResponse.code());

                            geoAccessLiveData.postValue(geoAccessInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        GeoAccessInfo geoAccessInfo = new GeoAccessInfo();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            geoAccessInfo.setResponseCode(NO_CONNECTION);
                        } else {
                            geoAccessInfo.setResponseCode(0);
                        }
                        geoAccessInfo.getAllow().setAllow("false");
                        geoAccessLiveData.postValue(geoAccessInfo);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return geoAccessLiveData;
    }


    public LiveData<VersionResponseWrapper> isNewVersionAvailable(String macAddress, int versionCode, String versionName, String applicationId) {
        appInfoLiveData = new MediatorLiveData<>();
        appInfoLiveData.setValue(null);
        VersionResponseWrapper versionResponseWrapper = new VersionResponseWrapper();
        Gson gson = new Gson();

        Observable<Response<ResponseBody>> checkVersion = apiInterface.checkForAppVersion(macAddress, versionCode, versionName, applicationId);
        checkVersion.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> versionInfoResponse) {
                        String json = null;
                        try {
                            json = versionInfoResponse.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        switch (isResponseArray(json)) {
                            case IS_JSON_ARRAY:
                                List<AppVersionInfo> appDataList = new ArrayList<>();
                                JSONArray appArray = null;
                                try {
                                    appArray = new JSONArray(json);
                                    for (int i = 0; i < appArray.length(); i++) {
                                        JSONObject appobject = appArray.getJSONObject(i);
                                        String appObjToString = appobject.toString();
                                        AppVersionInfo appData = gson.fromJson(appObjToString, AppVersionInfo.class);
                                        appDataList.add(appData);
                                        versionResponseWrapper.setAppVersionInfo(appDataList);
                                        appInfoLiveData.postValue(versionResponseWrapper);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case IS_JSON_OBJECT:
                                VersionErrorResponse errorResponse = gson.fromJson(json, VersionErrorResponse.class);
                                versionResponseWrapper.setVersionErrorResponse(errorResponse);
                                appInfoLiveData.postValue(versionResponseWrapper);
                                break;
                            default:
                                onError(new Throwable("ERR_RESPONSE_UNEXPECTED"));
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        VersionErrorResponse versionInfo = new VersionErrorResponse();
                        versionInfo.setMessage(e.getLocalizedMessage());
                        versionInfo.setStatus(401);
                        versionResponseWrapper.setVersionErrorResponse(versionInfo);
                        appInfoLiveData.postValue(versionResponseWrapper);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return appInfoLiveData;
    }

    private String isResponseArray(String json) {
        try {
            new JSONArray(json);
            return IS_JSON_ARRAY;
        } catch (JSONException e) {
            e.printStackTrace();
            try {
                new JSONObject(json);
                return IS_JSON_OBJECT;
            } catch (JSONException e1) {
                e1.printStackTrace();
                return UNEXPECTED_JSON;
            }

        }
    }

    public LiveData<UserCheckWrapper> isUserRegistered(String macAddress) {
        userCheckLiveData = new MediatorLiveData<>();
        userCheckLiveData.setValue(null);
        UserCheckWrapper userCheckWrapper = new UserCheckWrapper();
        Gson gson = new Gson();
        Observable<Response<ResponseBody>> userCheckObserver = apiInterface.checkUserStatus(macAddress);
        userCheckObserver.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> userCheckInfoResponse) {
                        String json = null;
                        try {
                            json = userCheckInfoResponse.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if (jsonObject.has(ON_SUCCESS_KEY)) {
                                UserCheckInfo userCheckInfo = gson.fromJson(json, UserCheckInfo.class);
                                userCheckWrapper.setUserCheckInfo(userCheckInfo);
                            } else {
                                UserErrorInfo userErrorInfo = gson.fromJson(json, UserErrorInfo.class);
                                userCheckWrapper.setUserErrorInfo(userErrorInfo);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        userCheckLiveData.postValue(userCheckWrapper);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //set Extra Code for error Handeling
                        UserErrorInfo userErrorInfo = new UserErrorInfo();
                        if (e instanceof HttpException || e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
                            userErrorInfo.setStatus(NO_CONNECTION);
                            userErrorInfo.setMessage(e.getLocalizedMessage());
                        } else {
                            userErrorInfo.setStatus(500);
                            userErrorInfo.setMessage(e.getLocalizedMessage());
                        }
                        userCheckWrapper.setUserErrorInfo(userErrorInfo);
                        userCheckLiveData.postValue(userCheckWrapper);


                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return userCheckLiveData;

    }

    public LiveData<Login> getData() {
        userCredentialData.addSource(mLoginDao.getLoginData(), login -> {
            if (login != null) {
                userCredentialData.postValue(login);
            }

        });
        return userCredentialData;
    }

    public LiveData<CatChannelWrapper> getChannels(String token, String utc, String userId, String hashValue) {
        catChannelData = new MediatorLiveData<>();
        catChannelData.setValue(null);
        CatChannelWrapper catChannelWrapper = new CatChannelWrapper();
        Gson gson = new Gson();
        Observable<Response<ResponseBody>> catChannel = apiInterface.getCatChannel(token, Long.parseLong(utc), userId, hashValue);
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
                                    catChannelData.addSource(catChannelDao.getChannels(), channelItemList -> {
                                        if (channelItemList != null) {
                                            catChannelInfo.setChannel(channelItemList);
                                            catChannelWrapper.setCatChannelInfo(catChannelInfo);
                                            catChannelData.postValue(catChannelWrapper);
                                        }
                                    });
                                } else {
                                    catChannelError.setStatus(NO_CONNECTION);
                                    catChannelError.setErrorMessage(e.getLocalizedMessage());
                                    catChannelWrapper.setCatChannelError(catChannelError);
                                    catChannelData.postValue(catChannelWrapper);
                                }
                            });

                        } else {
                            catChannelError.setStatus(500);
                            catChannelError.setErrorMessage(e.getLocalizedMessage());
                            catChannelWrapper.setCatChannelError(catChannelError);
                            catChannelData.postValue(catChannelWrapper);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return catChannelData;

    }


    private void insertCatChannelsToDatabase(List<CategoryItem> categoryList, List<ChannelItem> channels) {
        Completable.fromRunnable(() -> {
            catChannelDao.insertCategory(categoryList);
            catChannelDao.insertChannels(channels);
        }).subscribeOn(Schedulers.io()).subscribe().dispose();
    }

    public LiveData<Integer> getRowCount() {
        return rowCountData;

    }

    public LiveData<Integer> getChannelCount() {
        return channelCountData;
    }

    public void insertCatChannelToDB(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        insertCatChannelsToDatabase(categoryList, channelList);
    }

    public LiveData<List<ChannelItem>> getChannelList() {
        return channelListData;
    }

    public LiveData<LoginResponseWrapper> getLoginResponse(String userEmail, String userPassword, String macAddress) {
        loginInfoLiveData = new MediatorLiveData<>();
        loginInfoLiveData.setValue(null);
        LoginResponseWrapper loginResponseWrapper = new LoginResponseWrapper();
        Gson gson = new Gson();
        Observable<Response<ResponseBody>> login = apiInterface.signIn(userEmail, userPassword, macAddress);
        login.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io())
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

    private void insertLoginData(Login login, String userPassword, String macAddress) {
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

    public void deleteLoginFile(){
        Completable.fromRunnable(LoginFileUtils::deleteLoginFile).subscribeOn(Schedulers.io()).subscribe();
    }
    public void deleteLoginFromDB() {
        Completable.fromRunnable(()-> mLoginDao.deleteAll()).subscribeOn(Schedulers.io()).subscribe();
    }
}
