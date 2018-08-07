package androidtv.livetv.stb.entity;

public class UserCheckWrapper {
    public UserCheckInfo getUserCheckInfo() {
        return userCheckInfo;
    }

    public void setUserCheckInfo(UserCheckInfo userCheckInfo) {
        this.userCheckInfo = userCheckInfo;
    }

    public UserErrorInfo getUserErrorInfo() {
        return userErrorInfo;
    }

    public void setUserErrorInfo(UserErrorInfo userErrorInfo) {
        this.userErrorInfo = userErrorInfo;
    }

    private UserCheckInfo userCheckInfo;
    private UserErrorInfo userErrorInfo;
}
