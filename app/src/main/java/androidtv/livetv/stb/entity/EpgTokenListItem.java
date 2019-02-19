package androidtv.livetv.stb.entity;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class EpgTokenListItem{

	@SerializedName("programName")
	private String programName;

	@SerializedName("startTime")
	private String startTime;

	@SerializedName("endTime")
	private String endTime;

	@SerializedName("day")
	private String day;

	@SerializedName("startDate")
	private String startDate;

	public void setProgramName(String programName){
		this.programName = programName;
	}

	public String getProgramName(){
		return programName;
	}

	public void setStartTime(String startTime){
		this.startTime = startTime;
	}

	public String getStartTime(){
		return startTime;
	}

	public void setEndTime(String endTime){
		this.endTime = endTime;
	}

	public String getEndTime(){
		return endTime;
	}

	public void setDay(String day){
		this.day = day;
	}

	public String getDay(){
		return day;
	}

	public void setStartDate(String startDate){
		this.startDate = startDate;
	}

	public String getStartDate(){
		return startDate;
	}

	@Override
 	public String toString(){
		return 
			"EpgTokenListItem{" + 
			"programName = '" + programName + '\'' + 
			",startTime = '" + startTime + '\'' + 
			",endTime = '" + endTime + '\'' + 
			",day = '" + day + '\'' + 
			",startDate = '" + startDate + '\'' + 
			"}";
		}
}