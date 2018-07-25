package androidtv.livetv.stb.entity;

public class ChannelLinkResponse {
    private ChannelLink channel;
    private String status;
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
