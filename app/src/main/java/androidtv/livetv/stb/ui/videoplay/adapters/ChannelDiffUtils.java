package androidtv.livetv.stb.ui.videoplay.adapters;


import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

import androidtv.livetv.stb.entity.ChannelItem;

public class ChannelDiffUtils extends DiffUtil.Callback {
    List<ChannelItem> oldChannel;
    List<ChannelItem> newChannel;
    public ChannelDiffUtils(List<ChannelItem> oldItems, List<ChannelItem> newItems) {
       this.oldChannel=oldItems;
       this.newChannel=newItems;
    }

    @Override
    public int getOldListSize() {
        return oldChannel.size();
    }

    @Override
    public int getNewListSize() {
        return newChannel.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldChannel.get(oldItemPosition).getId() == newChannel.get(newItemPosition).getId();
    }
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldChannel.get(oldItemPosition).equals(newChannel.get(newItemPosition));
    }
    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
