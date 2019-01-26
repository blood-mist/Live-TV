package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.ChannelItem;

import static androidtv.livetv.stb.utils.LinkConfig.CATEGORY_FAVORITE;

public class GridCategoryAdapter extends RecyclerView.Adapter<GridCategoryAdapter.MyCategoryViewHolder> {

    private final LayoutInflater mInflater;

    private List<CategoriesWithChannels> categoryItemList;
    private OnListClickListener mListener;
    private int selectedPos;
    private RecyclerView recyclerView;
    private List<ChannelItem> allFavList,allChannelList;

    public View getSelectedCategoryView() {
        return selectedCategoryView;
    }

    public void setSelectedCategoryView(View selectedCategoryView) {
        this.selectedCategoryView = selectedCategoryView;
    }

    private View selectedCategoryView;


    /**
     * Constructer
     *
     * @param context
     */
    public GridCategoryAdapter(Context context, OnListClickListener lis) {
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
        View v = mInflater.inflate(R.layout.category_list_grid, parent, false);
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
        this.recyclerView=recyclerView;
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
        CategoriesWithChannels categoryItem;
        /**
         * check the position of list
         * if position is 0 the created a category called ALl with
         * id = -1;
         */
        if (position == 0) {
            holder.mTitleView.setText(R.string.all_channels);
        } else {
            if (allFavList != null && allFavList.size() > 0) {
                if (position == 1) {
                    holder.mTitleView.setText(CATEGORY_FAVORITE);
                } else {
                    if (categoryItemList != null) {
                        categoryItem = categoryItemList.get(position - 2);
                        holder.mTitleView.setText(categoryItem.categoryItem.getTitle());
                    } else {
                        holder.mTitleView.setText(R.string.no_category);
                    }
                }
            } else {
                if (categoryItemList != null) {
                    categoryItem = categoryItemList.get(position - 1);
                    holder.mTitleView.setText(categoryItem.categoryItem.getTitle());
                } else {
                    holder.mTitleView.setText(R.string.no_category);
                }
            }

        }
        holder.mCategoryLayout.setTag(position);


        /**
         * final object created.
         */
    }
    public void setAllChannelList(List<ChannelItem> allChannelList) {
        this.allChannelList = allChannelList;
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


    class MyCategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleView;
        private LinearLayout mCategoryLayout;
        CategoriesWithChannels finalCategoryItem ;
        private MyCategoryViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.grid_cat_textView);
            mCategoryLayout = itemView.findViewById(R.id.grid_item_layout);

            mCategoryLayout.setOnClickListener(v -> {

                selectedPos = getAdapterPosition();
                if (getAdapterPosition() == 0)
                    mListener.onClickCategory("All Channels", getAdapterPosition(),allChannelList);
                else {
                    if (allFavList != null && allFavList.size() > 0) {
                        if (getAdapterPosition() == 1)
                            mListener.onClickCategory(CATEGORY_FAVORITE, getAdapterPosition(),allFavList);
                        else if(categoryItemList!=null) {
                            finalCategoryItem = categoryItemList.get(getAdapterPosition() - 2);
                            mListener.onClickCategory(finalCategoryItem.categoryItem.getTitle(), getAdapterPosition(), finalCategoryItem.channelItemList);
                        }

                    } else if (categoryItemList != null) {
                        finalCategoryItem = categoryItemList.get(getAdapterPosition() - 1);
                        mListener.onClickCategory(finalCategoryItem.categoryItem.getTitle(), getAdapterPosition(), finalCategoryItem.channelItemList);
                    }


                }
            });


            /**
             * when focus changes
             */
            mCategoryLayout.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
//                    recyclerView.smoothScrollToPosition(getAdapterPosition());
                    mTitleView.setSelected(true);
                    mCategoryLayout.setScaleY(1.02f);
                    ViewCompat.setElevation(mCategoryLayout,10);
                    mListener.onSelectCategory(getAdapterPosition(),v);
                    setSelectedCategoryView(mCategoryLayout);
                } else {
                    mTitleView.setSelected(false);
                    ViewCompat.setElevation(mCategoryLayout,0);
                    mCategoryLayout.setScaleX(1.0f);
                }
            });
        }
    }


}