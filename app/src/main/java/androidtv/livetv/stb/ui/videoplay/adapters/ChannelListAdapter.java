package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.MyChannelListViewHolder;
import androidtv.livetv.stb.utils.GlideApp;

/**
 * Adapter for channel list in live tv.
 */
public class ChannelListAdapter extends RecyclerView.Adapter<MyChannelListViewHolder> {

    private Context mContext;
    private List<ChannelItem> mList;
    private int positionSelected;
    private ChannelListClickListener listener;

    public int getPositionSelected() {
        return positionSelected;
    }

    public void setPositionSelected(int positionSelected) {
        this.positionSelected = positionSelected;
    }

    public ChannelListAdapter(Context context, List channels , ChannelListClickListener lis){
        this.mContext = context;
        this.mList = channels;
        this.listener = lis;
    }

    @NonNull
    @Override
    public MyChannelListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.channelist_row_item,parent,false);
        return  new MyChannelListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyChannelListViewHolder holder, int position) {
        ChannelItem item = mList.get(position);
        GlideApp.with(mContext).load(item.getChannelLogo())
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
                if(hasFocus){
                    listener.onChannelFocused(position);
                    holder.channelLogo.setScaleX(1.2f);
                    holder.channelLogo.setScaleY(1.05f);
                    holder.channelLogo.setAlpha(1f);
                    holder.view.setVisibility(View.INVISIBLE);
                }else{
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
                listener.onClickChannel(position);
            }
        });




    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public List<ChannelItem> getmList() {
        return mList;
    }

    public interface ChannelListClickListener {
        void onClickChannel(int position);
        void onChannelFocused(int position);
    }
}
