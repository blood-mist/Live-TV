package androidtv.livetv.stb.entity;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class EpgResponse{

	@SerializedName("epg")
	private List<EpgItem> epg;

	@SerializedName("status")
	private int status;

	@SerializedName("error_code")
	private int error_code;

	public int getError_code() {
		return error_code;
	}

	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	public void setEpg(List<EpgItem> epg){
		this.epg = epg;
	}

	public List<EpgItem> getEpg(){
		return epg;
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
			"EpgResponse{" + 
			"epg = '" + epg + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}