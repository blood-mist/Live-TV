package androidtv.livetv.stb.ui.videoplay.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.ui.custom_views.CustomTextView;

public class DateViewHolder extends RecyclerView.ViewHolder {
    public CustomTextView prgmDetails;
    public LinearLayout layoutTxtImgHor;
    public DateViewHolder(View itemView) {
        super(itemView);
        this.prgmDetails = itemView.findViewById(R.id.txt_prgm_name);
        this.layoutTxtImgHor = itemView.findViewById(R.id.layout_txt_img_hor);
    }
}
