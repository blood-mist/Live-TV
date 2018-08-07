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

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.LoginError;
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
        txtUsername.setText(username);

        login.setOnClickListener(view -> initLogin());

    }

    private void initLogin() {
        loginLoader.smoothToShow();
        loginViewModel.performLogin(username, txtPasssword.getText().toString(), macAddress).observe(this, loginResponseWrapper -> {
            if (loginResponseWrapper != null) {
                if (loginResponseWrapper.getLoginInfo() != null) {
                    GlobalVariables.login = loginResponseWrapper.getLoginInfo().getLogin();
                    long utc = GetUtc.getInstance().getTimestamp().getUtc();
                    fetchChannelDetails(loginResponseWrapper.getLoginInfo().getLogin().getToken(), utc,
                            loginResponseWrapper.getLoginInfo().getLogin().getId(), LinkConfig.getHashCode(String.valueOf(loginResponseWrapper.getLoginInfo().getLogin().getId()),
                                    String.valueOf(utc), loginResponseWrapper.getLoginInfo().getLogin().getSession()));
                } else if (loginResponseWrapper.getLoginInvalidResponse() != null) {
                    if (loginResponseWrapper.getLoginInvalidResponse().getLoginInvalidData().getErrorCode().equals("404")) {
                        loadUnauthorized(getString(R.string.mac_not_registered), "N/A");
                    } else {
                        Toast.makeText(this, loginResponseWrapper.getLoginInvalidResponse().getLoginInvalidData().getMessage(), Toast.LENGTH_LONG).show();
                       loginLoader.smoothToHide();
                       txtPasssword.requestFocus();
                    }

                } else {
                    showLoginErrorDialog(loginResponseWrapper.getLoginErrorResponse().getError());
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
        loginViewModel.fetchChannelDetails(token, String.valueOf(utc), String.valueOf(id), hashCode).observe(this, catChannelWrapper -> {
            if (catChannelWrapper != null) {
                if (catChannelWrapper.getCatChannelInfo() != null) {
                    Timber.d(catChannelWrapper.getCatChannelInfo().getCategory().size() + "");
                    loginLoader.smoothToHide();
                    this.catChannelInfo=catChannelWrapper.getCatChannelInfo();
                    checkForExistingChannelData();
                }else{
                  switch(catChannelWrapper.getCatChannelError().getStatus()){
                      case INVALID_HASH:
                          showErrorDialog(INVALID_HASH, getString(R.string.session_expired));
                          break;
                      case INVALID_USER:
                          showErrorDialog(INVALID_USER, catChannelWrapper.getCatChannelError().getErrorMessage());
                          break;
                  }
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
        loginViewModel.getAllChannelsInDBToCompare().observe(this, channelItemList -> {
            if (channelItemList != null) {
                updateListData(channelItemList, catChannelInfo.getChannel());
            }
        });
    }

    private void updateListData(List<ChannelItem> channelItemList, List<ChannelItem> channels) {
        Completable.fromRunnable(() -> {
            for(int i=0;i<channels.size();i++) {
                for(ChannelItem dbCHannelItem:channelItemList) {
                    if(channels.get(i).getId()==dbCHannelItem.getId()) {
                        channels.get(i).setIs_fav(dbCHannelItem.getIs_fav());
                    }
                }
            }
            saveChannelDetailstoDb(catChannelInfo.getCategory(), channels);

        }).subscribeOn(Schedulers.io()).subscribe().dispose();
    }

    private void saveChannelDetailstoDb(List<CategoryItem> categoryList, List<ChannelItem> channelList) {
        loginViewModel.insertCatChannelToDB(categoryList, channelList);
        loadChannelActivity();
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
    }
}
