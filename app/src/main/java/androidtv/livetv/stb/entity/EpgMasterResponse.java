package androidtv.livetv.stb.entity;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class EpgMasterResponse{

	@SerializedName("date")
	private String date;

	@SerializedName("epgTokenList")
	private List<EpgTokenListItem> epgTokenList;

	public void setDate(String date){
		this.date = date;
	}

	public String getDate(){
		return date;
	}

	public void setEpgTokenList(List<EpgTokenListItem> epgTokenList){
		this.epgTokenList = epgTokenList;
	}

	public List<EpgTokenListItem> getEpgTokenList(){
		return epgTokenList;
	}

	@Override
 	public String toString(){
		return 
			"EpgMasterResponse{" + 
			"date = '" + date + '\'' + 
			",epgTokenList = '" + epgTokenList + '\'' + 
			"}";
		}
}