package androidtv.livetv.stb;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import timber.log.Timber;


public class ApplicationMain extends Application {


    protected String userAgent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
        userAgent = Util.getUserAgent(this, getString(R.string.app_name));
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }





    private void handleUncaughtException(Thread thread, Throwable e) {
        try {
            e.printStackTrace();
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            String s = writer.toString();
            Log.d("CheckingErrorStatus", s);
            // String fpath = "/sdcard/.Movies_wod/"+fname+".txt";
            File file = new File(getExternalFilesDir(null), "LiveTV report");
            Log.d("File Stored in", getExternalFilesDir(null).getPath()
                    + "crash_report");
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.close();
        } catch (IOException e1) {

            Toast.makeText(this, "failed to save datas", Toast.LENGTH_LONG).show();
        }
        System.exit(1);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public DataSource.Factory buildDataSourceFactory(TransferListener listener) {
        return new DefaultDataSourceFactory(this, listener, buildHttpDataSourceFactory(listener));
    }

    /** Returns a {@link HttpDataSource.Factory}. */
    public HttpDataSource.Factory buildHttpDataSourceFactory(
            TransferListener listener) {
        return new DefaultHttpDataSourceFactory(userAgent, listener);
    }

}

/**
 * A tree which logs important information for crash reporting.
 */
final class CrashReportingTree extends Timber.Tree {

    @Override
    protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        FakeCrashLibrary.log(priority, tag, message);

        if (t != null) {
            if (priority == Log.ERROR) {
                FakeCrashLibrary.logError(t);
            } else if (priority == Log.WARN) {
                FakeCrashLibrary.logWarning(t);
            }
        }
    }



}
