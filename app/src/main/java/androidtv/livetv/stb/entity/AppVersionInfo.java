package androidtv.livetv.stb.entity;

import javax.annotation.Generated;

import com.google.gson.annotations.SerializedName;

import androidtv.livetv.stb.BuildConfig;

@Generated("com.robohorse.robopojogenerator")
public class AppVersionInfo {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isAllow() {
        return isAllow;
    }

    public void setAllow(boolean allow) {
        isAllow = allow;
    }

    private String errorCode;

    @SerializedName("image")
    private String image;

    @SerializedName("update_type")
    private String updateType;

    @SerializedName("visibility")
    private boolean visibility;

    @SerializedName("myapp_image")
    private String myappImage;

    @SerializedName("version_code")
    private int versionCode;

    @SerializedName("description")
    private String description;

    @SerializedName("update")
    private boolean update;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("priority")
    private int priority;

    @SerializedName("version_name")
    private String versionName;

    @SerializedName("package_name")
    private String packageName;

    @SerializedName("launcher_display")
    private boolean launcherDisplay;

    @SerializedName("apk_download_link")
    private String apkDownloadLink;

    @SerializedName("is_allow")
    private boolean isAllow;

    @SerializedName("id")
    private int id;

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setMyappImage(String myappImage) {
        this.myappImage = myappImage;
    }

    public String getMyappImage() {
        return myappImage;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setLauncherDisplay(boolean launcherDisplay) {
        this.launcherDisplay = launcherDisplay;
    }

    public boolean isLauncherDisplay() {
        return launcherDisplay;
    }

    public void setApkDownloadLink(String apkDownloadLink) {
        this.apkDownloadLink = apkDownloadLink;
    }

    public String getApkDownloadLink() {
        return apkDownloadLink;
    }

    public void setIsAllow(boolean isAllow) {
        this.isAllow = isAllow;
    }

    public boolean isIsAllow() {
        return isAllow;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return
                "AppVersionInfo{" +
                        "image = '" + image + '\'' +
                        ",update_type = '" + updateType + '\'' +
                        ",visibility = '" + visibility + '\'' +
                        ",myapp_image = '" + myappImage + '\'' +
                        ",version_code = '" + versionCode + '\'' +
                        ",description = '" + description + '\'' +
                        ",update = '" + update + '\'' +
                        ",display_name = '" + displayName + '\'' +
                        ",priority = '" + priority + '\'' +
                        ",version_name = '" + versionName + '\'' +
                        ",package_name = '" + packageName + '\'' +
                        ",launcher_display = '" + launcherDisplay + '\'' +
                        ",apk_download_link = '" + apkDownloadLink + '\'' +
                        ",is_allow = '" + isAllow + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }

    public boolean getUpdate() {
        if (getVersionCode() > BuildConfig.VERSION_CODE)
            return true;
        else
            return false;
    }
}