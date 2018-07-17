package androidtv.livetv.stb.entity;

import javax.annotation.Generated;

import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class GeoAccessInfo {

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    String responseCode;
    String error;


    @SerializedName("allow")
    private Allow allow;

    @SerializedName("status")
    private int status;

    public void setAllow(Allow allow) {
        this.allow = allow;
    }

    public Allow getAllow() {
        return allow;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return
                "GeoAccessInfo{" +
                        "allow = '" + allow + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}