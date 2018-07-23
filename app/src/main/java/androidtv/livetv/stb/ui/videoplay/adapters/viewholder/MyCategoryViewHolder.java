package androidtv.livetv.stb.ui.videoplay.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidtv.livetv.stb.R;

public class MyCategoryViewHolder extends RecyclerView.ViewHolder {
    public TextView mTitleView;
    public LinearLayout mCategoryLayout;
    public MyCategoryViewHolder(View itemView) {
        super(itemView);
        mTitleView = itemView.findViewById(R.id.category_title);
        mCategoryLayout = itemView.findViewById(R.id.channelCategory_layout);
    }
}
