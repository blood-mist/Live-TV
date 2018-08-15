package androidtv.livetv.stb.utils;

import android.content.Context;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidtv.livetv.stb.R;
import timber.log.Timber;

public class LinkConfig {
    public static final String LOGIN_FILE_NAME = "androidtv_mylogin";
    public static final String TOKEN_CONFIG_FILE_NAME ="authCode" ;
    public static final String ACCOUNT_PACKAGE="androidtv.myaccount.stb";
    public static final String BASE_URL ="https://middleware.yourman.info/" ;
    public static final String CHECK_IF_SERVER_RECHABLE = "https://middleware.yourman.info";
    public static final String CHANNEL_LOGO_URL = "uploads/channel/";
    public static final String CHANNEL_LINK_URL = "api/v1/livetv/getchannels";
    public static final String NEW_CHANNEL_LINK="api/v1/livetv/package_validation";
    public static final String EXTRA_LINK_URL = "api/v1/livetv/internet";


    public static final String LOGIN_BUTTON_CLICK ="api/v1/user/login";
    public static final String ALLOW_COUNTRY ="api/v1/other/geoblock";
    public static final String GET_SESSION = "api/v1/session/update";
    public static final String CATEGORY_URL = "api/v1/livetv";
    public static final String GET_UTC =  "api/v1/other/utc";
    public static final String GROUP_DATA = "api/v1/user/group_json";
    public static final String MAC_EXISTS ="api/v1/mac/exists";
    public static final String LINK_SEVER_APKs = "market_app_info/api/market_app_info.php";
    public static final String CHECK_VALIDITY_ACTIVATION_APPROVAL = "api/v1/user/checklogin";
    public static final String PREVIEW_LINK_LOADER = "api/v1/livetv/channel/preview";
    public static final String EPG_URL ="api/v1/epg";
    public static final String FAV_UNFAV_URL = "api/v1/livetv/favorite/addfavorite";
    public static final String DVR_VIDEO_URL = "api/v1/dvr/getdvr";
    public static final String FAV_CHANNELS_ID_URL = "api/v1/livetv/favorite/list";
    public static final String CHANNEL_CHANGE_OBSERVABLE="channel_change";

    public static final String LIVE_ERROR_CODE="error_code";
    public static final String LIVE_ERROR_MESSAGE="error_message";
    public static final String LIVE_IP="ip_address";
    public static final String DOWNLOAD_LINK="download_link";
    public static final String DOWNLOAD_NAME="download_name";
    public static final String DOWNLOAD_ID="download_id";
    public static final String MESSAGE_PROGRESS = "message_progress";
    public static final String MESSAGE_ERROR = "message_error";
    public static final String DOWNLOAD_FRAGMENT="download_fragment";
    public static final String USER_EMAIL="user_email";
    public static  final String USER_PASSWORD="user_password";
    public static final String DATABASE_NAME="androidtv_db";
    public static final String LOGIN_TABLE="login_table";
    public static final String CHANNEL_TABLE="channel_table";
    public static final String CATEGORY_TABLE="category_table";
    public static final String EPG_TABLE = "epg_table";
    public  static final int  NO_CONNECTION=400;
    public static final int INVALID_HASH = 101;
    public static final int INVALID_USER=102;
    public static final String CHANNEL_ID ="channel_id" ;
    public static final String CATEGORY_FAVORITE = "Favorites";
     public static final String PLAYED_CATEGORY_NAME ="last_played_category_name";
     public static final String SELECTED_CATEGORY_NAME ="selected_category_name";
    public static String getHashCode(String utc) {
      /*  String sessionId = null;
        try {
//			if (LoginFileUtils.readFromFile(EntryPoint.macAddress)) {
            sessionId = sessionId;
            Timber.e("session id in hash"+ sessionId);
            System.out.println(sessionId + "hash");
            if (sessionId != null || GroupDataParser.groupData.getUserId() + "" != null) {

                String SecretKey = "123456789";
                JSONObject jsonObject = new JSONObject(utc);
                String json_utc = jsonObject.getString("utc");
                Timber.d("utc time", json_utc + "");

                String stringToMD5 = SecretKey + sessionId
                        + GroupDataParser.groupData.getUserId() + "" + json_utc;
                String hexString = md5(stringToMD5);


                return "utc=" + json_utc.trim() + "&userId=" + GroupDataParser.groupData.getUserId() + ""
                        + "&hash=" + hexString.toString();
            } else {
                return null;
            }
//			} else {
//				return null;
//			}
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }*/
      return null;
    }
public static String getHashCode(String userId,String utc,String sessionId){
    String SecretKey = "123456789";
    String stringToMD5 = SecretKey + sessionId
            + userId + "" + utc;
    return md5(stringToMD5);
}
    private static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getString(Context context, int resId) {
        return BASE_URL + context.getResources().getString(resId);


    }
}
