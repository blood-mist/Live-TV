package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class LoginInfo{
	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private String responseCode;

	private String errorMessage;

	@SerializedName("login")
	private Login login;

	public void setLogin(Login login){
		this.login = login;
	}

	public Login getLogin(){
		return login;
	}

	@Override
 	public String toString(){
		return 
			"LoginInfo{" + 
			"login = '" + login + '\'' + 
			"}";
		}
}