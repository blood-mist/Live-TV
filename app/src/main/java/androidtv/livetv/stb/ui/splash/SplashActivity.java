package androidtv.livetv.stb.ui.splash;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import androidtv.livetv.stb.BuildConfig;
import androidtv.livetv.stb.R;
import androidtv.livetv.stb.downloads.DownloadFragment;
import androidtv.livetv.stb.downloads.DownloadService;
import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.UserLoginData;
import androidtv.livetv.stb.ui.login.LoginActivity;
import androidtv.livetv.stb.ui.unauthorized.UnauthorizedAccess;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.CustomDialogManager;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.PackageUtils;
import androidtv.livetv.stb.utils.PermissionUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidtv.livetv.stb.utils.LinkConfig.ACCOUNT_PACKAGE;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_FRAGMENT;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_ID;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_LINK;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_CODE;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_MESSAGE;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_IP;
import static androidtv.livetv.stb.utils.LinkConfig.USER_EMAIL;
import static androidtv.livetv.stb.utils.LinkConfig.USER_PASSWORD;

public class SplashActivity extends AppCompatActivity implements PermissionUtils.PermissionResultCallback, DownloadFragment.OnDismissInteraction {
    private static final String MAC_REGISTERED = "yes";
    private static final String MAC_NOT_REGISTERED = "no";
    private static final String GEO_ACCESS_ENABLED = "true";
    private static final int UPDATE_ID = 1;
    private static final int FORCE_ID = 2;
    @BindView(R.id.txt_version_loading)
    TextView appVersion;
    private SplashViewModel splashViewModel;
    private String macAddress;
    private AppVersionInfo appVersionInfo;
    private PermissionUtils permissionutils;
    private String accountDownloadLink = "";
    ArrayList<String> permissions;

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
        checkForValidMacAddress();
    }

    private void checkForValidMacAddress() {
        splashViewModel.checkIfValidMacAddress(macAddress).observe(this, macInfo -> {
            if (macInfo != null)
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
        });
    }

    private void loadUnauthorized(String error_code, String error_message, String ip) {
        Intent unauthorizedIntent = new Intent(this, UnauthorizedAccess.class);
        unauthorizedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        unauthorizedIntent.putExtra(LIVE_ERROR_CODE, error_code);
        unauthorizedIntent.putExtra(LIVE_ERROR_MESSAGE, error_message);
        unauthorizedIntent.putExtra(LIVE_IP, ip);
        unauthorizedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        splashViewModel.checkVersion(macAddress, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME, BuildConfig.APPLICATION_ID).observe(this, appVersionInfos -> {
            if (appVersionInfos != null && !appVersionInfos.isEmpty()) {
                for (AppVersionInfo appVersionInfo : appVersionInfos) {
                    if (appVersionInfo.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        compareVersion(appVersionInfo);
                    }
                    if (appVersionInfo.getPackageName().equalsIgnoreCase(ACCOUNT_PACKAGE))
                        accountDownloadLink = appVersionInfo.getApkDownloadLink();
                }
            }
        });
    }

    private void compareVersion(AppVersionInfo appVersionInfo) {
        this.appVersionInfo = appVersionInfo;
        if (appVersionInfo.getUpdate()) {
            permissionutils.check_permission(permissions, getString(R.string.request_permissions), UPDATE_ID);
        } else {
//          versionPresenter.checkMacDetails(macAddress);
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
        }
    }

    private void checkValidUser() {
        splashViewModel.checkIfUserRegistered(macAddress).observe(this, userCheckInfo -> {
            if (userCheckInfo != null) {
                if (userCheckInfo.getData() != null && (userCheckInfo.getData().getActivationStatus() == 1 && userCheckInfo.getData().getIsActive() == 1)) {
                    if (fetchTokenAndLoginCredentialsFromProvider() != null) {
                        //getSession using userEmail and boxID
                    } else {
                        showLogin("", "");
                    }
                } else {
                    if (userCheckInfo.getStatus() == 401 || userCheckInfo.getStatus() == 402) {
                        openAccountApk(ACCOUNT_PACKAGE, accountDownloadLink);
                    } else
                        loadUnauthorized(userCheckInfo.getErrror(), userCheckInfo.getMessage(), "N/A");

                }
            }
        });
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

    private void showLogin(String userEmail, String userPassword) {

        Intent loginIntent = new Intent(this, LoginActivity.class);
        if (!userEmail.isEmpty() || !userPassword.isEmpty()) {
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.putExtra(USER_EMAIL, userEmail);
            loginIntent.putExtra(USER_PASSWORD, userPassword);
            startActivity(loginIntent);
        }

    }

    private UserLoginData fetchTokenAndLoginCredentialsFromProvider() {
        return null;
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> pending_permissions) {
        permissionutils.check_permission(pending_permissions, getString(R.string.request_permissions), request_code);

    }

    @Override
    public void PermissionDenied(int request_code) {
        switch (request_code) {
            case UPDATE_ID:
                checkValidUser();
                break;
        }

    }

    @Override
    public void NeverAskAgain(int request_code) {
        switch (request_code) {
            case UPDATE_ID:
                checkValidUser();
                break;
        }

    }

    @Override
    public void onDismissBtnClicked() {
        checkValidUser();
    }
}
