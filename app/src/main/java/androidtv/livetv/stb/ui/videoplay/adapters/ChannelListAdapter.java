package androidtv.livetv.stb.ui.videoplay.adapters;

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

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.utils.GlideApp;
import androidtv.livetv.stb.utils.LinkConfig;
import timber.log.Timber;

import static android.view.View.GONE;


/**
 * Adapter for channel list in live tv.
 */
public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.MyChannelListViewHolder> {

    private Context mContext;
    private List<ChannelItem> mList;
    private int positionSelected;
    private ChannelListClickListener listener;
    private int selectedPos;
    private RecyclerView recyclerView;

    public int getPositionSelected() {
        return positionSelected;
    }

    public void setPositionSelected(int positionSelected) {
        this.positionSelected = positionSelected;
//        notifyDataSetChanged();
    }

    public ChannelListAdapter(Context context, ChannelListClickListener lis) {
        this.mContext = context;
        this.listener = lis;
    }


    public void setChannelItems(List<ChannelItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MyChannelListViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if(getPositionSelected()==holder.getAdapterPosition()){
            holder.relativeLayout.requestFocus();

        }
    }

    @NonNull
    @Override
    public MyChannelListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.channelist_row_item, parent, false);
        return new MyChannelListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyChannelListViewHolder holder, int position) {
        ChannelItem item = mList.get(position);
        GlideApp.with(mContext).load(LinkConfig.BASE_URL + LinkConfig.CHANNEL_LOGO_URL + item.getChannelLogo())
                .placeholder(R.drawable.placeholder_logo)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(holder.channelLogo);

        if (item.getIs_fav() == 1)
            holder.fav.setVisibility(View.VISIBLE);
        else
            holder.fav.setVisibility(GONE);


        if (position == getPositionSelected()) {
            holder.relativeLayout.setSelected(true);
        } else {
            holder.relativeLayout.setSelected(false);
        }

        holder.relativeLayout.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                listener.onChannelFocused(position);
                holder.channelLogo.setScaleX(1.2f);
                holder.channelLogo.setScaleY(1.05f);
                holder.channelLogo.setAlpha(1f);
//                holder.view.setVisibility(View.INVISIBLE);
            } else {
                holder.channelLogo.setScaleX(1f);
                holder.channelLogo.setScaleY(1f);
                holder.channelLogo.setAlpha(0.37f);
//                holder.view.setVisibility(View.VISIBLE);
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
        this.recyclerView = recyclerView;
        recyclerView.setOnKeyListener((v, keyCode, event) -> {
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

    public class MyChannelListViewHolder extends RecyclerView.ViewHolder {
        public View view;
        RelativeLayout relativeLayout;
        public ImageView fav;
        ImageView channelLogo;

        MyChannelListViewHolder(View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.view);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
            fav = itemView.findViewById(R.id.fav);
            channelLogo = itemView.findViewById(R.id.img);


            relativeLayout.setOnClickListener(v -> {
                selectedPos = getAdapterPosition();
                listener.onClickChannel(getAdapterPosition());
            });
        }
    }

    public int getSelectedChannelPositionViaId(int channelId) {
        if (mList != null) {
            for (int i = 0; i < mList.size(); i++) {
                ChannelItem item = mList.get(i);
                if (item.getId() == channelId) {
                    return i;
                }
            }
        }
        return -1;
    }

}
