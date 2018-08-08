package androidtv.livetv.stb.entity;

import com.google.gson.annotations.SerializedName;

public class DvrLinkResponse {
    /* {"link":"http:\/\/cloud.truestreamz.com\/woddvr\/setmax\/setmax.stream_2018-08-02-16.26.31.982-JST.mp4\/playlist.m3u8?wmsAuthSign=c2VydmVyX3RpbWU9OC80LzIwMTggMTE6MDc6NDIgQU0maGFzaF92YWx1ZT1BdW9rSDZRcW4yYklQMU1oMjBZQjd3PT0mdmFsaWRtaW51dGVzPTEmaWQ9MjU2",
             "startTime":"16:26:31","nextVideoName":"setmax.stream_2018-08-02-16.56.34.573-JST.mp4"}*/
    @SerializedName("link")
    private String link;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("nextVideoName")
    private String nextVideoName;

//    {"error_code":"100","error_msg":"Sorry, No channel Assigned"}

    @SerializedName("error_code")
    private int errorCode;


    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getNextVideoName() {
        return nextVideoName;
    }

    public void setNextVideoName(String nextVideoName) {
        this.nextVideoName = nextVideoName;
    }
}
