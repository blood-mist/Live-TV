package androidtv.livetv.stb.ui.login;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.VideoPlayActivity;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DeviceUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static androidtv.livetv.stb.utils.LinkConfig.USER_EMAIL;
import static androidtv.livetv.stb.utils.LinkConfig.USER_PASSWORD;

public class LoginActivity  extends AppCompatActivity{

    @BindView(R.id.userNamePS)
    TextView txtUsername;

    @BindView(R.id.passWordPS)
    EditText txtPasssword;

    @BindView(R.id.login_loader)
    AVLoadingIndicatorView loginLoader;

    @BindView(R.id.loginButtonPS)
    Button login;

    private LoginViewModel loginViewModel;
    private String username,passsword;
    private String macAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        Bundle loginBundle= getIntent().getExtras();
        if(loginBundle!=null){
            username=loginBundle.getString(USER_EMAIL,"");
            passsword=loginBundle.getString(USER_PASSWORD,"");
        }
        macAddress= AppConfig.isDevelopment()?AppConfig.getMac():DeviceUtils.getMac(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        txtUsername.setText(username);
        loginViewModel= ViewModelProviders.of(this).get(LoginViewModel.class);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initLogin();
            }
        });

    }

    private void initLogin() {
        loginLoader.smoothToShow();
        loginViewModel.performLogin(username,passsword.equals("")?txtPasssword.getText().toString():passsword,macAddress ).observe(this,loginInfo -> {
            if(loginInfo!=null){
               loginViewModel.getLoginInfoFromDB().observe(this,login1 -> {
                   if(login1!=null) {
                       GlobalVariables.login = login1;
                       Toast.makeText(this, "userNamewhenOffline is" + login1.getEmail(), Toast.LENGTH_LONG).show();
                       long utc = GetUtc.getInstance().getTimestamp().getUtc();
                       fetchChannelDetails(login1.getToken(), utc, login1.getId(), LinkConfig.getHashCode(String.valueOf(login1.getId()), String.valueOf(utc), login1.getSession()));
                   }

               });
            }
        });

    }

    private void fetchChannelDetails(String token, long utc, int id, String hashCode) {
        loginViewModel.fetchChannelDetails(token, String.valueOf(utc), String.valueOf(id), hashCode).observe(this, catChannelInfo -> {
            if (catChannelInfo != null) {
                Timber.d(catChannelInfo.getCategory().size() + "");
                loadChannelActivity();
            }

        });
    }

    private void loadChannelActivity() {
        Intent channelLoadIntent= new Intent(this,VideoPlayActivity.class);
        channelLoadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(channelLoadIntent);
        loginLoader.smoothToHide();
    }
}
