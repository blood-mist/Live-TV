package androidtv.livetv.stb.ui.videoplay;

import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.utils.LinkConfig;
import io.reactivex.Observable;
import io.reactivex.Observer;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface VideoPlayApiInterface {

    @GET(LinkConfig.BASE_URL+LinkConfig.CHANNEL_LINK_URL)
    Observable<Response<ChannelLinkResponse>> getChannelLink(@Header("Authorization") String token, @Query("utc") long utc, @Query("userId") String userId, @Query("hash")String hash, @Query("channelID") String id);
}
