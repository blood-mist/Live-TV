package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class LoginErrorResponse{

	@SerializedName("error")
	private LoginError error;

	public void setError(LoginError error){
		this.error = error;
	}

	public LoginError getError(){
		return error;
	}

	@Override
 	public String toString(){
		return 
			"LoginErrorResponse{" + 
			"error = '" + error + '\'' + 
			"}";
		}
}