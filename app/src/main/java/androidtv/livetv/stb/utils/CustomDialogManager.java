package androidtv.livetv.stb.utils;

/**
 * Mostly used as follows:
 * Dismiss Button --> extra
 * Next Button -----> negative
 * Prev Button -----> positive
 * Settings--------->neutral
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.ui.splash.SplashActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class CustomDialogManager {

    public static final int DEFAULT = 0;
    public static final int LOADING = 1;
    public static final int PROGRESS = 2;
    public static final int ALERT = 3;
    public static final int MESSAGE = 4;
    public static final int WARNING = 5;

    private Context context = null;
    private String version = "", macAddress = "", title = "", message = "", error_code = "";

    private Dialog d;
    @BindView(R.id.dialog_heading)
    TextView alertTitle;
    @BindView(R.id.txt_error_code)
    TextView errorCodeTextView;
    @BindView(R.id.macaddress_variable)
    TextView MacTextView;
    @BindView(R.id.app_version_variable)
    TextView versionTextView;
    @BindView(R.id.message)
    TextView messageTextView;
    @BindView(R.id.macaddress_fixed)
    TextView MacTextViewFixed;
    @BindView(R.id.app_version_fixed)
    TextView versionTextViewFixed;

    @BindView(R.id.mac_version)
    LinearLayout macAndVersion;

    @BindView(R.id.button_layout)
    LinearLayout buttonLayout;

    @BindView(R.id.negative)
     Button negative;
    @BindView(R.id.positive)
     Button positive;
    @BindView(R.id.neutral)
     Button neutral;
    @BindView(R.id.error_image)
     ImageView error_image;


    @BindView(R.id.progressBar)
     ProgressBar progressBar;

    @BindView(R.id.view_above_button)
     View viewAboveButtons;

    @BindView(R.id.custom_progress_bar)
     AVLoadingIndicatorView progressBarLayout;

    @BindView(R.id.custom_dialog_layout)
     FrameLayout custom_dialog_layout;

    @BindView(R.id.closeButton)
    ImageButton close_btn;

    private Typeface light, medium, semibold, regular;

    private int type = DEFAULT;

    /**
     * mostly loading dialog
     *
     * @param context
     * @param type
     */
    public CustomDialogManager(Context context, int type) {
        this.context = context;
        this.type = type;
        this.title = context.getString(R.string.app_name);
        this.message = context.getString(R.string.err_unexpected);
    }

    /**
     * Dialog with app name as title
     *
     * @param context
     * @param message
     * @param type
     */
    public CustomDialogManager(Context context, String message, int type) {
        this.context = context;
        this.type = type;
        this.message = message;
        this.title = context.getString(R.string.app_name);
    }

    /**
     * @param context
     * @param title
     * @param message
     * @param type
     */
    public CustomDialogManager(Context context, String title, String message,
                               int type) {
        this.context = context;
        this.type = type;
        this.title = title;
        this.message = message;
    }

    public void build() {
        d = new Dialog(context);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        d.getWindow().setDimAmount(0.8f);
        d.setContentView(R.layout.custom_dialog);
        d.setCancelable(true);
        ButterKnife.bind(this, d);
        setTypeFace();

        hidePriorDialogUI();


        setDialogTypeSetting(type);


    }

    private void setTypeFace() {
        light = Typeface.createFromAsset(context.getAssets(), "font/Exo2-Light.otf");
        medium = Typeface.createFromAsset(context.getAssets(), "font/Exo2-Medium.otf");
        semibold = Typeface.createFromAsset(context.getAssets(), "font/Exo2-SemiBold.otf");
        regular = Typeface.createFromAsset(context.getAssets(), "font/Exo2-Regular_0.otf");
        alertTitle.setText(title);
        alertTitle.setTypeface(semibold);
        MacTextViewFixed.setTypeface(semibold);
        versionTextViewFixed.setTypeface(semibold);
        MacTextView.setTypeface(light);
        versionTextView.setTypeface(light);
    }

    private void hidePriorDialogUI() {
        macAndVersion.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        error_image.setVisibility(View.GONE);
        viewAboveButtons.setVisibility(View.GONE);
        //viewBelowMac.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.GONE);
        positive.setVisibility(View.GONE);
        neutral.setVisibility(View.GONE);
        // extra.setVisibility(View.GONE);
        negative.setVisibility(View.GONE);
        close_btn.setVisibility(View.GONE);
    }

    private void setDialogTypeSetting(int type) {
        if (type == LOADING) {
       /*     alertTitle.setVisibility(View.GONE);
            error_image.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            viewBelowMac.setVisibility(View.GONE);
            this.message = userAccountActivity.getString(R.string.txt_loading);
            messageTextView.setText(this.message + "");
            setErrorCode("null");*/
            progressBarLayout.setVisibility(View.VISIBLE);
            custom_dialog_layout.setVisibility(View.INVISIBLE);
        } else if (type == PROGRESS) {
            progressBarLayout.setVisibility(View.INVISIBLE);
            custom_dialog_layout.setVisibility(View.VISIBLE);
            alertTitle.setVisibility(View.VISIBLE);
            macAndVersion.setVisibility(View.GONE);
            viewAboveButtons.setVisibility(View.GONE);
            error_image.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            messageTextView.setText(this.message + "");
            setErrorCode(this.error_code);
        } else if (type == ALERT) {
            progressBarLayout.setVisibility(View.INVISIBLE);
            custom_dialog_layout.setVisibility(View.VISIBLE);
            alertTitle.setVisibility(View.VISIBLE);
            error_image.setImageResource(R.drawable.error);
            error_image.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            messageTextView.setText(this.message + "");
            setErrorCode(this.error_code);
        } else if (type == WARNING) {
            progressBarLayout.setVisibility(View.INVISIBLE);
            custom_dialog_layout.setVisibility(View.VISIBLE);
            alertTitle.setVisibility(View.VISIBLE);
            error_image.setImageResource(R.drawable.warning);
            error_image.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            messageTextView.setText(this.message + "");
            setErrorCode(this.error_code);
        } else if (type == MESSAGE) {
            progressBarLayout.setVisibility(View.INVISIBLE);
            custom_dialog_layout.setVisibility(View.VISIBLE);
            alertTitle.setVisibility(View.VISIBLE);
            macAndVersion.setVisibility(View.GONE);
            error_image.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            messageTextView.setText(this.message + "");
            setErrorCode(this.error_code);
        }
    }

    public void dismissDialogOnBackPressed() {
        d.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        d.dismiss();
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    public void closeAppOnBackPressed() {
        d.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        d.dismiss();
                        System.exit(0);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    public Dialog getInnerObject() {
        return d;
    }

    public void finishActivityOnBackPressed(final Context context) {
        d.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        d.dismiss();
                        ((Activity) context).finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public Button setPositiveButton(String btn_text,
                                    View.OnClickListener onClickListener) {
        viewAboveButtons.setVisibility(View.VISIBLE);
        buttonLayout.setVisibility(View.VISIBLE);
        positive.setText(btn_text);
        positive.setVisibility(View.VISIBLE);
        positive.setOnClickListener(onClickListener);
        return positive;

    }

    public Button setNeutralButton(String btn_text,
                                   View.OnClickListener onClickListener) {
        viewAboveButtons.setVisibility(View.VISIBLE);
        buttonLayout.setVisibility(View.VISIBLE);
        neutral.setText(btn_text);
        neutral.setVisibility(View.VISIBLE);
        neutral.setOnClickListener(onClickListener);
        return neutral;

    }

    public Button setNegativeButton(String btn_text,
                                    View.OnClickListener onClickListener) {
        viewAboveButtons.setVisibility(View.VISIBLE);
        buttonLayout.setVisibility(View.VISIBLE);
        negative.setText(btn_text);
        negative.setVisibility(View.VISIBLE);
        negative.setOnClickListener(onClickListener);
        return negative;

    }

    public String getTitle() {
        return alertTitle.getText() + "";
    }

    public void setTitle(String title) {
        this.title = title;
        /**
         * if set title is done before build it should be found again
         */
        alertTitle = (TextView) d.findViewById(R.id.dialog_heading);
        alertTitle.setText(title);
        alertTitle.setVisibility(View.VISIBLE);
    }

    public void setMessage(String error_code, String message) {
        this.message = message;
        this.error_code = error_code;
        /**
         * if set message is done before build it should be found again
         */

        messageTextView = (TextView) d.findViewById(R.id.message);
        messageTextView.setText(message);
        messageTextView.setTypeface(light);
        setErrorCode(error_code);

    }

    public void showMacAndVersion() {
        if (AppConfig.isDevelopment()) {
            macAddress = AppConfig.getMac();
        } else {
            macAddress = DeviceUtils.getMac(context);
        }
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (NameNotFoundException e) {
            version = "N/A";
            Timber.e(e);
        }
        MacTextView.setText(macAddress);
        versionTextView.setText(version);

        macAndVersion.setVisibility(View.VISIBLE);
//        viewBelowMac.setVisibility(View.VISIBLE);
    }

    public void show() {
        d.show();
    }

    public void hide() {
        d.hide();
    }

    public boolean isShowing() {
        /**
         * if build is not done then handle exception for dialog
         */
        try {
            return d.isShowing();
        } catch (Exception e) {
            return false;
        }
    }

    public Button getNeutralButton() {
        return neutral;
    }


    public void finishActivityonDismissPressed(final Context context) {

        setExtraButton(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                        ((Activity) context).finish();

                    }
                });
    }

    public ImageButton setExtraButton(
            View.OnClickListener onClickListener) {
        // viewAboveButtons.setVisibility(View.VISIBLE);
//        buttonLayout.setVisibility(View.VISIBLE);
//        extra.setText(btn_text);
        //extra.setVisibility(View.VISIBLE);
        close_btn.setVisibility(View.VISIBLE);
        close_btn.setOnClickListener(onClickListener);
        return close_btn;

    }

    // end of re used custom Dialog

    public void addDissmissButtonToDialog() {
        setExtraButton(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (context.getClass().getName()
                                .equals(
                                        SplashActivity.class.getName())
                                )

                            ((Activity) context).finish();
                    }
                });

    }

    public void dismiss() {
        d.dismiss();
    }

    public void setErrorCode(String error_code) {
        this.error_code = error_code;
        errorCodeTextView = (TextView) d.findViewById(R.id.txt_error_code);
        if (error_code.equalsIgnoreCase("null") || error_code.equals("")) {
            errorCodeTextView.setVisibility(View.GONE);
        } else {
            errorCodeTextView.setText("Error Code:" + error_code);
        }
    }

    public static class ReUsedCustomDialogs {
        // Dialog to show when required data not available
        public static CustomDialogManager showDataNotFetchedAlert(final Context context) {
            final CustomDialogManager error = new CustomDialogManager(context,
                    CustomDialogManager.ALERT);
            error.build();
            error.showMacAndVersion();
            error.setMessage("null", context.getString(R.string.err_json_exception));
            error.addDissmissButtonToDialog();
            if (context
                    .getClass()
                    .getName()
                    .equals(SplashActivity.class.getName())
                    ) {
                error.setNegativeButton(context.getString(R.string.btn_reconnect), new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        error.dismiss();
                        Intent i = ((ContextWrapper) context)
                                .getBaseContext()
                                .getPackageManager()
                                .getLaunchIntentForPackage(
                                        ((ContextWrapper) context).getBaseContext()
                                                .getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);

                        ((Activity) context).finish();

                    }
                });
            }
            error.show();
            return error;
        }

       /* public static CustomDialogManager showLoginError(final Context context) {
            final CustomDialogManager error = new CustomDialogManager(context,
                    CustomDialogManager.ALERT);
            error.build();
            error.showMacAndVersion();
            error.setMessage("null", context.getString(R.string.err_file_data_does_not_match));
            error.addDissmissButtonToDialog();
            if (context
                    .getClass()
                    .getName()
                    .equals(EntryPoint.class.getName())
                    ) {
                error.setNegativeButton(context.getString(R.string.btn_ok), new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        error.dismiss();
                        Intent i = ((ContextWrapper) context)
                                .getBaseContext()
                                .getPackageManager()
                                .getLaunchIntentForPackage(
                                        ((ContextWrapper) context).getBaseContext()
                                                .getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);

                        ((Activity) context).finish();

                    }
                });
            }
            error.show();
            return error;
        }*/


        public static CustomDialogManager noInternet(final Context context) {
            final CustomDialogManager noInternet = new CustomDialogManager(context, CustomDialogManager.WARNING);
            noInternet.build();
            noInternet.getInnerObject().setCancelable(true);
            noInternet.setTitle(context.getString(R.string.no_internet_title));
            noInternet.setMessage("E 025", context.getString(R.string.no_internet_body));
            noInternet.show();

            noInternet.addDissmissButtonToDialog();

            noInternet.setNeutralButton(context.getString(R.string.btn_settings), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    noInternet.dismiss();
                    //  MarketAppDetailParser.openApk(context, MarketAppDetailParser.MyNITVSettings);

                    try {
                        try {

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.rk_itvui.settings", "com.rk_itvui.settings.Settings"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        } catch (Exception e) {
                            try {
                                Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage("com.giec.settings");
                                context.startActivity(LaunchIntent);
                                ((Activity) context).finish();
                            } catch (Exception c) {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                ((Activity) context).finish();
                            }
                        }


                    } catch (Exception a) {
                        context.startActivity(
                                new Intent(Settings.ACTION_SETTINGS));
                        ((Activity) context).finish();
                    }
                }
            });

//Relaunch Settings Dismiss .....
            if (context.getClass().getName().equals(SplashActivity.class.getName())) {

                noInternet.finishActivityonDismissPressed(context);
                noInternet.finishActivityOnBackPressed(context);

                noInternet.setPositiveButton(context.getString(R.string.btn_reconnect), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        noInternet.dismiss();
                        context.startActivity(i);
                        ((Activity) context).finish();

                    }
                });

                noInternet.getInnerObject().setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                        ((Activity) context).finish();
                    }

                });
            }
            return noInternet;
        }

        public static CustomDialogManager featureNotAvailable(Context context) {
            CustomDialogManager featureNotAvailable = new CustomDialogManager(context, CustomDialogManager.MESSAGE);
            featureNotAvailable.build();
            featureNotAvailable.showMacAndVersion();
            featureNotAvailable.setMessage("null", context.getString(R.string.msg_feature_not_available));
            featureNotAvailable.addDissmissButtonToDialog();
            featureNotAvailable.dismissDialogOnBackPressed();
            featureNotAvailable.getInnerObject().setCancelable(true);
            featureNotAvailable.show();
            return featureNotAvailable;
        }


    }


    public static void dataNotFetched(final Context context) {
        final CustomDialogManager error = new CustomDialogManager(context, CustomDialogManager.ALERT);
        error.build();
        error.showMacAndVersion();
        error.setMessage("null", context.getString(R.string.err_json_exception));
        error.setExtraButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                error.dismiss();
                ((Activity) context).finish();

            }
        });
        error.show();
    }

}
