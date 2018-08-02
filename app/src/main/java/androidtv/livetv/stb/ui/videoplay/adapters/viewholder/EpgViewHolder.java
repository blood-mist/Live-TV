package androidtv.livetv.stb.ui.videoplay.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidtv.livetv.stb.R;

public class EpgViewHolder extends RecyclerView.ViewHolder {
    public LinearLayout LayoutTxtImgHor;
    public TextView prgmName;
    public TextView prgmTime;
    public ImageView alarmPlay;
    public TextView onAirText;

    public EpgViewHolder(View itemView) {
        super(itemView);
        this.LayoutTxtImgHor = itemView.findViewById(R.id.layout_txt_img_hor);
        this.prgmName = itemView.findViewById(R.id.txt_prgm_name);
        this.prgmTime = itemView.findViewById(R.id.txt_prgm_time);
        this.alarmPlay = itemView.findViewById(R.id.ico_alarm_play);
        this.onAirText = itemView.findViewById(R.id.on_air_text);
    }
}
