package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.DvrViewHolder;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.EpgViewHolder;
import androidtv.livetv.stb.utils.DataUtils;
import androidtv.livetv.stb.utils.DateUtils;

public class DvrListAdapter extends RecyclerView.Adapter<DvrViewHolder> {
    private Context mContext;
    private List<Epgs> mList;
    private OnClickDvrList listener;

    public DvrListAdapter(Context context, OnClickDvrList lis) {
        this.mContext = context;
        this.listener = lis;
    }


    @NonNull
    @Override
    public DvrViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dvr_row_item, parent, false);
        return new DvrViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DvrViewHolder holder, int position) {

        Epgs epg = mList.get(position);
        holder.prgmName.setText(epg.getProgramTitle());
        holder.prgmTime.setText(DataUtils.getPrgmTime(epg.getStartTime(), epg.getEndTime()));
        Calendar currentCal = Calendar.getInstance();
        Date currentDateTime = currentCal.getTime();
        if ((epg.getStartTime().before(currentDateTime) && epg.getEndTime().after(currentDateTime)) || epg.getStartTime() == currentDateTime || epg.getEndTime() == currentDateTime) {
            holder.LayoutTxtImgHor.setTag("12");
            holder.alarmPlay.setImageResource(R.drawable.red_circle);
            holder.onAirText.setText("ON AIR");
            holder.LayoutTxtImgHor.setBackgroundColor(mContext.getResources().getColor(R.color.transp));
            listener.onOnAirSetup(epg);

        } else {
            holder.LayoutTxtImgHor.setBackgroundColor(mContext.getResources().getColor(R.color.epg_transp));
            holder.alarmPlay.setImageResource(R.drawable.play);
            holder.onAirText.setText("");
            holder.LayoutTxtImgHor.setTag("10");

        }
        holder.LayoutTxtImgHor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    holder.LayoutTxtImgHor.setBackgroundColor(mContext.getResources().getColor(R.color.darkgrey));
                } else{
                    holder.LayoutTxtImgHor.setBackgroundColor(mContext.getResources().getColor(R.color.epg_transp));
                }
            }
        });
        holder.LayoutTxtImgHor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getTag() == "12"){
                    listener.onAirClick(epg);
                }else {
                    listener.clickDvr(epg);

                }
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        } else {
            return 0;
        }
    }

    public void setmList(List<Epgs> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void clear() {
        if(mList != null && mList.size()>0){
            mList.clear();
            notifyDataSetChanged();
        }
    }


    public interface OnClickDvrList {
        void clickDvr(Epgs epg);
        void onOnAirSetup(Epgs epg);
        void onAirClick(Epgs epgs);
    }


}
