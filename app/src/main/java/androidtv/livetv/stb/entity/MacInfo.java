package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class MacInfo{

	int responseCode;
	String error;

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@SerializedName("mac_exists")
	private String macExists;

	@SerializedName("status_check_interval")
	private int statusCheckInterval;

	@SerializedName("code")
	private String code;

	@SerializedName("message")
	private String message;

	@SerializedName("timer_expiry")
	private int timerExpiry;

	public void setMacExists(String macExists){
		this.macExists = macExists;
	}

	public String getMacExists(){
		return macExists;
	}

	public void setStatusCheckInterval(int statusCheckInterval){
		this.statusCheckInterval = statusCheckInterval;
	}

	public int getStatusCheckInterval(){
		return statusCheckInterval;
	}

	public void setCode(String code){
		this.code = code;
	}

	public String getCode(){
		return code;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setTimerExpiry(int timerExpiry){
		this.timerExpiry = timerExpiry;
	}

	public int getTimerExpiry(){
		return timerExpiry;
	}

	@Override
 	public String toString(){
		return 
			"MacInfo{" + 
			"mac_exists = '" + macExists + '\'' + 
			",status_check_interval = '" + statusCheckInterval + '\'' + 
			",code = '" + code + '\'' + 
			",message = '" + message + '\'' + 
			",timer_expiry = '" + timerExpiry + '\'' + 
			"}";
		}
}