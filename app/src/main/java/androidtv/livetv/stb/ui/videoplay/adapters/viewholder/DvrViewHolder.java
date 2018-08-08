package androidtv.livetv.stb.ui.videoplay.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidtv.livetv.stb.R;

public class DvrViewHolder extends RecyclerView.ViewHolder {
    public LinearLayout LayoutTxtImgHor;
    public TextView prgmName;
    public TextView prgmTime;
    public ImageView alarmPlay;
    public TextView onAirText;
    public DvrViewHolder(View itemView) {
        super(itemView);
        LayoutTxtImgHor = (LinearLayout)itemView.findViewById(R.id.layout_txt_img_hor);
        prgmName = (TextView) itemView
                .findViewById(R.id.txt_prgm_name);
        prgmTime = (TextView) itemView
                .findViewById(R.id.txt_prgm_time);
        alarmPlay = (ImageView) itemView.findViewById(R.id.ico_alarm_play);
        onAirText=(TextView) itemView.findViewById(R.id.on_air_text);

    }
}
