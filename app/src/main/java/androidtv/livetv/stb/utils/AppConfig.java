package androidtv.livetv.stb.utils;

/**
 * Created by blood-mist on 1/12/18.
 */

public class AppConfig {
    private static boolean isFromDevelopment =true;

    public static boolean isDevelopment() {
        return isFromDevelopment;
    }
     public static String getMac(){

      return "ccd3e2226503";
//      return "abcd";
     }
}
