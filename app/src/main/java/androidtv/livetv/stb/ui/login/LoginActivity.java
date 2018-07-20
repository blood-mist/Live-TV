package androidtv.livetv.stb.ui.login;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
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
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DeviceUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

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
        loginViewModel.performLogin(username,passsword.equals("")?txtPasssword.getText().toString():passsword,macAddress ).observe(this,loginInfo -> {
            if(loginInfo!=null){
               loginViewModel.getLoginInfoFromDB().observe(this,login1 -> {
                   if(login1!=null)
                   Toast.makeText(this,"userNamewhenOffline is"+login1.getEmail(),Toast.LENGTH_LONG).show();
               });
            }
        });

    }
}
