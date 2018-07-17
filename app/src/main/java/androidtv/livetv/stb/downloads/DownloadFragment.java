package androidtv.livetv.stb.downloads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.Download;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_FRAGMENT;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_ID;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_LINK;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.MESSAGE_ERROR;
import static androidtv.livetv.stb.utils.LinkConfig.MESSAGE_PROGRESS;


public class DownloadFragment extends Fragment {

    private static final String APP_TITLE = "header_title";
    private static final String DOWNLOAD_MESSAGE = "download_message";
    private static final String FRAGMENT_STATUS = "fragment_status";
    @BindView(R.id.progress_download)
    ProgressBar downloadProgressBar;

    @BindView(R.id.downloadTextView)
    TextView downloadTextView;
    @BindView(R.id.fragment_header_textView)
    TextView headerTextView;
    @BindView(R.id.download_percent)
    TextView downloadPercentTextView;
    @BindView(R.id.btn_download)
    Button downloadBtn;
    @BindView(R.id.btn_dismiss)
    Button dismissBtn;
    @BindView(R.id.download_or_update_layout)
    LinearLayout downloadOptionLayout;
    private String headerString, bodyString, downloadLink;
    private int fragment_status;
    private OnDismissInteraction onDismissInteraction;


    public static DownloadFragment newInstance(String title, String message, int status) {
        DownloadFragment fragment = new DownloadFragment();
        Bundle args = new Bundle();
        args.putString(APP_TITLE, title);
        args.putString(DOWNLOAD_MESSAGE, message);
        args.putInt(FRAGMENT_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        headerString = getArguments().getString(APP_TITLE);
        bodyString = getArguments().getString(DOWNLOAD_MESSAGE);
        fragment_status = getArguments().getInt(FRAGMENT_STATUS);
        try {
            downloadLink = getArguments().getString(DOWNLOAD_LINK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        registerReciever();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void registerReciever() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        intentFilter.addAction(MESSAGE_ERROR);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View downloadView = inflater.inflate(R.layout.fragment_download, container, false);
        ButterKnife.bind(this,downloadView);
        return downloadView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
    }

    private void findViews() {
        downloadBtn.requestFocus();


        headerTextView.setText(headerString);
        switch (fragment_status) {
            case 1:
                downloadTextView.setText(bodyString);
                downloadProgressBar.setVisibility(View.GONE);
                downloadPercentTextView.setVisibility(View.GONE);
                downloadOptionLayout.setVisibility(View.VISIBLE);
                break;

            case 2:
                downloadTextView.setText(bodyString);
                downloadProgressBar.setVisibility(View.VISIBLE);
                downloadPercentTextView.setVisibility(View.VISIBLE);
                downloadOptionLayout.setVisibility(View.GONE);
                break;
        }
        downloadBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), DownloadService.class);
            intent.putExtra(DOWNLOAD_LINK, downloadLink);
            intent.putExtra(DOWNLOAD_NAME, getString(R.string.app_name) + ".apk");
            intent.putExtra(DOWNLOAD_ID, 1);
            getActivity().startService(intent);
            downloadTextView.setText(R.string.downloading);
            downloadProgressBar.setVisibility(View.VISIBLE);
            downloadPercentTextView.setVisibility(View.VISIBLE);
            downloadOptionLayout.setVisibility(View.GONE);
        });

        dismissBtn.setOnClickListener(view -> {
            Fragment frament = DownloadFragment.this;
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(frament).commit();
            onDismissInteraction.onDismissBtnClicked();

        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDismissInteraction)
            onDismissInteraction = (OnDismissInteraction) context;
        else throw new RuntimeException("Must Implement OnDismissInterActionListener ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getActivity());
        bManager.unregisterReceiver(broadcastReceiver);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(MESSAGE_PROGRESS)) {

                Download download = intent.getParcelableExtra("download");
                downloadProgressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context, R.color.red_selector), PorterDuff.Mode.SRC_IN);
                downloadProgressBar.setProgress(download.getProgress());
                if (download.getProgress() == 100) {
                    downloadTextView.setText(R.string.download_complete);
                    Fragment toRemoveFragment = getActivity().getSupportFragmentManager().findFragmentByTag(DOWNLOAD_FRAGMENT);
                    if (toRemoveFragment != null)
                        getActivity().getSupportFragmentManager().beginTransaction().remove(toRemoveFragment).commit();
                    getActivity().finish();
                } else {
                    int downloadPercent = (int) (download.getCurrentFileSize() / download.getTotalFileSize() * 100);
                    downloadPercentTextView.setText(downloadPercent + "%");
                    downloadTextView.setText("Downloading...");
//                    downloadTextView.setText("Downloading file " + new DecimalFormat("##.##").format(download.getCurrentFileSize()) + "/" + new DecimalFormat("##.##").format(download.getTotalFileSize()) + "MB");
                }
            } else if (intent.getAction().equals(MESSAGE_ERROR)) {
                //show error fragment;
            }
        }
    };

    public static DownloadFragment newInstance(String title, String message, int status, String apkDownloadLink) {
        DownloadFragment fragment = new DownloadFragment();
        Bundle args = new Bundle();
        args.putString(APP_TITLE, title);
        args.putString(DOWNLOAD_MESSAGE, message);
        args.putInt(FRAGMENT_STATUS, status);
        args.putString(DOWNLOAD_LINK, apkDownloadLink);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnDismissInteraction {
        void onDismissBtnClicked();
    }
}
