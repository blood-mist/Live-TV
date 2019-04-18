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
    //return "78c2c09cf3d5";
   // return "ccd3e222652e";
//         return "78c2c09cf233";
         return "a1b2c3d4e5f6";
     }
}
