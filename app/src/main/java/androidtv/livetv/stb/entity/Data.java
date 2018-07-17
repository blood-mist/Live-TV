package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Data{

	@SerializedName("activation_status")
	private int activationStatus;

	@SerializedName("is_active")
	private int isActive;

	@SerializedName("user_name")
	private String userName;

	@SerializedName("display_name")
	private String displayName;

	public void setActivationStatus(int activationStatus){
		this.activationStatus = activationStatus;
	}

	public int getActivationStatus(){
		return activationStatus;
	}

	public void setIsActive(int isActive){
		this.isActive = isActive;
	}

	public int getIsActive(){
		return isActive;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return userName;
	}

	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}

	public String getDisplayName(){
		return displayName;
	}

	@Override
 	public String toString(){
		return 
			"Data{" + 
			"activation_status = '" + activationStatus + '\'' + 
			",is_active = '" + isActive + '\'' + 
			",user_name = '" + userName + '\'' + 
			",display_name = '" + displayName + '\'' + 
			"}";
		}
}