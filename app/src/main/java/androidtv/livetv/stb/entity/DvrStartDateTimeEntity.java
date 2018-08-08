package androidtv.livetv.stb.entity;

import com.google.gson.annotations.SerializedName;

public class DvrStartDateTimeEntity {
    @SerializedName("startDate")
    private String startDate;

    @SerializedName("startTime")
    private String startTime;

    //"error_code":"100","error_message":"No contents available for the DVR

    @SerializedName("error_code")
    private String errorCode;

    @SerializedName("error_message")
    private String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

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
