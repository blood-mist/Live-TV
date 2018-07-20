package androidtv.livetv.stb.entity;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class CatChannelInfo{

	@SerializedName("channel")
	private List<ChannelItem> channel;

	@SerializedName("category")
	private List<CategoryItem> category;

	@SerializedName("status")
	private int status;

	public void setChannel(List<ChannelItem> channel){
		this.channel = channel;
	}

	public List<ChannelItem> getChannel(){
		return channel;
	}

	public void setCategory(List<CategoryItem> category){
		this.category = category;
	}

	public List<CategoryItem> getCategory(){
		return category;
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
			"CatChannelInfo{" + 
			"channel = '" + channel + '\'' + 
			",category = '" + category + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}