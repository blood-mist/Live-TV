package androidtv.livetv.stb.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import javax.annotation.Generated;

import static androidtv.livetv.stb.utils.LinkConfig.EPG_TABLE;

@Generated("com.robohorse.robopojogenerator")
@TypeConverters(DateConverter.class)
@Entity(tableName = EPG_TABLE,indices = {@Index("channel_id")})
public class Epgs {

    @PrimaryKey
    @ColumnInfo(name = "epg_id")
    @SerializedName("id")
    private int id;

    @ColumnInfo(name = "channel_id")
    @SerializedName("channel_id")
    private int channelID;

    @ColumnInfo(name = "program_title")
    @SerializedName("program_title")
    private String programTitle;


    @ColumnInfo(name = "start_time")
    @SerializedName("start_time")
    private Date startTime;

    @ColumnInfo(name = "end_time")
    @SerializedName("end_time")
    private Date endTime;

    @ColumnInfo(name = "date")
    @SerializedName("date")
    private Date date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public void setProgramTitle(String programTitle) {
        this.programTitle = programTitle;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }



}
