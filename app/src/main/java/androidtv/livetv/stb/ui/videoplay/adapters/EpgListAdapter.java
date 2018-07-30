package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.EpgViewHolder;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.MyChannelListViewHolder;
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
        holder.prgmTime.setText(getPrgmTime(epg.getStartTime(),epg.getEndTime()));


    }

    @Override
    public int getItemCount() {
        if(mList != null) return mList.size();
        else return 0;
    }

    public String getPrgmTime(Date startTime , Date endDate){
       String start = DateUtils._24HrsTimeFormat.format(startTime);
       String end = DateUtils._24HrsTimeFormat.format(endDate);
       return start +" - "+ end;
      }

}
