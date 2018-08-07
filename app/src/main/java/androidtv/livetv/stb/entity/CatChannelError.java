package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class CatChannelError{

	@SerializedName("error_message")
	private String errorMessage;

	@SerializedName("status")
	private int status;

	public void setErrorMessage(String errorMessage){
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage(){
		return errorMessage;
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
			"CatChannelError{" + 
			"error_message = '" + errorMessage + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}