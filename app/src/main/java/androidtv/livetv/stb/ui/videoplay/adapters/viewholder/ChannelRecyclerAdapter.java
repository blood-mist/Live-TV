package androidtv.livetv.stb.ui.videoplay.adapters.viewholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.utils.GlideApp;
import androidtv.livetv.stb.utils.LinkConfig;


/**
 */

public class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerAdapter.ViewHolder> {
    public void setChannelList(List<ChannelItem> channelList) {
        this.channelList = channelList;
        notifyDataSetChanged();
    }

    private List<ChannelItem> channelList;
    private OnChannelListInteractionListener mListener;

    private Context mContext;
    private int focusedItem = 0;
    int tryFocusItem;

    public int getSelectedChannel() {
        return selectedChannel;
    }

    public void setSelectedChannel(int selectedChannelPos) {
        this.selectedChannel = selectedChannelPos;

    }

    private int selectedChannel=-1;

    public ChannelRecyclerAdapter(Context context,  ChannelRecyclerAdapter.OnChannelListInteractionListener mListener) {
        this.mContext = context;
        this.mListener = mListener;

    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }

                return false;
            }
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int nextSelectItem = selectedChannel + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (nextSelectItem == 0 && nextSelectItem <getItemCount()) {
            notifyItemChanged(selectedChannel);
            selectedChannel = nextSelectItem;
            lm.scrollToPosition(selectedChannel);
            notifyItemChanged(selectedChannel);
            return true;
        }

        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.epg_channel_row_list, parent,false);


        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ChannelRecyclerAdapter.ViewHolder holder, final int position) {

//        holder.itemView.setSelected(tryFocusItem==position);

        ChannelItem channel = channelList.get(position);
        GlideApp.with(mContext)
                .load(LinkConfig.BASE_URL+LinkConfig.CHANNEL_LOGO_URL+channel.getChannelLogo())
                .placeholder(R.drawable.placeholder_logo)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.channelImage);

        if (position == getSelectedChannel()) {
            holder.itemLayout.setSelected(true);
        } else {
            holder.itemLayout.setSelected(false);
        }


    }

    @Override
    public int getItemCount() {
        if(channelList != null)
        return channelList.size();
        else return 0;
    }

    public int getChannelPositionById(int selectedChannelId) {
        if(channelList != null && channelList.size()>0){
            for (int i = 0; i <channelList.size() ; i++) {
                ChannelItem item = channelList.get(i);
                if(selectedChannelId == item.getId()){
                   return i;
                }
            }
        }
        return 0;
    }

    public ChannelItem getChannelById(int selectedChannelId) {
        if(channelList != null && channelList.size()>0){
            for (int i = 0; i <channelList.size() ; i++) {
                ChannelItem item = channelList.get(i);
                if(selectedChannelId == item.getId()){
                    return item;
                }
            }
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout itemLayout;
        private ImageView channelImage;
        ViewHolder(final View itemView) {
            super(itemView);
            channelImage=itemView.findViewById(R.id.img);
            itemLayout = itemView.findViewById(R.id.relativeLayout);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onChannelClicked(channelList.get(getAdapterPosition()),getAdapterPosition());

                }
            });

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        itemLayout.setSelected(true);
                    }
                    else{
                        itemLayout.setSelected(false);
                    }
                }
            });

        }
    }


    private void onChannelClicked(ChannelItem channel, int adapterPosition) {
        mListener.onChannelClickInteraction(channel,adapterPosition);
    }

    public interface OnChannelListInteractionListener {
        void onChannelClickInteraction(ChannelItem channel, int adapterPosition);
    }

    public boolean hasData() {
        if (channelList != null) {
            return channelList.size() > 0;
        }
        else return false;
    }

    public ChannelItem getChannel(int selectedChannel){
        ChannelItem returnItem = null;
        for (ChannelItem item:channelList) {
            if(item.getId() == selectedChannel){
                returnItem = item;
            }
        }
        return returnItem;
    }

    public void setSelectedChannelViaId(int channelId){
        if(channelList != null){
            for (int i = 0;i<channelList.size();i++){
                ChannelItem item = channelList.get(i);
                if(item.getId() == channelId){
                    setSelectedChannel(i);
                }
            }
        }
    }





}
