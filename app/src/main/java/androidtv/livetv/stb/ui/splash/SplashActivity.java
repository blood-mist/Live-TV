package androidtv.livetv.stb.ui.splash;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidtv.livetv.stb.BuildConfig;
import androidtv.livetv.stb.R;
import androidtv.livetv.stb.downloads.DownloadFragment;
import androidtv.livetv.stb.downloads.DownloadService;
import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CatChannelWrapper;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.LoginError;
import androidtv.livetv.stb.ui.login.LoginActivity;
import androidtv.livetv.stb.ui.unauthorized.UnauthorizedAccess;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.VideoPlayActivity;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.CustomDialogManager;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import androidtv.livetv.stb.utils.LoginFileUtils;
import androidtv.livetv.stb.utils.MyEncryption;
import androidtv.livetv.stb.utils.PackageUtils;
import androidtv.livetv.stb.utils.PermissionUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static androidtv.livetv.stb.utils.LinkConfig.ACCOUNT_PACKAGE;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_FRAGMENT;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_ID;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_LINK;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.INVALID_HASH;
import static androidtv.livetv.stb.utils.LinkConfig.INVALID_USER;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_CODE;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_MESSAGE;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_IP;
import static androidtv.livetv.stb.utils.LinkConfig.NO_CONNECTION;
import static androidtv.livetv.stb.utils.LinkConfig.USER_EMAIL;

public class SplashActivity extends AppCompatActivity implements PermissionUtils.PermissionResultCallback, DownloadFragment.OnDismissInteraction {
    private static final String MAC_REGISTERED = "yes";
    private static final String MAC_NOT_REGISTERED = "no";
    private static final String GEO_ACCESS_ENABLED = "true";
    private static final int UPDATE_ID = 1;
    private static final int FORCE_ID = 2;
    private static final int DOWNLOAD_ACCOUNT = 3;
    private static final int CHECK_USER = 4;
    @BindView(R.id.txt_version_loading)
    TextView appVersion;
    private SplashViewModel splashViewModel;
    private String macAddress;
    private AppVersionInfo appVersionInfo;
    private PermissionUtils permissionutils;
    private String accountDownloadLink = "";
    ArrayList<String> permissions;
    private CatChannelInfo catChannelInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        permissionutils = new PermissionUtils(this);
        macAddress = AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this);
        permissions = new ArrayList<>(
                Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE));


    }

    @Override
    protected void onStart() {
        super.onStart();
        appVersion.setText(BuildConfig.VERSION_NAME);
        splashViewModel = ViewModelProviders.of(this).get(SplashViewModel.class);
        checkIfLoginDetailsAvailable();

    }

    private void checkIfLoginDetailsAvailable() {
        LiveData<Integer> loginDataSize = splashViewModel.checkIfDataExists();
        loginDataSize.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
                    Timber.d(integer + "");
                    if (integer > 0)
                        fetchLoginDataFromDB();
                    else
                        checkForValidMacAddress();

                    loginDataSize.removeObserver(this);

                }
            }
        });

    }

    private void fetchLoginDataFromDB() {
        splashViewModel.checkDatainDb().observe(this, login -> {
            if (login != null) {
                GlobalVariables.login = login;
                long utc = GetUtc.getInstance().getTimestamp().getUtc();
                fetchChannelDetails(login.getToken(), utc, login.getId(), LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc), login.getSession()));
            }
        });
    }


    private void fetchChannelDetails(String token, long utc, int id, String hashCode) {
        LiveData<CatChannelWrapper> categoryChannelData = splashViewModel.fetchChannelDetails(token, String.valueOf(utc), String.valueOf(id), hashCode);
        categoryChannelData.observe(this, new Observer<CatChannelWrapper>() {
            @Override
            public void onChanged(@Nullable CatChannelWrapper catChannelWrapper) {
                if (catChannelWrapper != null) {
                    if (catChannelWrapper.getCatChannelInfo() != null) {
                        Timber.d(catChannelWrapper.getCatChannelInfo().getCategory().size() + "");
                        catChannelInfo = catChannelWrapper.getCatChannelInfo();
                        checkForExistingChannelData();
                    } else {
                        switch (catChannelWrapper.getCatChannelError().getStatus()) {
                            case INVALID_HASH:
                                showErrorDialog(INVALID_HASH, getString(R.string.session_expired));
                                break;
                            case INVALID_USER:
                                showErrorDialog(INVALID_USER, catChannelWrapper.getCatChannelError().getErrorMessage());
                                break;
                        }
                    }
                    categoryChannelData.removeObserver(this);

                }
            }

        });
    }

    private void showErrorDialog(int errorCode, String message) {
        CustomDialogManager splashError = new CustomDialogManager(this, CustomDialogManager.ALERT);
        splashError.build();
        splashError.showMacAndVersion();
        splashError.setMessage(String.valueOf(errorCode), message);
        splashError.setExtraButton(v -> {
            splashError.dismiss();
            finish();
        });
        splashError.setNeutralButton(getString(R.string.btn_retry), view -> {
            splashError.dismiss();
            switch (errorCode) {
                case INVALID_HASH:
                    showLogin(GlobalVariables.login.getEmail());
                    break;
                case INVALID_USER:
                    checkValidUser();
                    break;

            }

        });
        splashError.show();
    }

    private void checkForExistingChannelData() {
        LiveData<Integer> channelSize = splashViewModel.checkChannelsInDB();
        channelSize.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
                    Timber.d("size:" + integer);
                    if (integer > 0) {
                        fetchChannelsFromDBtoUpdate();
                    } else {
                        insertDataToDB();
                    }
                    channelSize.removeObserver(this);
                }

            }
        });

    }

    private void fetchChannelsFromDBtoUpdate() {
        splashViewModel.getAllChannelsInDBToCompare().observe(this, channelItemList -> {
            if (channelItemList != null) {
                updateListData(channelItemList, catChannelInfo.getChannel());
            }
        });

    }

    private void updateListData(List<ChannelItem> channelItemList, List<ChannelItem> channels) {
        Completable.fromRunnable(() -> {
            for (int i = 0; i < channels.size(); i++) {
                for (ChannelItem dbCHannelItem : channelItemList) {
                    if (channels.get(i).getId() == dbCHannelItem.getId()) {
                        channels.get(i).setIs_fav(dbCHannelItem.getIs_fav());
                    }
                }
            }
            saveChannelDetailstoDb(catChannelInfo.getCategory(), channels);

        }).subscribeOn(Schedulers.io()).subscribe().dispose();
//        channels.forEach(channelItem -> channelItemList.stream().filter(channelItem1 -> channelItem1.getId() == channelItem.getId()).findAny().ifPresent(channelItem1 -> channelItem.setIs_fav(channelItem1.getIs_fav())));


    }


    private void insertDataToDB() {
        saveChannelDetailstoDb(catChannelInfo.getCategory(), catChannelInfo.getChannel());

    }

    private void saveChannelDetailstoDb(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        splashViewModel.insertCatChannelToDB(categoryList, channelList);
        Timber.d("gotoChannelLoad");
        loadChannelActivity();
    }

    private void loadChannelActivity() {
        Intent channelLoadIntent = new Intent(this, VideoPlayActivity.class);
        channelLoadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(channelLoadIntent);
        finish();
    }

    /**
     * check if the device is registered or not, if no connection found check d for offline
     */
    private void checkForValidMacAddress() {
        splashViewModel.checkIfValidMacAddress(macAddress).observe(this, macInfo -> {
            if (macInfo != null)
                if (macInfo.getResponseCode() != NO_CONNECTION)
                    switch (macInfo.getMacExists()) {
                        case MAC_REGISTERED:
                            checkGeoAccessibility();
                            break;
                        case MAC_NOT_REGISTERED:
                            loadUnauthorized(macInfo.getCode(), macInfo.getMessage(), "N/A");
                            break;
                        default:
                            CustomDialogManager.dataNotFetched(this);
                            break;

                    }
                else
                    CustomDialogManager.ReUsedCustomDialogs.noInternet(this);


        });
    }

    private void loadUnauthorized(String error_code, String error_message, String ip) {
        Intent unauthorizedIntent = new Intent(this, UnauthorizedAccess.class);
        unauthorizedIntent.putExtra(LIVE_ERROR_CODE, error_code);
        unauthorizedIntent.putExtra(LIVE_ERROR_MESSAGE, error_message);
        unauthorizedIntent.putExtra(LIVE_IP, ip);
        unauthorizedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(unauthorizedIntent);
        finish();
    }

    private void checkGeoAccessibility() {
        splashViewModel.checkIfGeoAccessEnabled().observe(this, geoAccessInfo -> {
            if (geoAccessInfo != null) {
                if (geoAccessInfo.getAllow().getAllow().equalsIgnoreCase(GEO_ACCESS_ENABLED))
                    initVersionCheck();
                else
                    loadUnauthorized(geoAccessInfo.getAllow().getCode(), geoAccessInfo.getError(), geoAccessInfo.getAllow().getIp());
            }
        });
    }

    private void initVersionCheck() {
        splashViewModel.checkVersion(macAddress, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME, BuildConfig.APPLICATION_ID).observe(this, versionResponseWrapper -> {
            if (versionResponseWrapper != null) {
                if (versionResponseWrapper.getAppVersionInfo() != null && !versionResponseWrapper.getAppVersionInfo().isEmpty())
                    for (AppVersionInfo appVersionInfo : versionResponseWrapper.getAppVersionInfo()) {
                        if (appVersionInfo.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                            compareVersion(appVersionInfo);
                        }
                        if (appVersionInfo.getPackageName().equalsIgnoreCase(ACCOUNT_PACKAGE)) {
                            accountDownloadLink = appVersionInfo.getApkDownloadLink();
                        }
                    }

                else {
                    Toast.makeText(SplashActivity.this, versionResponseWrapper.getVersionErrorResponse().getMessage(), Toast.LENGTH_LONG).show();
                    permissionutils.check_permission(permissions, getString(R.string.request_permissions), CHECK_USER);
                }
            }
        });
    }

    private void compareVersion(AppVersionInfo appVersionInfo) {
        this.appVersionInfo = appVersionInfo;
        if (appVersionInfo.getUpdate()) {
            permissionutils.check_permission(permissions, getString(R.string.request_permissions), UPDATE_ID);
        } else {
            permissionutils.check_permission(permissions, getString(R.string.request_permissions), CHECK_USER);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionutils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void downloadApk(String apkDownloadLink, String appName, String message, int statusId) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DOWNLOAD_LINK, apkDownloadLink);
        intent.putExtra(DOWNLOAD_NAME, appName);
        intent.putExtra(DOWNLOAD_ID, statusId);
        startService(intent);
        getSupportFragmentManager().beginTransaction().replace(R.id.splash_container, DownloadFragment.newInstance(getString(R.string.update), message, statusId, apkDownloadLink), DOWNLOAD_FRAGMENT).commit();


    }

    @Override
    public void PermissionGranted(int request_code) {
        switch (request_code) {
            case UPDATE_ID:
                apkVersionCheck();
                break;
            case CHECK_USER:
                checkValidUser();
                break;
        }
    }

    private void apkVersionCheck() {
        switch (appVersionInfo.getUpdateType()) {

            case "force":
                downloadApk(appVersionInfo.getApkDownloadLink(), getString(R.string.app_name), "Connecting...", FORCE_ID);
                break;
            case "normal":
                downloadApk(appVersionInfo.getApkDownloadLink(), getString(R.string.app_name), getString(R.string.msg_update), UPDATE_ID);
                break;
            default:
                checkValidUser();
                break;
        }
    }

    private void checkValidUser() {
        splashViewModel.checkIfUserRegistered(macAddress).observe(this, userCheckWrapper -> {
                    if (userCheckWrapper != null) {
                        if (userCheckWrapper.getUserCheckInfo() != null) {
                            if ((userCheckWrapper.getUserCheckInfo().getData().getActivationStatus() == 1 && userCheckWrapper.getUserCheckInfo().getData().getIsActive() == 1)) {
                                if (LoginFileUtils.checkIfFileExists(LinkConfig.LOGIN_FILE_NAME)) {
                                    proceedToLoginViaFile(userCheckWrapper.getUserCheckInfo().getData().getUserName());
                                } else {
                                    showLogin(userCheckWrapper.getUserCheckInfo().getData().getUserName());
                                }
                            }
                        } else {
                            if (userCheckWrapper.getUserErrorInfo().getStatus() == 401 || userCheckWrapper.getUserErrorInfo().getStatus() == 402) {
                                openAccountApk(ACCOUNT_PACKAGE, accountDownloadLink);
                            } else
                                loadUnauthorized(String.valueOf(userCheckWrapper.getUserErrorInfo().getStatus()), userCheckWrapper.getUserErrorInfo().getMessage(), "N/A");

                        }
                    }
                }

        );
    }

    private void proceedToLoginViaFile(String emailFrmApi) {
        if (LoginFileUtils.readFromFile(AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this))) {
            String encrypted_password = LoginFileUtils.getUserPassword() + "";


            Timber.d("File:", LoginFileUtils.getUserEmail() + "");
            String userEmail = LoginFileUtils.getUserEmail();
            Timber.d("File:", encrypted_password + "");

            MyEncryption sUtils = new MyEncryption();
            Timber.d("Decrypting " + encrypted_password);
            String decrypted_password = sUtils.getDecryptedToken(encrypted_password);
            Timber.d("CheckingPassword", decrypted_password);
            splashViewModel.loginFromFile(userEmail, decrypted_password, macAddress).observe(this, loginResponseWrapper -> {
                if (loginResponseWrapper != null) {
                    if (loginResponseWrapper.getLoginInfo() != null) {
                        GlobalVariables.login = loginResponseWrapper.getLoginInfo().getLogin();
                        long utc = GetUtc.getInstance().getTimestamp().getUtc();
                        fetchChannelDetails(loginResponseWrapper.getLoginInfo().getLogin().getToken(), utc,
                                loginResponseWrapper.getLoginInfo().getLogin().getId(), LinkConfig.getHashCode(String.valueOf(loginResponseWrapper.getLoginInfo().getLogin().getId()),
                                        String.valueOf(utc), loginResponseWrapper.getLoginInfo().getLogin().getSession()));
                    } else if (loginResponseWrapper.getLoginInvalidResponse() != null) {
                        if (loginResponseWrapper.getLoginInvalidResponse().getLoginInvalidData().getErrorCode().equals("404")) {
                            loadUnauthorized("404", getString(R.string.mac_not_registered), "N/A");
                        } else {
                            Toast.makeText(this, loginResponseWrapper.getLoginInvalidResponse().getLoginInvalidData().getMessage(), Toast.LENGTH_LONG).show();
                            LoginFileUtils.deleteLoginFile();
                            checkForValidMacAddress();
                        }

                    } else {
                        showLoginErrorDialog(loginResponseWrapper.getLoginErrorResponse().getError(), userEmail);
                        LoginFileUtils.deleteLoginFile();
                    }

                }
            });
        } else {
            LoginFileUtils.deleteLoginFile();
            showLogin(emailFrmApi);
        }
    }

    private void showLoginErrorDialog(LoginError error, String userEmail) {
        CustomDialogManager loginError = new CustomDialogManager(this, CustomDialogManager.ALERT);
        loginError.build();
        loginError.showMacAndVersion();
        loginError.setMessage(String.valueOf(error.getErrorCode()), error.getMessage());
        loginError.setExtraButton(v -> {
            loginError.dismiss();
            finish();
        });
        loginError.setNeutralButton(getString(R.string.btn_retry), view -> {
            loginError.dismiss();
            showLogin(userEmail);
        });
        loginError.show();
    }

    private void openAccountApk(String accountPackage, String accountDownloadLink) {
        if (PackageUtils.isPackageInstalled(this, accountPackage)) {
            Intent openApk = getPackageManager()
                    .getLaunchIntentForPackage(accountPackage);
            startActivity(openApk);
            finish();
        } else {
            downloadApk(accountDownloadLink, getString(R.string.myaccount), getString(R.string.download_myaccount), UPDATE_ID);
        }
    }

    private void showLogin(String userEmail) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.putExtra(USER_EMAIL, userEmail);
        startActivity(loginIntent);
        finish();

    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> pending_permissions) {
        permissionutils.check_permission(pending_permissions, getString(R.string.request_permissions), request_code);

    }

    @Override
    public void PermissionDenied(int request_code) {
        finish();
    }

    @Override
    public void NeverAskAgain(int request_code) {
        switch (request_code) {
            case UPDATE_ID:
                apkVersionCheck();
                break;
            case CHECK_USER:
                checkValidUser();
                break;

        }

    }

    @Override
    public void onDismissBtnClicked() {
        checkValidUser();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }
}
