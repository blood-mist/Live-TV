package androidtv.livetv.stb.entity;

public class ChannelLinkResponseWrapper {

    private ChannelLinkResponse channelLinkResponse;
    private Exception exception;

    public ChannelLinkResponse getChannelLinkResponse() {
        return channelLinkResponse;
    }

    public void setChannelLinkResponse(ChannelLinkResponse channelLinkResponse) {
        this.channelLinkResponse = channelLinkResponse;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
