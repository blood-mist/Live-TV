package androidtv.livetv.stb.ui.videoplay.fragments.epg;

import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.EpgResponse;
import androidtv.livetv.stb.utils.LinkConfig;
import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EpgApiInterface {
    @GET(LinkConfig.BASE_URL+LinkConfig.EPG_URL+"/{channelId}")
    Observable<Response<EpgResponse>> getEpgs( @Path("channelId")String channelId, @Header("Authorization") String token, @Query("utc") long utc, @Query("userId") String userId, @Query("hash")String hash);
}
