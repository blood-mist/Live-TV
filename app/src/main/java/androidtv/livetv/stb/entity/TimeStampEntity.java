package androidtv.livetv.stb.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class TimeStampEntity {
    @SerializedName("timestamp")
    @Expose
    private long timestamp;

    public long getUtc() {
        return timestamp;
    }

    public void setUtc(long timestamp) {
        this.timestamp = timestamp;
    }

}

