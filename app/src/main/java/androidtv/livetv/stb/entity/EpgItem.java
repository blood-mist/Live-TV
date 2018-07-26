package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Generated("com.robohorse.robopojogenerator")
public class EpgItem{

	@SerializedName("start_time")
	private String startTime;

	@SerializedName("program_name")
	private String programName;

	@SerializedName("channel")
	private String channel;

	@SerializedName("end_time")
	private String endTime;






	public void setStartTime(String startTime){
		this.startTime = startTime;
	}

	public String getStartTime(){
		return startTime;
	}

	public void setProgramName(String programName){
		this.programName = programName;
	}

	public String getProgramName(){
		return programName;
	}

	public void setChannel(String channel){
		this.channel = channel;
	}

	public String getChannel(){
		return channel;
	}

	public void setEndTime(String endTime){
		this.endTime = endTime;
	}

	public String getEndTime(){
		return endTime;
	}

	@Override
 	public String toString(){
		return 
			"EpgItem{" + 
			"start_time = '" + startTime + '\'' + 
			",program_name = '" + programName + '\'' + 
			",channel = '" + channel + '\'' + 
			",end_time = '" + endTime + '\'' + 
			"}";
		}
}