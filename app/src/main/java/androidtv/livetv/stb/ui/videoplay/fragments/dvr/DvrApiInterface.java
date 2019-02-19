package androidtv.livetv.stb.ui.videoplay.fragments.dvr;

import java.util.List;

import androidtv.livetv.stb.entity.DvrStartDateTimeEntity;
import androidtv.livetv.stb.entity.EpgMasterResponse;
import androidtv.livetv.stb.entity.EpgResponse;
import androidtv.livetv.stb.utils.LinkConfig;
import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface DvrApiInterface {
//    @GET(LinkConfig.BASE_URL+LinkConfig.EPG_URL+"/{channelId}")
    @GET
    Observable<Response<List<EpgMasterResponse>>> getEpgs(@Url String epgUrl, @Header("Authorization") String token, @Query(value = "date",encoded = true) String startDate, @Query("dvr")String setFalse,@Query(value = "location",encoded = true) String timeZone);

    //https://middleware.yourman.info/api/v1/dvr/getdvr?utc=1533140261&userId=274&hash=752993d415520e52b82664330f1afd96&hasDVR=1&channelId=42
    @GET(LinkConfig.BASE_URL+LinkConfig.DVR_VIDEO_URL)
    Observable<Response<DvrStartDateTimeEntity>> getStartTime(@Header("Authorization") String token,@Query("utc") long utc,@Query("userId") String userId, @Query("hash")String hash ,@Query("hasDVR") int hasDvr,@Query("channelId") int channelId);

}
