package androidtv.livetv.stb.ui.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import androidtv.livetv.stb.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity  extends AppCompatActivity{

    @BindView(R.id.userNamePS)
    TextView txtUsername;

    @BindView(R.id.passWordPS)
    EditText txtPasssword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }
}
