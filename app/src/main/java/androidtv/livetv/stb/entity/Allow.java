package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Allow{

	@SerializedName("allow")
	private String allow;

	@SerializedName("code")
	private String code;

	@SerializedName("ip")
	private String ip;

	@SerializedName("message")
	private String message;

	public void setAllow(String allow){
		this.allow = allow;
	}

	public String getAllow(){
		return allow;
	}

	public void setCode(String code){
		this.code = code;
	}

	public String getCode(){
		return code;
	}

	public void setIp(String ip){
		this.ip = ip;
	}

	public String getIp(){
		return ip;
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
			"Allow{" + 
			"allow = '" + allow + '\'' + 
			",code = '" + code + '\'' + 
			",ip = '" + ip + '\'' + 
			",message = '" + message + '\'' + 
			"}";
		}
}