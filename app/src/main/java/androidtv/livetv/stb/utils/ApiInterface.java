package androidtv.livetv.stb.utils;

import android.arch.lifecycle.LiveData;

import java.util.List;

import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.CatChannelInfo;
import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.FavoriteResponse;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.TimeStampEntity;
import androidtv.livetv.stb.entity.UserCheckInfo;
import androidtv.livetv.stb.utils.LinkConfig;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {
    @GET(LinkConfig.MAC_EXISTS)
    Observable<Response<MacInfo>> checkMacValidation(@Query("mac") String macAddress);

    @GET(LinkConfig.ALLOW_COUNTRY)
    Observable<Response<GeoAccessInfo>> checkGeoAccess();

    @GET(LinkConfig.LINK_SEVER_APKs)
    Observable<Response<ResponseBody>> checkForAppVersion(@Query("macAddress") String macAddress, @Query("versionCode") int versionCode
            , @Query("versionName") String versionName, @Query("packageName") String packageName);

    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    @GET(LinkConfig.CHECK_VALIDITY_ACTIVATION_APPROVAL)
    Observable<Response<ResponseBody>> checkUserStatus(@Query("boxId") String macAddress);

    @POST(LinkConfig.LOGIN_BUTTON_CLICK)
    @FormUrlEncoded
    Observable<Response<ResponseBody>> signIn(@Field("uname") String userEmail, @Field("pswd") String userPassword, @Field("boxId") String boxId);

    @GET(LinkConfig.CATEGORY_URL)
    Observable<Response<ResponseBody>> getCatChannel(@Header("Authorization") String token, @Query("utc") long utc,@Query("userId") String userId,@Query("hash")String hash);

    @GET(LinkConfig.GET_UTC)
    Observable<Response<TimeStampEntity>> getTimestamp();
    @GET(LinkConfig.CHANNEL_LINK_URL)
    Observable<Response<ChannelLinkResponse>> getChannelLink(@Header("Authorization") String token, @Query("utc") long utc, @Query("userId") String userId, @Query("hash")String hash, @Query("channelID") String id);

    @GET(LinkConfig.FAV_UNFAV_URL)
    Observable<Response<FavoriteResponse>> addToFavorite(@Header("Authorization") String token, @Query("utc")long utc, @Query("userId")String userId, @Query("hash")String hash, @Query("channelId") String channelId);

    @GET(LinkConfig.PREVIEW_LINK_LOADER)
    Observable<Response<ChannelLinkResponse>> getPreviewLink(@Header("Authorization")String token, @Query("utc")long utc, @Query("userId")String userId,@Query("hash") String hash,@Query("channelID") String channelId);
}
