package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.EpgViewHolder;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.MyChannelListViewHolder;
import androidtv.livetv.stb.utils.DataUtils;
import androidtv.livetv.stb.utils.DateUtils;


public class EpgListAdapter extends RecyclerView.Adapter<EpgViewHolder> {

    private List<Epgs> mList;
    private Context mContext;

    public EpgListAdapter(Context context){
        this.mContext = context;
    }

    public void setmList(List<Epgs> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.epg_row_item, parent, false);
        return new EpgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpgViewHolder holder, int position) {
        Epgs epg = mList.get(position);
        holder.prgmName.setText(epg.getProgramTitle());
        holder.prgmTime.setText(DataUtils.getPrgmTime(epg.getStartTime(),epg.getEndTime()));
        Calendar currentCal = Calendar.getInstance();
        Date currentDateTime = currentCal.getTime();
        if((epg.getStartTime().before(currentDateTime) && epg.getEndTime().after(currentDateTime)) || epg.getStartTime() == currentDateTime || epg.getEndTime() == currentDateTime){

            holder.alarmPlay.setImageResource(R.drawable.red_circle);
            holder.onAirText.setText("ON AIR");
            holder.LayoutTxtImgHor.setBackgroundColor(mContext.getResources().getColor(R.color.transp));

        }else{
            holder.LayoutTxtImgHor.setBackgroundColor(mContext.getResources().getColor(R.color.epg_transp));
            holder.alarmPlay.setImageResource(R.drawable.icon_alarm);
            holder.onAirText.setText("");

        }



    }

    @Override
    public int getItemCount() {
        if(mList != null) return mList.size();
        else return 0;
    }



    public void clear() {
        mList = null;
        notifyDataSetChanged();
    }
}
