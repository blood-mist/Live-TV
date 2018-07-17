package androidtv.livetv.stb.ui.splash;

import android.arch.lifecycle.LiveData;

import java.util.List;

import androidtv.livetv.stb.entity.AppVersionInfo;
import androidtv.livetv.stb.entity.GeoAccessInfo;
import androidtv.livetv.stb.entity.MacInfo;
import androidtv.livetv.stb.entity.UserCheckInfo;
import androidtv.livetv.stb.utils.LinkConfig;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface SplashApiInterface {
    @GET(LinkConfig.MAC_EXISTS)
    Observable<Response<MacInfo>> checkMacValidation(@Query("mac") String macAddress);

    @GET(LinkConfig.ALLOW_COUNTRY)
    Observable<Response<GeoAccessInfo>> checkGeoAccess();

    @GET(LinkConfig.LINK_SEVER_APKs)
    Observable<Response<List<AppVersionInfo>>> checkForAppVersion(@Query("macAddress") String macAddress, @Query("versionCode") int versionCode
            , @Query("versionName") String versionName, @Query("packageName") String packageName);

    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    @GET(LinkConfig.CHECK_VALIDITY_ACTIVATION_APPROVAL)
    Observable<Response<UserCheckInfo>> checkUserStatus(@Query("boxId") String macAddress);
}
