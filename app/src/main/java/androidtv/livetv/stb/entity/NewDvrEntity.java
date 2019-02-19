package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class NewDvrEntity{

	@SerializedName("dvr_path")
	private String dvrPath;

	public void setDvrPath(String dvrPath){
		this.dvrPath = dvrPath;
	}

	public String getDvrPath(){
		return dvrPath;
	}

	@Override
 	public String toString(){
		return 
			"NewDvrEntity{" + 
			"dvr_path = '" + dvrPath + '\'' + 
			"}";
		}
}