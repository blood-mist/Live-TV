package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class LoginSessionInfo {

	@SerializedName("session")
	private String session;

	@SerializedName("status")
	private int status;

	public void setSession(String session){
		this.session = session;
	}

	public String getSession(){
		return session;
	}

	public void setStatus(int status){
		this.status = status;
	}

	public int getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"LoginSessionInfo{" +
			"session = '" + session + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}