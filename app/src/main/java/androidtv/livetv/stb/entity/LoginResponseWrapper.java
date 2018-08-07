package androidtv.livetv.stb.entity;

public class LoginResponseWrapper {
    private LoginInfo loginInfo;
    private LoginErrorResponse loginErrorResponse;

    public LoginInvalidResponse getLoginInvalidResponse() {
        return loginInvalidResponse;
    }

    public void setLoginInvalidResponse(LoginInvalidResponse loginInvalidResponse) {
        this.loginInvalidResponse = loginInvalidResponse;
    }

    private LoginInvalidResponse loginInvalidResponse;

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }

    public LoginErrorResponse getLoginErrorResponse() {
        return loginErrorResponse;
    }

    public void setLoginErrorResponse(LoginErrorResponse loginErrorResponse) {
        this.loginErrorResponse = loginErrorResponse;
    }


}
