package androidtv.livetv.stb.ui.videoplay.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.MyCategoryViewHolder;

import static androidtv.livetv.stb.utils.LinkConfig.CATEGORY_FAVORITE;


/**
 * Category Adapter
 */
public class CategoryAdapter extends RecyclerView.Adapter<MyCategoryViewHolder> {

    private final LayoutInflater mInflater;

    private List<CategoriesWithChannels> categoryItemList;
    private OnListClickListener mListener;
    private int selectedPos;
    private List<ChannelItem> allChannelList, allFavList;

    public void setAllChannelList(List<ChannelItem> allChannelList) {
        this.allChannelList = allChannelList;
    }


    /**
     * Constructer
     *
     * @param context
     */
    public CategoryAdapter(Context context, OnListClickListener lis) {
        mInflater = LayoutInflater.from(context);
        this.mListener = lis;
    }

    /**
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public MyCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.category_list_row, parent, false);
        return new MyCategoryViewHolder(v);
    }

    /**
     * To handle key event from remote
     *
     * @param recyclerView
     */
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


    public void setCategory(List<CategoriesWithChannels> categoryItems) {
        categoryItemList = categoryItems;
        notifyDataSetChanged();
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


    /**
     * bind view and data
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull MyCategoryViewHolder holder, int position) {
        int pos = holder.getAdapterPosition();
        int relative_position=0;
        CategoriesWithChannels categoryItem = null;
        /**
         * check the position of list
         * if position is 0 the created a category called ALl with
         * id = -1;
         */
        if (pos == 0) {
            holder.mTitleView.setText(R.string.all_channels);
            relative_position=0;
        } else {
            if (allFavList != null && allFavList.size() > 0) {
                if (pos == 1) {
                    holder.mTitleView.setText(CATEGORY_FAVORITE);
                    relative_position=1;
                } else {
                    if (categoryItemList != null) {
                        categoryItem = categoryItemList.get(position - 2);
                        relative_position=position-2;
                        holder.mTitleView.setText(categoryItem.categoryItem.getTitle());
                    } else {
                        holder.mTitleView.setText(R.string.no_category);
                    }
                }
            } else {
                if (categoryItemList != null) {
                    categoryItem = categoryItemList.get(position - 1);
                    relative_position=-1;
                    holder.mTitleView.setText(categoryItem.categoryItem.getTitle());
                } else {
                    holder.mTitleView.setText(R.string.no_category);
                }
            }

        }

        /**
         * final object created.
         */
        final CategoriesWithChannels finalCategoryItem = categoryItem;
        holder.mCategoryLayout.setOnClickListener(v -> {

            selectedPos = pos;
            if (pos == 0)
                mListener.onClickCategory("All Channels", pos,allChannelList);
            else {
                if (allFavList != null && allFavList.size() > 0) {
                    if (pos == 1)
                        mListener.onClickCategory(CATEGORY_FAVORITE, pos,allFavList);
                    else if (finalCategoryItem != null)
                        mListener.onClickCategory(finalCategoryItem.categoryItem.getTitle(),pos, finalCategoryItem.channelItemList);

                } else if (finalCategoryItem != null)
                    mListener.onClickCategory(finalCategoryItem.categoryItem.getTitle(), pos,finalCategoryItem.channelItemList);


            }
        });

        /**
         * when focus changes
         */
        holder.mCategoryLayout.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.mTitleView.setSelected(true);
                holder.mCategoryLayout.setScaleY(1.02f);
                mListener.onSelectCategory(pos,v);
            } else {
                holder.mTitleView.setSelected(false);
                holder.mCategoryLayout.setScaleX(1.0f);
            }
        });
        holder.mCategoryLayout.setTag(position);


    }

    public void addFavoriteItem(List<ChannelItem> favoriteList) {
        this.allFavList = favoriteList;
    }


    /**
     * @return list count
     */
    @Override
    public int getItemCount() {
        if (categoryItemList != null && allFavList != null && allFavList.size() > 0)
            return categoryItemList.size() + 2;
        else if (categoryItemList != null)
            return categoryItemList.size() + 1;
        else
            return 0;
    }

    /**
     * interface for  clicklistener
     */
    public interface OnListClickListener {

        void onClickCategory(String categoryName,int categoryPosition, List<ChannelItem> channels);
        void onSelectCategory(int position, View focusedCatView);
    }

}
