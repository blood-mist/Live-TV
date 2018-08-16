package androidtv.livetv.stb.entity;

public class ChannelLinkResponse {
    private ChannelLink channel;
    private String status;
    private String error_message;
    private int error_code;

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    /*channel":{"link":"http:\/\/61.194.237.12\/livestreamer\/wodArydigital783451.stream\/playlist.m3u8","server_type":1},"status":200
*/
    public ChannelLink getChannel() {
        return channel;
    }

    public void setChannel(ChannelLink channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
