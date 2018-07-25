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
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.MyCategoryViewHolder;


/**
 * Category Adapter
 */
public class CategoryAdapter extends RecyclerView.Adapter<MyCategoryViewHolder> {
    private final LayoutInflater mInflater;

    private List<CategoryItem> categoryItemList;
    private OnListClickListener mListener;
    private int selectedPos;

    /**
     * Constructer
     *
     * @param context
     */
    public CategoryAdapter(Context context,OnListClickListener lis) {
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

    public void setCategory(List<CategoryItem> categoryItems) {
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
        CategoryItem categoryItem = null;
        /**
         * check the position of list
         * if position is 0 the created a category called ALl with
         * id = -1;
         */
        if (position == 0) {
            categoryItem = new CategoryItem();
            categoryItem.setId(-1);
            categoryItem.setTitle("ALL");
            holder.mTitleView.setText(categoryItem.getTitle());
        } else {
            if (categoryItemList != null) {
                categoryItem = categoryItemList.get(position - 1);
                holder.mTitleView.setText(categoryItem.getTitle());
            } else {
                holder.mTitleView.setText("No Categories Found");
            }
        }


        /**
         * final object created.
         */
        CategoryItem finalCategoryItem = categoryItem;
        holder.mCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPos = position;
                mListener.onClickCategory(finalCategoryItem);
            }
        });

        /**
         * when focus changes
         */
        holder.mCategoryLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    holder.mTitleView.setSelected(true);
                    holder.mCategoryLayout.setScaleY(1.02f);
                } else {
                    holder.mTitleView.setSelected(false);
                    holder.mCategoryLayout.setScaleX(1.0f);
                }
            }
        });


    }

    /**
     * @return list count
     */
    @Override
    public int getItemCount() {
        if (categoryItemList != null)
            return categoryItemList.size() + 1;
        else
            return 0;
    }

    public List<CategoryItem> getCategoryItemList() {
        return categoryItemList;
    }

    /**
     * interface for  clicklistener
     */
    public interface OnListClickListener {
        void onClickCategory(CategoryItem categoryItem);
    }

}
