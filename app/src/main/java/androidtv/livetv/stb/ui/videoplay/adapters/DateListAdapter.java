package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.DateViewHolder;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.EpgViewHolder;
import androidtv.livetv.stb.utils.DateUtils;

public class DateListAdapter extends RecyclerView.Adapter<DateViewHolder> {
    private Context mContext;
    private List<Date> dateList;
    private DateClickLis listener;

    public DateListAdapter(Context context , List<Date> lis,DateClickLis listener){
        this.mContext = context;
        this.dateList = lis;
        this.listener = listener;
    }


    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.date_row_item, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        Date d = dateList.get(position);
        String txtToSet = DateUtils.smalldateFormat.format(d);
        holder.prgmDetails.setText(txtToSet);
        holder.layoutTxtImgHor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    holder.prgmDetails.setFont(mContext.getResources().getString(R.string.font_exo_Bold));
                    holder.prgmDetails.setScaleX(1.05f);
                    holder.prgmDetails.setScaleY(1.05f);
                }else{
                    holder.prgmDetails.setFont(mContext.getString(R.string.font_exo_Light));
                    holder.prgmDetails.setScaleX(1f);
                    holder.prgmDetails.setScaleY(1f);
                }
            }
        });

        holder.layoutTxtImgHor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(position,dateList.get(position));
            }
        });


    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public interface DateClickLis{
        void onClick(int postion,Date date);
    }
}
