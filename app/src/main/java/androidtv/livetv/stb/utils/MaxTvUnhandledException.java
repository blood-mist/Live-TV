package androidtv.livetv.stb.utils;

public class MaxTvUnhandledException extends Exception {
    private int error_code;
    private String message;

    public MaxTvUnhandledException(int c,String m){
        this.error_code=c;
        this.message = m;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
