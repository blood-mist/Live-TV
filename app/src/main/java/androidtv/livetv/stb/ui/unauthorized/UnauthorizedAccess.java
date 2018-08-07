package androidtv.livetv.stb.ui.unauthorized;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.utils.AppConfig;
import androidtv.livetv.stb.utils.DeviceUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_CODE;
import static androidtv.livetv.stb.utils.LinkConfig.LIVE_ERROR_MESSAGE;


public class UnauthorizedAccess extends Activity {
    @BindView(R.id.txt_msg)
     TextView messageView;

    @BindView(R.id.username_txt)
     TextView txt_username;

    @BindView(R.id.box_id_txt)
     TextView boxid;

    @BindView(R.id.ip_address_txtvw)
     TextView ipAddress_txt;

    @BindView(R.id.error_code_txt)
     TextView errorCode_txt;

    @BindView(R.id.exit_button)
     Button retry;

    private Button finishBut;
    private String username, macAddress = "", error_code = "", error_message, ipAddress = "";
    private StringBuilder mb = new StringBuilder();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.unauthorized_access);
        ButterKnife.bind(this);
        error_code = getIntent().getStringExtra(LIVE_ERROR_CODE);
        error_message = getIntent().getStringExtra(LIVE_ERROR_MESSAGE);

        retry.requestFocus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        error_message = getAppropiateErrorMessage(error_code);
        if (AppConfig.isDevelopment()) {
            macAddress = AppConfig.getMac();
        } else {
            macAddress = DeviceUtils.getMac(this); // Getting mac addresss
        }
        boxid.setText("Box id: " + macAddress);
        errorCode_txt.setText("ERROR CODE: " + error_code);
        messageView.setText(error_message);
        Timber.e(error_code + "code", error_message + "");
        try {
            username = getIntent().getStringExtra("username");
            System.out.println(username);
            if (username == null || username.equals("null")|| username.isEmpty()) {
                System.out.println("I am here");
                txt_username.setVisibility(View.INVISIBLE);

            } else {
                txt_username.setVisibility(View.VISIBLE);
                txt_username.setText("username: " + username);

            }
//			txt_username.setVisibility(View.VISIBLE);
            Timber.e("username", username);
        } catch (Exception e) {
            txt_username.setVisibility(View.INVISIBLE);
        }

        try {
            ipAddress = getIntent().getStringExtra("ipAddress");
            if (ipAddress == null ||ipAddress.isEmpty()) {
                ipAddress = "N/A";
//                mb.append("Ip Adress:\t").append(ipAddress).append("\n\n");
            }
            ipAddress_txt.setText("IP Address: " + ipAddress);
        } catch (Exception e) {
            Timber.e(e);

            ipAddress_txt.setVisibility(View.INVISIBLE);
        }

        if (error_code.equals(""))
            errorCode_txt.setVisibility(View.INVISIBLE);

        else
            /*mb.append("Box ID: " + macAddress.toUpperCase())
                    .append("\n")
                    .append("LoginError Code: " + error_code)
                    .append("\n")
                    .append(error_message);*/
            errorCode_txt.setText("LoginError Code: " + error_code);

//        messageView.setText(mb.toString());
        retry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }


    private String getAppropiateErrorMessage(String error_code) {
        switch (error_code) {
            case "401":
                error_message = getString(R.string.user_not_registered);
                break;
            case "402":
                error_message = getString(R.string.user_not_active);
                break;
            case "403":
                error_message = getString(R.string.user_not_approved);
                break;
            case "404":
                error_message = getString(R.string.mac_not_registered);
                break;
        }
        return error_message;
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }
}
