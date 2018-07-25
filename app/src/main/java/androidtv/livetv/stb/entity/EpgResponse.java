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