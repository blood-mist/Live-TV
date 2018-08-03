package androidtv.livetv.stb.entity;

import com.google.gson.annotations.SerializedName;

public class DvrStartDateTimeEntity {
    @SerializedName("startDate")
    private String startDate;

    @SerializedName("startTime")
    private String startTime;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
