package androidtv.livetv.stb.entity;

import java.util.List;

public class AllChannelsEvent {
    public List<ChannelItem> getChannelItemList() {
        return channelItemList;
    }

    public void setChannelItemList(List<ChannelItem> channelItemList) {
        this.channelItemList = channelItemList;
    }

    private List<ChannelItem> channelItemList;

    public List<CategoriesWithChannels> getCategoriesWithChannels() {
        return categoriesWithChannels;
    }

    public void setCategoriesWithChannels(List<CategoriesWithChannels> categoriesWithChannels) {
        this.categoriesWithChannels = categoriesWithChannels;
    }

    private List<CategoriesWithChannels> categoriesWithChannels;

    public AllChannelsEvent(List<ChannelItem> channelItemList, List<CategoriesWithChannels> categoriesWithChannels) {
        this.channelItemList=channelItemList;
        this.categoriesWithChannels=categoriesWithChannels;
    }
}
