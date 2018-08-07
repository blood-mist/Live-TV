package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class LoginInvalidResponse {

	public LoginInvalidData getLoginInvalidData() {
		return loginInvalidData;
	}

	public void setLoginInvalidData(LoginInvalidData loginInvalidData) {
		this.loginInvalidData = loginInvalidData;
	}

	@SerializedName("login")
	private LoginInvalidData loginInvalidData;



	@Override
 	public String toString(){
		return 
			"LoginInvalidResponse{" +
			"login = '" + loginInvalidData + '\'' +
			"}";
		}
}