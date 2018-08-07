package androidtv.livetv.stb.entity;

public class CatChannelWrapper {
    private CatChannelInfo catChannelInfo;
    private CatChannelError catChannelError;

    public CatChannelInfo getCatChannelInfo() {
        return catChannelInfo;
    }

    public void setCatChannelInfo(CatChannelInfo catChannelInfo) {
        this.catChannelInfo = catChannelInfo;
    }

    public CatChannelError getCatChannelError() {
        return catChannelError;
    }

    public void setCatChannelError(CatChannelError catChannelError) {
        this.catChannelError = catChannelError;
    }
}
