package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class LoginError {

	@SerializedName("error_code")
	private int errorCode;

	@SerializedName("message")
	private String message;

	public void setErrorCode(int errorCode){
		this.errorCode = errorCode;
	}

	public int getErrorCode(){
		return errorCode;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	@Override
 	public String toString(){
		return 
			"LoginError{" +
			"error_code = '" + errorCode + '\'' + 
			",message = '" + message + '\'' + 
			"}";
		}
}