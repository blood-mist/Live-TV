package androidtv.livetv.stb.entity;

import java.util.List;

public class FavUpdatedListEvent {
    public List<ChannelItem> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelItem> channels) {
        this.channels = channels;
    }

    private List<ChannelItem> channels;

    public List<CategoryItem> getCategoryItemList() {
        return categoryItemList;
    }

    public void setCategoryItemList(List<CategoryItem> categoryItemList) {
        this.categoryItemList = categoryItemList;
    }

    private List<CategoryItem> categoryItemList;
    public FavUpdatedListEvent(List<CategoryItem> categoryItemList, List<ChannelItem> channels) {
        this.channels=channels;
        this.categoryItemList=categoryItemList;
    }
}
