package androidtv.livetv.stb.ui.videoplay.fragments.dvr;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
import androidtv.livetv.stb.utils.DateUtils;
import androidtv.livetv.stb.utils.GlideApp;
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

    public Epgs getCurrentPlayedEpg() {
        return currentPlayedEpg;
    }

    public void setCurrentPlayedEpg(Epgs currentPlayedEpg) {
        this.currentPlayedEpg = currentPlayedEpg;
    }

    private Epgs currentPlayedEpg;
    private int currentEpgSelectedPosition = -1;
    private List<ChannelItem> allChannelItems;

    public DvrFragment() {
        // Required empty public constructor
    }


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
    @BindView(R.id.img_dvr_channel_onAir)
    ImageView dvrOnAirChLogo;
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


    public void setSelectedDvrPosition(int selectedDvrPosition) {
        this.currentEpgSelectedPosition = selectedDvrPosition;
    }

    public void setGetSelectedDatePosition(int getSelectedDatePosition) {
        this.selectedDatePosition = getSelectedDatePosition;
    }

    private int getSelectedDatePosition;

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
        View v = inflater.inflate(R.layout.fragment_dvr2, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Date currentDate=Calendar.getInstance().getTime();
        String currentDay= DateUtils.fullDayFormat.format(currentDate);
        String currentExpectedDate = DateUtils.daymonthFormat.format(currentDate);
        txtDayView.setText(currentDay);
        txtDateView.setText(currentExpectedDate);
        gvDate.setLayoutManager(new LinearLayoutManager(getActivity()));
        gvDate.setVisibility(View.GONE);
        adapter = new ChannelRecyclerAdapter(getContext(), this);
        dvrListAdapter = new DvrListAdapter(getActivity(), this);
        gvEpgDvr.setLayoutManager(new LinearLayoutManager(getActivity()));
        gvEpgDvr.setAdapter(dvrListAdapter);
        gvChannelList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        gvChannelList.setAdapter(adapter);
        adapter.setChannelList(getFilteredChannelList(allChannelItems));
        adapter.setSelectedChannel(adapter.getChannelPositionById(currentChannel.getId()));
        gvChannelList.scrollToPosition((adapter.getChannelPositionById(currentChannel.getId())));
//        gvChannelList.requestFocus();
        onChannelClickInteraction(adapter.getChannelById(currentChannel.getId()), adapter.getChannelPositionById(currentChannel.getId()));
        View focusChannelChlid = null;
        gvChannelList.setNextFocusDownId(gvDate.getId());
        gvChannelList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

    }

    private List<ChannelItem> getFilteredChannelList(List<ChannelItem> allChannelItems) {
        List<ChannelItem> filteredItems = new ArrayList<>();
        for (ChannelItem item : allChannelItems) {
            if (item.isHasDvr()) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    private List<Date> getDateList(String startDate) {
        Date current = Calendar.getInstance().getTime();
        return DataUtils.getParsedDateList(startDate);
//        List<Date> dates = new ArrayList<>();
//        for (int i = 4; i > 0; i--) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DATE, -1);
//            dates.add(calendar.getTime());
//        }
//
//        dates.add(Calendar.getInstance().getTime());
//        return dates;
    }

    @Override
    public void onChannelClickInteraction(ChannelItem channel, int adapterPosition) {
        setCurrentChannel(channel);
        GlideApp.with(Objects.requireNonNull(getActivity()))
                .asBitmap()
                .load(LinkConfig.BASE_URL+LinkConfig.CHANNEL_LOGO_URL+channel.getChannelLogo())
                .placeholder(R.drawable.placeholder_logo)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(dvrOnAirChLogo);
        dvrListAdapter.clear();
        gvDate.setVisibility(View.GONE);
        gvEpgDvr.setVisibility(View.GONE);
        Login login = GlobalVariables.login;
        nOEpg.setText("Loading...");
        nOEpg.setVisibility(View.VISIBLE);
        TimeStampEntity utc = GetUtc.getInstance().getTimestamp();
        dateListAdapter = new DateListAdapter(getContext(), getDateList(""), DvrFragment.this);
        gvDate.setAdapter(dateListAdapter);
        gvDate.setVisibility(View.VISIBLE);
        /*gvDate.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gvDate.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            gvDate.findViewHolderForAdapterPosition(dateListAdapter.getItemCount() - 1).itemView.requestFocus();
                        } catch (Exception e) {
                            try {
                                gvDate.findViewHolderForLayoutPosition(dateListAdapter.getItemCount() - 1).itemView.requestFocus();
                            } catch (Exception e1) {
                                gvDate.getChildAt(0).requestFocus();
                            }
                        }
                    }
                }, 1000);
            }
        });*/
        setUpEpgs(channel, login, utc);
    }

    private void setUpEpgs(@NonNull ChannelItem channel, Login login, TimeStampEntity utc) {
        if (channel.getChannelEpgName() == null || channel.getChannelEpgName().isEmpty()) {
            dateListAdapter.setPositionClicked(dateListAdapter.getItemCount() - 1);
            setUpAdapter(null, dateListAdapter.getDateList().get(dateListAdapter.getItemCount() - 1));
            return;
        }
        viewModel.getEpgs(channel.getChannelEpgName(), login.getToken(), String.valueOf(channel.getId()), getRequiredDateFormat()).observe(this, new Observer<List<Epgs>>() {
            @Override
            public void onChanged(@Nullable List<Epgs> epgs) {
                if (epgs != null) {
                    cuurentEpgList = epgs;
                    if (selectedDatePosition != -1 && currentPlayedEpg != null && currentChannel.getId() == currentPlayedEpg.getChannelID()) {
                        dateListAdapter.setPositionClicked(selectedDatePosition);
                        onClick(selectedDatePosition, dateListAdapter.getDateList().get(selectedDatePosition));
                        dvrListAdapter.setEpg(currentPlayedEpg);
                        gvEpgDvr.scrollToPosition(dvrListAdapter.getPosition(currentPlayedEpg));
                        if (currentEpgSelectedPosition != -1) {
                            dvrListAdapter.setSelectedFocusedPosition(currentEpgSelectedPosition);
                        }
                    } else {
                        dateListAdapter.setPositionClicked(dateListAdapter.getItemCount() - 1);
                        setUpAdapter(epgs, dateListAdapter.getDateList().get(dateListAdapter.getItemCount() - 1));
                    }
                }
            }
        });
    }

    private String getRequiredDateFormat() {
        SimpleDateFormat epgDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return epgDateFormat.format(Calendar.getInstance().getTime());
    }

    private void setUpAdapter(List<Epgs> epgs, Date currentEpgDate) {
        List<Epgs> newList = getFilteredEpgs(epgs, currentEpgDate);
        if (newList.size() > 0) {
            dvrListAdapter.setmList(newList);
            gvEpgDvr.setVisibility(View.VISIBLE);
            if (currentPlayedEpg != null && currentChannel.getId() == currentPlayedEpg.getChannelID()) {

            } else {
                dvrListAdapter.setSelectedFocusedPosition(dvrListAdapter.getItemCount() - 1);
                gvEpgDvr.scrollToPosition(dvrListAdapter.getItemCount() - 1);
            }
            nOEpg.setVisibility(View.GONE);
        } else {
            showNoEpg();
        }
    }

    private void showNoEpg() {
        gvEpgDvr.setVisibility(View.GONE);
        nOEpg.setVisibility(View.VISIBLE);
    }

    private List<Epgs> getFilteredEpgs(List<Epgs> epgs, Date currentEpgDate) {
        List<Epgs> newList = new ArrayList<>();
        if (epgs != null) {
            for (Epgs epg : epgs) {
                if (checkDate(epg.getDate(), currentEpgDate)) {
                    Calendar currentTime = Calendar.getInstance();
                    Date currentDate = currentTime.getTime();
                    if (epg.getStartTime().before(currentDate)) {
                        newList.add(epg);
                    }
                }

            }
        }

        if (newList.size() <= 0) {
            Epgs epgs1 = new Epgs();
            epgs1.setProgramTitle("MISC");
            epgs1.setChannelID(currentChannel.getId());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentEpgDate);
            calendar.set(Calendar.HOUR_OF_DAY, 00);
            calendar.set(Calendar.MINUTE, 00);
            epgs1.setStartTime(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            epgs1.setEndTime(calendar.getTime());
            epgs1.setDate(currentEpgDate);
            epgs1.setId(currentChannel.getId() + String.valueOf(epgs1.getStartTime()));
            newList.add(epgs1);
        }


        return newList;
    }

    private boolean checkDate(Date date, Date currentEpgDate) {
        String epgDate = androidtv.livetv.stb.utils.DateUtils.dateAndTime.format(date);
        String current = androidtv.livetv.stb.utils.DateUtils.dateAndTime.format(currentEpgDate);
        return epgDate.equals(current);
    }

    @Override
    public void onClick(int postion, Date date) {
        currentEpgDate = date;
        setUpAdapter(cuurentEpgList, date);
        gvDate.smoothScrollToPosition(postion);
    }

    @Override
    public void clickDvr(Epgs epg, int position) {
        Log.d("dvr", "clicked :" + epg.getProgramTitle());
        selectedDatePosition = dateListAdapter.getPositionClicked();
        currentPlayedEpg = epg;
        currentEpgSelectedPosition = position;
        dvrListAdapter.setEpg(currentPlayedEpg);
        mListener.playDvr(epg, currentChannel, position, selectedDatePosition);
    }

    @Override
    public void onOnAirSetup(Epgs epg) {
        if (currentChannel.getId() == epg.getChannelID()) {
            txtPrgmName.setText(epg.getProgramTitle());
            txtPrgmTime.setText(DataUtils.getPrgmTime(epg.getStartTime(), epg.getEndTime()));
        } else {
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
        mListener.playChannelFromOnAir(adapter.getChannel(epgs.getChannelID()), allChannelItems.indexOf(currentChannel), true);
    }

    public void setCurrentChannel(ChannelItem currentChannel) {
        this.currentChannel = currentChannel;
    }

    public void setAllChannelList(List<ChannelItem> allChannelItems) {
        this.allChannelItems = allChannelItems;
    }

    public interface FragmentDvrInteraction {
        void playChannelFromOnAir(ChannelItem channel, int channelPositionById, boolean onAir);

        void playDvr(Epgs epgs, ChannelItem item, int selectedDvr, int dateSelected);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (getView() != null)
                getView().requestFocus();
            if (dvrListAdapter != null)
                dvrListAdapter.setSelectedFocusedPosition(currentEpgSelectedPosition);
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
