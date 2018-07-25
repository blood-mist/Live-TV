package androidtv.livetv.stb.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class CategoriesWithChannels {
    @Embedded
    public CategoryItem categoryItem;

    @Relation(parentColumn = "category_id",entityColumn = "category_id",entity = ChannelItem.class)
    public List<ChannelItem> channelItemList;
}
