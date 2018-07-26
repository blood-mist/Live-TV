package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.MyChannelListViewHolder;
import androidtv.livetv.stb.utils.GlideApp;
import androidtv.livetv.stb.utils.LinkConfig;


/**
 * Adapter for channel list in live tv.
 */
public class ChannelListAdapter extends RecyclerView.Adapter<MyChannelListViewHolder> {

    private Context mContext;
    private List<ChannelItem> mList;
    private int positionSelected;
    private ChannelListClickListener listener;
    private int selectedPos;

    public int getPositionSelected() {
        return positionSelected;
    }

    public void setPositionSelected(int positionSelected) {
        this.positionSelected = positionSelected;
    }

    public ChannelListAdapter(Context context, ChannelListClickListener lis) {
        this.mContext = context;
        this.listener = lis;
    }


    public void setChannelItems(List<ChannelItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyChannelListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.channelist_row_item, parent, false);
        return new MyChannelListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyChannelListViewHolder holder, int position) {
        ChannelItem item = mList.get(position);
        GlideApp.with(mContext).load(LinkConfig.BASE_URL+LinkConfig.CHANNEL_LOGO_URL+item.getChannelLogo())
                .placeholder(R.drawable.placeholder_logo)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(holder.channelLogo);

//        if(position == positionSelected){
//            holder.channelLogo.setScaleX(1.2f);
//            holder.channelLogo.setScaleY(1.05f);
//            holder.channelLogo.setAlpha(1f);
//            holder.view.setVisibility(View.INVISIBLE);
//
//        }else{
//
//            holder.channelLogo.setScaleX(1f);
//            holder.channelLogo.setScaleY(1f);
//            holder.channelLogo.setAlpha(0.37f);
//            holder.view.setVisibility(View.VISIBLE);
//
//        }

        holder.relativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    listener.onChannelFocused(position);
                    holder.channelLogo.setScaleX(1.2f);
                    holder.channelLogo.setScaleY(1.05f);
                    holder.channelLogo.setAlpha(1f);
                    holder.view.setVisibility(View.INVISIBLE);
                } else {
                    holder.channelLogo.setScaleX(1f);
                    holder.channelLogo.setScaleY(1f);
                    holder.channelLogo.setAlpha(0.37f);
                    holder.view.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPos = position;
                listener.onClickChannel(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mList != null)
            return mList.size();
        else return 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
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

    /**
     * @param lm
     * @param direction
     * @return
     */
    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int nextSelectItem = selectedPos + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (nextSelectItem == 0 && nextSelectItem < getItemCount()) {
            notifyItemChanged(selectedPos);
            selectedPos = nextSelectItem;
            notifyItemChanged(selectedPos);
            lm.scrollToPosition(selectedPos);
            return true;
        }

        return false;
    }

    public List<ChannelItem> getmList() {
        return mList;
    }

    public interface ChannelListClickListener {
        void onClickChannel(int position);
        void onChannelFocused(int position);
    }
}
