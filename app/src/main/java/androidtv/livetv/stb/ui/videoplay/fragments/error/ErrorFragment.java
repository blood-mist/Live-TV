package androidtv.livetv.stb.ui.videoplay.fragments.error;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.PlayBackErrorEntity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ErrorFragment extends Fragment {

    @BindView(R.id.btn_negative)
    Button btnNegative;

    @BindView(R.id.btn_positive)
    Button btnPositive;

    @BindView(R.id.txt_error_msg)
    TextView txtErrorMessage;

    @BindView(R.id.txt_error_code)
    TextView txtErrorCode;

    @BindView(R.id.error)
    TextView error;

    public PlayBackErrorEntity getPlayBackErrorEntity() {
        return playBackErrorEntity;
    }

    public void setPlayBackErrorEntity(PlayBackErrorEntity playBackErrorEntity) {
        this.playBackErrorEntity = playBackErrorEntity;
    }

    private PlayBackErrorEntity playBackErrorEntity;

    public ErrorFragment() {
        // Required empty public constructor
    }


    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_error, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpError(playBackErrorEntity);

    }

    private void checkErrorType(int errorType) {
        switch(errorType){
            case 1:
                txtErrorCode.setText(getString(R.string.err_code_media_error));
                txtErrorMessage.setText(getString(R.string.err_media_error));
                break;

            case 2:
                txtErrorCode.setText(getString(R.string.err_code_server_unreachable));
                txtErrorMessage.setText(getString(R.string.err_server_unreachable));
                break;

            default:
                txtErrorCode.setText("UNKNOWN");
                txtErrorMessage.setText("Unknown error occured.Please , try again later!." +
                        "Sorry for inconvenience.");
                break;


        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }

    public void setUpError(PlayBackErrorEntity upError) {
       txtErrorCode.setText(upError.getErrorCode());
       txtErrorMessage.setText(upError.getErrorMessage());
    }


}
