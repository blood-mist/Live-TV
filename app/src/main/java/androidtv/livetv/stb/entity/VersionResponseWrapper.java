package androidtv.livetv.stb.entity;

import java.util.List;

public class VersionResponseWrapper {
    public List<AppVersionInfo> getAppVersionInfo() {
        return appVersionInfo;
    }

    public void setAppVersionInfo(List<AppVersionInfo> appVersionInfo) {
        this.appVersionInfo = appVersionInfo;
    }

    private List<AppVersionInfo> appVersionInfo;

    public VersionErrorResponse getVersionErrorResponse() {
        return versionErrorResponse;
    }

    public void setVersionErrorResponse(VersionErrorResponse versionErrorResponse) {
        this.versionErrorResponse = versionErrorResponse;
    }

    private  VersionErrorResponse versionErrorResponse;
}
