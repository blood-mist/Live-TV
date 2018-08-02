package androidtv.livetv.stb.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class TimeStampEntity {
    @SerializedName("utc")
    private int utc;

    public long getUtc() {
        return utc;
    }

    public void setUtc(int utc) {
        this.utc = utc;
    }

}

