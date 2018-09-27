package androidtv.livetv.stb.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Generated;

import com.google.gson.annotations.SerializedName;

import io.reactivex.annotations.Nullable;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_TABLE;

@Generated("com.robohorse.robopojogenerator")
@Entity(tableName = CHANNEL_TABLE)
public class ChannelItem implements Parcelable {

    @ColumnInfo(name = "channel_logo")
    @SerializedName("channel_logo")
    private String channelLogo;

    @Ignore
    @SerializedName("is_content_allow")
    private String isContentAllow;

    public int getIs_fav() {
        return is_fav;
    }

    public void setIs_fav(int is_fav) {
        this.is_fav = is_fav;
    }


    @ColumnInfo(name = "is_fav")
    private int is_fav;


    @Ignore
    @SerializedName("mobile_ad_url")
    private String mobileAdUrl;

    @Ignore
    @SerializedName("mobile_url")
    private String mobileUrl;

    @Ignore
    @SerializedName("created_at")
    private String createdAt;

    @Ignore
    @SerializedName("device_type")
    private String deviceType;

    @Ignore
    @SerializedName("mobile_server_type")
    private int mobileServerType;

    @Ignore
    @SerializedName("channel_server_type")
    private int channelServerType;

    @Ignore
    @SerializedName("parental_lock")
    private String parentalLock;

    @Ignore
    @SerializedName("dvr_path_mobile")
    private String dvrPathMobile;

    @ColumnInfo(name = "category_id")
    @SerializedName("category_id")
    private int categoryId;

    @Ignore
    @SerializedName("updated_at")
    private String updatedAt;

    @ColumnInfo(name = "price")
    @SerializedName("price")
    private String price;

    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    @SerializedName("id")
    private int id;

    @Ignore
    @SerializedName("channel_type")
    private String channelType;

    @ColumnInfo(name = "epg_name")
    @SerializedName("channel_epg_name")
    private String channelEpgName;

    @ColumnInfo(name = "channel_cdn_url")
    @SerializedName("channel_cdn_url")
    private String channelCdnUrl;

    @Ignore
    @SerializedName("channel_cdn_server_type")
    private int channelCdnServerType;

    @ColumnInfo(name = "dvr_path")
    @SerializedName("dvr_path")
    private String dvrPath;

    @ColumnInfo(name = "channl_url")
    @SerializedName("channel_url")
    private String channelUrl;

    @ColumnInfo(name = "channel_desc")
    @SerializedName("channel_desc")
    private String channelDesc;

    @Ignore
    @SerializedName("channel_status")
    private int channelStatus;

    @Ignore
    @SerializedName("channel_language")
    private String channelLanguage;


    @ColumnInfo(name = "channel_name")
    @SerializedName("name")
    private String name;

    @ColumnInfo(name = "channel_priority")
    @SerializedName("channel_priority")
    private int channelPriority;

    @Ignore
    @SerializedName("mobile_ad_server_type")
    private int mobileAdServerType;

    @Ignore
    @SerializedName("country_id")
    private int countryId;

    @ColumnInfo(name = "has_dvr")
    @SerializedName("has_dvr")
    private boolean hasDvr;

    @ColumnInfo(name = "has_epg")
    @SerializedName("has_epg")
    private boolean hasEpg;

    public boolean isHasDvr() {
        return hasDvr;
    }

    public void setHasDvr(boolean hasDvr) {
        this.hasDvr = hasDvr;
    }

    public boolean isHasEpg() {
        return hasEpg;
    }

    public void setHasEpg(boolean hasEpg) {
        this.hasEpg = hasEpg;
    }

    public void setChannelLogo(String channelLogo) {
        this.channelLogo = channelLogo;
    }

    public String getChannelLogo() {
        return channelLogo;
    }

    public void setIsContentAllow(String isContentAllow) {
        this.isContentAllow = isContentAllow;
    }

    public String getIsContentAllow() {
        return isContentAllow;
    }

    public void setMobileAdUrl(String mobileAdUrl) {
        this.mobileAdUrl = mobileAdUrl;
    }

    public String getMobileAdUrl() {
        return mobileAdUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setMobileServerType(int mobileServerType) {
        this.mobileServerType = mobileServerType;
    }

    public int getMobileServerType() {
        return mobileServerType;
    }

    public void setChannelServerType(int channelServerType) {
        this.channelServerType = channelServerType;
    }

    public int getChannelServerType() {
        return channelServerType;
    }

    public void setParentalLock(String parentalLock) {
        this.parentalLock = parentalLock;
    }

    public String getParentalLock() {
        return parentalLock;
    }

    public void setDvrPathMobile(String dvrPathMobile) {
        this.dvrPathMobile = dvrPathMobile;
    }

    public String getDvrPathMobile() {
        return dvrPathMobile;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelEpgName(String channelEpgName) {
        this.channelEpgName = channelEpgName;
    }

    public String getChannelEpgName() {
        return channelEpgName;
    }

    public void setChannelCdnUrl(String channelCdnUrl) {
        this.channelCdnUrl = channelCdnUrl;
    }

    public String getChannelCdnUrl() {
        return channelCdnUrl;
    }

    public void setChannelCdnServerType(int channelCdnServerType) {
        this.channelCdnServerType = channelCdnServerType;
    }

    public int getChannelCdnServerType() {
        return channelCdnServerType;
    }

    public void setDvrPath(String dvrPath) {
        this.dvrPath = dvrPath;
    }

    public String getDvrPath() {
        return dvrPath;
    }

    public void setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelDesc(String channelDesc) {
        this.channelDesc = channelDesc;
    }

    public String getChannelDesc() {
        return channelDesc;
    }

    public void setChannelStatus(int channelStatus) {
        this.channelStatus = channelStatus;
    }

    public int getChannelStatus() {
        return channelStatus;
    }

    public void setChannelLanguage(String channelLanguage) {
        this.channelLanguage = channelLanguage;
    }

    public String getChannelLanguage() {
        return channelLanguage;
    }



    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setChannelPriority(int channelPriority) {
        this.channelPriority = channelPriority;
    }

    public int getChannelPriority() {
        return channelPriority;
    }

    public void setMobileAdServerType(int mobileAdServerType) {
        this.mobileAdServerType = mobileAdServerType;
    }

    public int getMobileAdServerType() {
        return mobileAdServerType;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getCountryId() {
        return countryId;
    }

    @Override
    public boolean equals(Object obj) {
        ChannelItem item= (ChannelItem) obj;
        if(item.getId()==getId())
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return
                "ChannelItem{" +
                        "channel_logo = '" + channelLogo + '\'' +
                        ",is_content_allow = '" + isContentAllow + '\'' +
                        ",mobile_ad_url = '" + mobileAdUrl + '\'' +
                        ",mobile_url = '" + mobileUrl + '\'' +
                        ",created_at = '" + createdAt + '\'' +
                        ",device_type = '" + deviceType + '\'' +
                        ",mobile_server_type = '" + mobileServerType + '\'' +
                        ",channel_server_type = '" + channelServerType + '\'' +
                        ",parental_lock = '" + parentalLock + '\'' +
                        ",dvr_path_mobile = '" + dvrPathMobile + '\'' +
                        ",category_id = '" + categoryId + '\'' +
                        ",updated_at = '" + updatedAt + '\'' +
                        ",price = '" + price + '\'' +
                        ",id = '" + id + '\'' +
                        ",channel_type = '" + channelType + '\'' +
                        ",channel_epg_name = '" + channelEpgName + '\'' +
                        ",channel_cdn_url = '" + channelCdnUrl + '\'' +
                        ",channel_cdn_server_type = '" + channelCdnServerType + '\'' +
                        ",dvr_path = '" + dvrPath + '\'' +
                        ",channel_url = '" + channelUrl + '\'' +
                        ",channel_desc = '" + channelDesc + '\'' +
                        ",channel_status = '" + channelStatus + '\'' +
                        ",channel_language = '" + channelLanguage + '\'' +
                        ",name = '" + name + '\'' +
                        ",channel_priority = '" + channelPriority + '\'' +
                        ",mobile_ad_server_type = '" + mobileAdServerType + '\'' +
                        ",country_id = '" + countryId + '\'' +
                        "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.channelLogo);
        dest.writeString(this.isContentAllow);
        dest.writeInt(this.is_fav);
        dest.writeString(this.mobileAdUrl);
        dest.writeString(this.mobileUrl);
        dest.writeString(this.createdAt);
        dest.writeString(this.deviceType);
        dest.writeInt(this.mobileServerType);
        dest.writeInt(this.channelServerType);
        dest.writeString(this.parentalLock);
        dest.writeString(this.dvrPathMobile);
        dest.writeInt(this.categoryId);
        dest.writeString(this.updatedAt);
        dest.writeString(this.price);
        dest.writeInt(this.id);
        dest.writeString(this.channelType);
        dest.writeString(this.channelEpgName);
        dest.writeString(this.channelCdnUrl);
        dest.writeInt(this.channelCdnServerType);
        dest.writeString(this.dvrPath);
        dest.writeString(this.channelUrl);
        dest.writeString(this.channelDesc);
        dest.writeInt(this.channelStatus);
        dest.writeString(this.channelLanguage);
        dest.writeString(this.name);
        dest.writeInt(this.channelPriority);
        dest.writeInt(this.mobileAdServerType);
        dest.writeInt(this.countryId);
    }

    public ChannelItem() {
    }

    protected ChannelItem(Parcel in) {
        this.channelLogo = in.readString();
        this.isContentAllow = in.readString();
        this.is_fav = in.readInt();
        this.mobileAdUrl = in.readString();
        this.mobileUrl = in.readString();
        this.createdAt = in.readString();
        this.deviceType = in.readString();
        this.mobileServerType = in.readInt();
        this.channelServerType = in.readInt();
        this.parentalLock = in.readString();
        this.dvrPathMobile = in.readString();
        this.categoryId = in.readInt();
        this.updatedAt = in.readString();
        this.price = in.readString();
        this.id = in.readInt();
        this.channelType = in.readString();
        this.channelEpgName = in.readString();
        this.channelCdnUrl = in.readString();
        this.channelCdnServerType = in.readInt();
        this.dvrPath = in.readString();
        this.channelUrl = in.readString();
        this.channelDesc = in.readString();
        this.channelStatus = in.readInt();
        this.channelLanguage = in.readString();
        this.name = in.readString();
        this.channelPriority = in.readInt();
        this.mobileAdServerType = in.readInt();
        this.countryId = in.readInt();
    }

    public static final Parcelable.Creator<ChannelItem> CREATOR = new Parcelable.Creator<ChannelItem>() {
        @Override
        public ChannelItem createFromParcel(Parcel source) {
            return new ChannelItem(source);
        }

        @Override
        public ChannelItem[] newArray(int size) {
            return new ChannelItem[size];
        }
    };
}