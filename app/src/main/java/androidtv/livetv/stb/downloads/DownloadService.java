package androidtv.livetv.stb.downloads;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import androidtv.livetv.stb.BuildConfig;
import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.Download;
import androidtv.livetv.stb.ui.splash.SplashApiInterface;
import androidtv.livetv.stb.utils.ApiManager;
import androidtv.livetv.stb.utils.LinkConfig;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_ID;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_LINK;
import static androidtv.livetv.stb.utils.LinkConfig.DOWNLOAD_NAME;

public class DownloadService extends Service {
    final static String TAG = DownloadService.class.getSimpleName();
    private static final String DOWNLOAD_THREAD ="download_thread" ;

    String downloadUrl, saveName;
    static int id;

    public DownloadService() {
        super();
    }

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private float totalFileSize;

    public static boolean isDownloading = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        downloadUrl = intent.getStringExtra(DOWNLOAD_LINK);
        saveName = intent.getStringExtra(DOWNLOAD_NAME);
        id = intent.getIntExtra(DOWNLOAD_ID, 0);

        isDownloading = true;

        HandlerThread thread = new HandlerThread(DOWNLOAD_THREAD, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    public static boolean isDownloading(int id) {
        return DownloadService.id == id;
    }

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "onHandleIntent: ");

            //SHOW NOTIFICATION IN WHILE RUNNING BACKGROUND SERVICES
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.live_tv)
                    .setContentTitle(saveName)
                    .setContentText("Downloading: " + saveName)
                    .setProgress(0, 0, true)
                    .setOngoing(true);
            notificationManager.notify(id, notificationBuilder.build());

            initDownload(downloadUrl, id);

        }
    }

    private void initDownload(String downloadUrl, int id) {
        Log.d(TAG, "initDownload: ");


        Retrofit retrofit = ApiManager.getAdapter();

        SplashApiInterface retrofitInterface = retrofit.create(SplashApiInterface.class);

        Call<ResponseBody> request = retrofitInterface.downloadFile(downloadUrl);

        try {
            downloadFile(request.execute().body(), id);
        } catch (IOException e) {
            e.printStackTrace();

            isDownloading = false;
            notificationManager.cancel(id);
//            Intent intent = new Intent(EbookDetailActivity.MESSAGE_ERROR);
//            LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
            Toast.makeText(getApplicationContext(), "Couldnot downlaod", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(ResponseBody body, int id) throws IOException {
        Log.d(TAG, "downloadFile: ");
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+saveName;
        File outputFile = new File(filePath);
        if(!outputFile.exists()){
            outputFile.createNewFile();
        }
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            //Converting byte to mega-byte
            totalFileSize = (float) (fileSize / (Math.pow(1024, 2)));//shows in MB
//            totalFileSize = 13;
            double current = (total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);
            Log.d(TAG, "downloadFile: totalFileSize: " + totalFileSize);

            if (currentTime > 10 * timeCount) {

                download.setCurrentFileSize((float) current);
                download.setProgress(progress);
                sendNotification(download, id);
                timeCount++;
            }

            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        bis.close();

//        Encryption encryption = new Encryption();
//        Log.d(TAG, "downloadFile: encryption");
//        try {
//            try {
//                encryption.encrypt(filePath);
//            } catch (NoSuchPaddingException e) {
//                e.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            }
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }

        onDownloadComplete(id);
        installApk(outputFile);

    }

    private void installApk(File outputFile) {
        if(outputFile.exists()){
            try {
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(outputFile),
                            "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }else{
                    Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", outputFile);
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(apkUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    getApplicationContext().startActivity(intent);
                }
            }catch (Exception e){
                e.printStackTrace();
                //show error Fragment
            }
        }
    }

    private void sendNotification(Download download, int id) {
        Log.d(TAG, "sendNotification: ");
        //Send to EbookDetailActivity
        sendIntent(download, id);
        //Send to Notification Bar
        notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText("Downloading file " + new DecimalFormat("##.##").format(download.getCurrentFileSize()) + "/" + new DecimalFormat("##.##").format(totalFileSize) + "MB");
        notificationManager.notify(id, notificationBuilder.build());
    }

    private void sendIntent(Download download, int id) {
        Log.d(TAG, "sendIntent: ");
        Intent intent = new Intent(LinkConfig.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        intent.putExtra("id", id);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete(int id) {
        Log.d(TAG, "onDownloadComplete: ");

        isDownloading = false;

        try {
            Download download = new Download();
            download.setProgress(100);
            sendIntent(download, id);

            notificationManager.cancel(id);
            notificationBuilder.setProgress(0, 0, false);
            notificationBuilder.setContentText("Tap to Open");
//            notificationManager.notify(id, notificationBuilder.build());

//            Intent intent = new Intent(this, EbookDetailActivity.class);
//            intent.putExtra("savename", saveName);
//            intent.putExtra("id",id);
//            PendingIntent pIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            notificationBuilder
//                    .setAutoCancel(true)
//                    .setContentIntent(pIntent)
//                    .setContentTitle("File Downloaded: " + saveName);
            notificationManager.notify(id, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(id);
    }

}
