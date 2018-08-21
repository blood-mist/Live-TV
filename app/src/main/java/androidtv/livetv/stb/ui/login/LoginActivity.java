package androidtv.livetv.stb.ui.login;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CatChannelWrapper;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelInserted;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.LoginError;
import androidtv.livetv.stb.entity.LoginResponseWrapper;
import androidtv.livetv.stb.ui.splash.SplashActivity;
import androidtv.livetv.stb.ui.unauthorized.UnauthorizedAccess;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.VideoPlayActivity;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.CustomDialogManager;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static androidtv.livetv.stb.utils.LinkConfig.INVALID_HASH;
import static androidtv.livetv.stb.utils.LinkConfig.INVALID_USER;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_CODE;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_MESSAGE;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_IP;
import static androidtv.livetv.stb.utils.LinkConfig.NO_CONNECTION;
import static androidtv.livetv.stb.utils.LinkConfig.USER_EMAIL;

public class LoginActivity extends AppCompatActivity {


    @BindView(R.id.userNamePS)
    TextView txtUsername;

    @BindView(R.id.passWordPS)
    EditText txtPasssword;

    @BindView(R.id.login_loader)
    AVLoadingIndicatorView loginLoader;

    @BindView(R.id.loginButtonPS)
    Button login;

    private LoginViewModel loginViewModel;
    private String username;
    private String macAddress;
    private CatChannelInfo catChannelInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        Bundle loginBundle = getIntent().getExtras();
        if (loginBundle != null) {
            username = loginBundle.getString(USER_EMAIL, "");
        }
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        macAddress = AppConfig.isDevelopment() ? AppConfig.getMac() : DeviceUtils.getMac(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        txtUsername.setText(username);
        login.setOnClickListener(view -> initLogin());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChannelInserted event) {
        if (!event.isInserted()) {
            loadChannelActivity();
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.err_unexpected), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void initLogin() {
        loginLoader.smoothToShow();
       LiveData<LoginResponseWrapper> loginResponseData= loginViewModel.performLogin(username, txtPasssword.getText().toString(), macAddress);
       loginResponseData.observe(this, new Observer<LoginResponseWrapper>() {
            @Override
            public void onChanged(@Nullable LoginResponseWrapper loginResponseWrapper) {
                if (loginResponseWrapper != null) {
                    if (loginResponseWrapper.getLoginInfo() != null) {
                        GlobalVariables.login = loginResponseWrapper.getLoginInfo().getLogin();
                        long utc = GetUtc.getInstance().getTimestamp().getUtc();
                        LoginActivity.this.fetchChannelDetails(loginResponseWrapper.getLoginInfo().getLogin().getToken(), utc,
                                loginResponseWrapper.getLoginInfo().getLogin().getId(), LinkConfig.getHashCode(String.valueOf(loginResponseWrapper.getLoginInfo().getLogin().getId()),
                                        String.valueOf(utc), loginResponseWrapper.getLoginInfo().getLogin().getSession()));
                    } else if (loginResponseWrapper.getLoginInvalidResponse() != null) {
                        if (loginResponseWrapper.getLoginInvalidResponse().getLoginInvalidData().getErrorCode().equals("404")) {
                            LoginActivity.this.loadUnauthorized(LoginActivity.this.getString(R.string.mac_not_registered), "N/A");
                        } else {
                            Toast.makeText(LoginActivity.this, loginResponseWrapper.getLoginInvalidResponse().getLoginInvalidData().getMessage(), Toast.LENGTH_LONG).show();
                            loginLoader.smoothToHide();
                            txtPasssword.requestFocus();
                        }

                    } else {
                        LoginActivity.this.showLoginErrorDialog(loginResponseWrapper.getLoginErrorResponse().getError());
                    }
                    loginResponseData.removeObserver(this);


                }
            }
        });

    }

    private void loadUnauthorized(String error_message, String ip) {
        Intent unauthorizedIntent = new Intent(this, UnauthorizedAccess.class);
        unauthorizedIntent.putExtra(LIVE_ERROR_CODE, "404");
        unauthorizedIntent.putExtra(LIVE_ERROR_MESSAGE, error_message);
        unauthorizedIntent.putExtra(LIVE_IP, ip);
        unauthorizedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(unauthorizedIntent);
        finish();
    }

    private void showLoginErrorDialog(LoginError error) {
        loginLoader.smoothToHide();
        CustomDialogManager.loginErrorDialog(this, error);
    }

    private void fetchChannelDetails(String token, long utc, int id, String hashCode) {
        LiveData<CatChannelWrapper> categoryChannelData = loginViewModel.fetchChannelDetails(token, String.valueOf(utc), String.valueOf(id), hashCode);
        categoryChannelData.observe(this, new Observer<CatChannelWrapper>() {
            @Override
            public void onChanged(@Nullable CatChannelWrapper catChannelWrapper) {
                if (catChannelWrapper != null) {
                    if (catChannelWrapper.getCatChannelInfo() != null) {
                        Timber.d(catChannelWrapper.getCatChannelInfo().getCategory().size() + "");
                        loginLoader.smoothToHide();
                        LoginActivity.this.catChannelInfo = catChannelWrapper.getCatChannelInfo();
                        Thread thread=new Thread(new Runnable() {
                            @Override
                            public void run() {
                                checkForExistingChannelData();
                            }
                        });
                        thread.start();

                    } else {
                        switch (catChannelWrapper.getCatChannelError().getStatus()) {
                            case INVALID_HASH:
                                LoginActivity.this.showErrorDialog(INVALID_HASH, LoginActivity.this.getString(R.string.session_expired));
                                break;
                            case INVALID_USER:
                                LoginActivity.this.showErrorDialog(INVALID_USER, catChannelWrapper.getCatChannelError().getErrorMessage());
                                break;
                            case NO_CONNECTION:
                                showErrorDialog(NO_CONNECTION,getString(R.string.no_internet_body));
                        }
                    }
                    categoryChannelData.removeObserver(this);
                }


            }
        });
    }

    private void checkForExistingChannelData() {
        LiveData<Integer> channelSize = loginViewModel.checkChannelsInDB();
        channelSize.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
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

    private void insertDataToDB() {
        saveChannelDetailstoDb(catChannelInfo.getCategory(), catChannelInfo.getChannel());
    }

    private void fetchChannelsFromDBtoUpdate() {
        LiveData<List<ChannelItem>>channelDBdata=   loginViewModel.getAllChannelsInDBToCompare();
        channelDBdata.observe(this, new Observer<List<ChannelItem>>() {
            @Override
            public void onChanged(@Nullable List<ChannelItem> channelItemList) {
                if (channelItemList != null) {
                    updateListData(channelItemList, catChannelInfo.getChannel());
                    channelDBdata.removeObserver(this);
                }
            }
        });
    }

    private void updateListData(List<ChannelItem> channelItemList, List<ChannelItem> channels) {
        for (int i = 0; i < channels.size(); i++) {
            for (ChannelItem dbCHannelItem : channelItemList) {
                if (channels.get(i).getId() == dbCHannelItem.getId()) {
                    channels.get(i).setIs_fav(dbCHannelItem.getIs_fav());
                }
            }
        }
        saveChannelDetailstoDb(catChannelInfo.getCategory(), channels);

    }

    private void saveChannelDetailstoDb(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        loginViewModel.insertCatChannelToDB(categoryList, channelList);
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
                    showSplash();
                    break;
                case NO_CONNECTION:
                    Intent intent=new Intent(this,SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

            }

        });
        splashError.show();
    }

    private void showSplash() {
        Intent splashIntent = new Intent(this, SplashActivity.class);
        splashIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(splashIntent);
        finish();

    }

    private void showLogin(String email) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.putExtra(USER_EMAIL, email);
        startActivity(loginIntent);
        finish();

    }

    private void loadChannelActivity() {
        Intent channelLoadIntent = new Intent(this, VideoPlayActivity.class);
        channelLoadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(channelLoadIntent);
        loginLoader.smoothToHide();
        finish();
    }
}
