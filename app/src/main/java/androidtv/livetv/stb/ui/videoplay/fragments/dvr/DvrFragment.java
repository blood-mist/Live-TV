package androidtv.livetv.stb.ui.videoplay.fragments.dvr;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.DvrStartDateTimeEntity;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.TimeStampEntity;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.adapters.DateListAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.DvrListAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.ChannelRecyclerAdapter;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.FragmentMenu;
import androidtv.livetv.stb.utils.DataUtils;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class DvrFragment extends Fragment implements ChannelRecyclerAdapter.OnChannelListInteractionListener, DateListAdapter.DateClickLis, DvrListAdapter.OnClickDvrList {


    private ChannelRecyclerAdapter adapter;
    private DateListAdapter dateListAdapter;
    private List<Epgs> cuurentEpgList;
    private Date currentEpgDate;
    private DvrListAdapter dvrListAdapter;
    private FragmentDvrInteraction mListener;
    private Date selectedDate;
    private int selectedDatePosition = -1;
    private int selectedChannelId = 0;
    private Epgs currentPlayedEpg;
    private int currentEpgSelectedPosition = -1;

    public DvrFragment() {
        // Required empty public constructor
    }

    /*gvChannelList = (RecyclerView) view.findViewById(R.id.channel_list);
    gvEpgDvr = (GridView) view.findViewById(R.id.gv_prgms);
    txtPrgmTime = (TextView) view.findViewById(R.id.txt_on_air_prgm_time);
    txtPrgmName = (TextView) view.findViewById(R.id.txt_on_air_prgm_name);
    gvDate = (GridView) view.findViewById(R.id.gv_date);
    layoutDateEpg = (LinearLayout) view.findViewById(R.id.layout_date_epg);
    txtChannelName = (TextView) view.findViewById(R.id.txt_channel_name);


    txtOnAir = (TextView) view.findViewById(R.id.txt_on_air);
    txtDayView = (TextView) view.findViewById(R.id.txt_day);
    txtDateView = (TextView) view.findViewById(R.id.txt_date);*/

     @BindView(R.id.channel_list)
     RecyclerView gvChannelList;
     @BindView(R.id.gv_prgms)
     RecyclerView gvEpgDvr;
     @BindView(R.id.gv_date)
     RecyclerView gvDate;
     @BindView(R.id.txt_on_air_prgm_time)
     TextView txtPrgmTime;
     @BindView(R.id.txt_on_air_prgm_name)
     TextView txtPrgmName;
     @BindView(R.id.layout_date_epg)
     LinearLayout layoutDateEpg;
     @BindView(R.id.txt_channel_name)
     TextView txtChannelName;
     @BindView(R.id.txt_on_air)
     TextView txtOnAir;
     @BindView(R.id.txt_day)
     TextView txtDayView;
     @BindView(R.id.txt_date)
     TextView txtDateView;
    @BindView(R.id.noepg)
    TextView nOEpg;

     private DvrViewModel viewModel;
     private ChannelItem currentChannel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(DvrViewModel.class);
        currentEpgDate = Calendar.getInstance().getTime();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_dvr2, container, false);
        ButterKnife.bind(this,v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String currentDate = androidtv.livetv.stb.utils.DateUtils.dateAndTime.format(Calendar.getInstance().getTime());
        txtDateView.setText(currentDate);
        gvDate.setLayoutManager(new LinearLayoutManager(getActivity()));
        gvDate.setVisibility(View.GONE);
        adapter = new ChannelRecyclerAdapter(getContext(), this);
        dvrListAdapter = new DvrListAdapter(getActivity(),this);
        gvEpgDvr.setLayoutManager(new LinearLayoutManager(getActivity()));
        gvEpgDvr.setAdapter(dvrListAdapter);
        gvChannelList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        gvChannelList.setAdapter(adapter);
        viewModel.getChannels().observe(this, new Observer<List<ChannelItem>>() {
            @Override
            public void onChanged(@Nullable List<ChannelItem> channelItems) {
                adapter.setChannelList(channelItems);
                adapter.setSelectedChannel(adapter.getChannelPositionById(currentChannel.getId()));
                gvChannelList.scrollToPosition((adapter.getChannelPositionById(currentChannel.getId())));
                gvChannelList.requestFocus();
                onChannelClickInteraction(adapter.getChannelById(currentChannel.getId()),adapter.getChannelPositionById(currentChannel.getId()));
            }
        });

        View focusChannelChlid = null;
        gvChannelList.setNextFocusDownId(gvDate.getId());
        gvChannelList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

    }

    private List<Date> getDateList(String startDate) {
        Date current = Calendar.getInstance().getTime();
        return DataUtils.getParsedDateList(startDate);
    }

    @Override
    public void onChannelClickInteraction(ChannelItem channel, int adapterPosition) {
        setCurrentChannel(channel);
        txtChannelName.setText(channel.getName());
        dvrListAdapter.clear();
        gvDate.setVisibility(View.GONE);
        gvEpgDvr.setVisibility(View.GONE);
        Login login = GlobalVariables.login;
        nOEpg.setText("Loading...");
        nOEpg.setVisibility(View.VISIBLE);
        TimeStampEntity utc = GetUtc.getInstance().getTimestamp();
        LiveData<DvrStartDateTimeEntity> getDvrstartData=viewModel.getStartTime(login.getToken(),utc.getUtc(),String.valueOf(login.getId()),
                LinkConfig.getHashCode(String.valueOf(login.getId())
                        ,String.valueOf(utc.getUtc()),login.getSession()),1,
                String.valueOf(channel.getId()));
                getDvrstartData.observe(this, new Observer<DvrStartDateTimeEntity>() {
            @Override
            public void onChanged(@Nullable DvrStartDateTimeEntity dvrStartDateTimeEntity) {
                if (dvrStartDateTimeEntity != null) {
                    if (dvrStartDateTimeEntity.getStartDate() != null && dvrStartDateTimeEntity.getStartTime() != null) {
                        dateListAdapter = new DateListAdapter(getContext(), getDateList(dvrStartDateTimeEntity.getStartDate()), DvrFragment.this::onClick);
                        gvDate.setAdapter(dateListAdapter);
                        gvDate.setVisibility(View.VISIBLE);
                        setUpEpgs(channel, login, utc);

                    } else {
                        //TODO Show Error

                        gvEpgDvr.setVisibility(View.GONE);
                        gvDate.setVisibility(View.GONE);
                        nOEpg.setText("Dvr not available for this channel");
                        nOEpg.setVisibility(View.VISIBLE);

                    }

                }

            }
        });
    }

    private void setUpEpgs(ChannelItem channel, Login login, TimeStampEntity utc) {
        viewModel.getEpgs(login.getToken(), utc.getUtc(), String.valueOf(login.getId()), LinkConfig.getHashCode(String.valueOf(login.getId())
                , String.valueOf(utc.getUtc()), login.getSession()), String.valueOf(channel.getId())).observe(this, new Observer<List<Epgs>>() {
            @Override
            public void onChanged(@Nullable List<Epgs> epgs) {
                cuurentEpgList = epgs;
                if(selectedDatePosition != -1 &&  currentPlayedEpg != null && currentChannel.getId() == currentPlayedEpg.getChannelID() ) {
                    dateListAdapter.setPositionClicked(selectedDatePosition);
                    onClick(selectedDatePosition,dateListAdapter.getDateList().get(selectedDatePosition));
                    dvrListAdapter.setEpg(currentPlayedEpg);
                    gvEpgDvr.scrollToPosition(dvrListAdapter.getPosition(currentPlayedEpg));
                    if(currentEpgSelectedPosition != -1 ){
                        dvrListAdapter.setSelectedFocusedPosition(currentEpgSelectedPosition);
                    }



                }else{
                    dateListAdapter.setPositionClicked(dateListAdapter.getItemCount()-1);
                    setUpAdapter(epgs);
                }
            }
        });
    }

    private void setUpAdapter(List<Epgs> epgs) {
        if (epgs != null) {
            List<Epgs> newList = getFilteredEpgs(epgs);
            if(newList.size()>0) {
                dvrListAdapter.setmList(newList);
                gvEpgDvr.setVisibility(View.VISIBLE);

                    if(currentPlayedEpg != null){

                }else {
                    gvEpgDvr.scrollToPosition(newList.size()-1);
                }
                nOEpg.setVisibility(View.GONE);
            }else{
                showNoEpg();
            }
        }else{
            showNoEpg();
        }
    }

    private void showNoEpg() {
        gvEpgDvr.setVisibility(View.GONE);
        nOEpg.setVisibility(View.VISIBLE);
    }

    private List<Epgs> getFilteredEpgs(List<Epgs> epgs) {
        List<Epgs> newList = new ArrayList<>();
        for (Epgs epg : epgs) {
            if (checkDate(epg.getDate())) {
                Calendar currentTime = Calendar.getInstance();
                Date currentDate = currentTime.getTime();
                if (epg.getStartTime().before(currentDate)) {
                    newList.add(epg);
                }
            }

        }

        if(newList.size()<=0){
            Epgs epgs1 = new Epgs();

            epgs1.setProgramTitle("MISC");
            epgs1.setChannelID(currentChannel.getId());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentEpgDate);
            calendar.set(Calendar.HOUR_OF_DAY,00);
            calendar.set(Calendar.MINUTE,00);
            epgs1.setStartTime(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            epgs1.setEndTime(calendar.getTime());
            epgs1.setDate(currentEpgDate);
            epgs1.setId(currentChannel.getId()+String.valueOf(epgs1.getStartTime()));
            newList.add(epgs1);
        }


        return newList ;
    }

    private boolean checkDate(Date date) {
        String epgDate = androidtv.livetv.stb.utils.DateUtils.dateAndTime.format(date);
        String current = androidtv.livetv.stb.utils.DateUtils.dateAndTime.format(currentEpgDate);
        return epgDate.equals(current);
    }

    @Override
    public void onClick(int postion, Date date) {
        currentEpgDate = date;
        setUpAdapter(cuurentEpgList);
//        gvDate.getChildAt(postion).requestFocus();
        gvDate.smoothScrollToPosition(postion);
    }



    @Override
    public void clickDvr(Epgs epg, int position) {
        Log.d("dvr","clicked :"+epg.getProgramTitle());
        mListener.playDvr(epg,currentChannel);
        selectedDatePosition = dateListAdapter.getPositionClicked();
        currentPlayedEpg = epg;
        currentEpgSelectedPosition = position;
        dvrListAdapter.setEpg(currentPlayedEpg);

    }

    @Override
    public void onOnAirSetup(Epgs epg) {
        if(currentChannel.getId() == epg.getChannelID()) {
            txtPrgmName.setText(epg.getProgramTitle());
            txtPrgmTime.setText(DataUtils.getPrgmTime(epg.getStartTime(), epg.getEndTime()));
        }else{
            txtPrgmName.setText("");
            txtPrgmTime.setText("");
        }
    }

    private String getChannelName(int channelID) {
        ChannelItem item = adapter.getChannel(channelID);
        return item.getName();
    }

    @Override
    public void onAirClick(Epgs epgs) {
        mListener.playChannelFromOnAir(adapter.getChannel(epgs.getChannelID()),true);
    }

    public void setCurrentChannel(ChannelItem currentChannel) {
        this.currentChannel = currentChannel;


    }

    public interface FragmentDvrInteraction {
        void playChannelFromOnAir(ChannelItem channel ,boolean onAir);
        void playDvr(Epgs epgs,ChannelItem item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            if(getView() != null )
            getView().requestFocus();
            if(dvrListAdapter != null)
            dvrListAdapter.notifyDataSetChanged();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentMenu.FragmentMenuInteraction) {
            mListener = (FragmentDvrInteraction) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }



    }
}
