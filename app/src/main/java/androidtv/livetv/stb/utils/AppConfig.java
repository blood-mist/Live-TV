package androidtv.livetv.stb.utils;

import android.os.Build;

import androidtv.livetv.stb.BuildConfig;

/**
 * Created by blood-mist on 1/12/18.
 */

public class AppConfig {
    private static boolean isFromDevelopment = BuildConfig.DEBUG;

    public static boolean isDevelopment() {
        return isFromDevelopment;
    }
     public static String getMac(){

//      return "ccd3e2226503";
//      return "abcd";
         return "ccd3e222652e";
     }
}
