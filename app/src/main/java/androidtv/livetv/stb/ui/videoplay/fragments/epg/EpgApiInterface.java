package androidtv.livetv.stb.ui.videoplay.fragments.epg;

import java.util.List;

import androidtv.livetv.stb.entity.ChannelLinkResponse;
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

public interface EpgApiInterface {
    @GET
    Observable<Response<List<EpgMasterResponse>>> getEpgs(@Url String epgUrl, @Header("Authorization") String token, @Query(value = "date", encoded = true) String startDate, @Query(value = "location", encoded = true) String timeZone);
}
