package androidtv.livetv.stb.entity;

public class ChannelInserted {
    private boolean isInserted;
    public ChannelInserted(Boolean isInserted) {
        this.isInserted=isInserted;
    }

    public boolean isInserted() {
        return isInserted;
    }

    public void setInserted(boolean inserted) {
        isInserted = inserted;
    }
}
