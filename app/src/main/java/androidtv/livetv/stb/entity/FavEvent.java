package androidtv.livetv.stb.entity;

public class FavEvent {
    private int favStatus;

    private long successStatus;

    public ChannelItem getChannelItem() {
        return channelItem;
    }

    public void setChannelItem(ChannelItem channelItem) {
        this.channelItem = channelItem;
    }

    private ChannelItem channelItem;

    public FavEvent(long successStatus,int favStatus, ChannelItem channelItem) {
        this.favStatus = favStatus;
        this.channelItem = channelItem;
    }

    public int getFavStatus() {
        return favStatus;
    }

    public void setFavStatus(int favStatus) {
        this.favStatus = favStatus;
    }

    public long getSuccessStatus() {
        return successStatus;
    }

    public void setSuccessStatus(long successStatus) {
        this.successStatus = successStatus;
    }
}
