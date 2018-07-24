package androidtv.livetv.stb.ui.videoplay.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidtv.livetv.stb.R;

public class MyChannelListViewHolder extends RecyclerView.ViewHolder {
    public View view;
    public RelativeLayout relativeLayout;
    public ImageView fav;
    public ImageView channelLogo;
    public MyChannelListViewHolder(View itemView) {
        super(itemView);
        view = itemView.findViewById(R.id.view);
        relativeLayout = itemView.findViewById(R.id.relative_layout);
        fav = itemView.findViewById(R.id.fav);
        channelLogo = itemView.findViewById(R.id.img);
    }
}
