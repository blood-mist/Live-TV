package androidtv.livetv.stb.ui.videoplay.fragments.epg;


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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.TimeStampEntity;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.adapters.DateListAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.EpgListAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.ChannelRecyclerAdapter;
import androidtv.livetv.stb.ui.videoplay.fragments.menu.FragmentMenu;
import androidtv.livetv.stb.utils.DataUtils;
import androidtv.livetv.stb.utils.DateUtils;
import androidtv.livetv.stb.utils.DisposableManager;
import androidtv.livetv.stb.utils.GlideApp;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class EpgFragment extends Fragment implements ChannelRecyclerAdapter.OnChannelListInteractionListener, DateListAdapter.DateClickLis, EpgListAdapter.EpgListAdapterListener {


    private FragmentEpgInteraction mListener;
    private LiveData<List<Epgs>> epgEntityLive = null;
    private ChannelItem currentSelectedChannel;
    private List<ChannelItem> allChannelItems;

    public ChannelItem getCurrentSelectedChannel() {
        return currentSelectedChannel;
    }

    public void setCurrentSelectedChannel(ChannelItem currentSelectedChannel) {
        this.currentSelectedChannel = currentSelectedChannel;
    }

    public void setSelectedChannelId(int selectedChannelId) {
        this.selectedChannelId = selectedChannelId;
    }

    private int selectedChannelId;

    public EpgFragment() {
        // Required empty public constructor
    }

    /**
     * gvChannelList = (RecyclerView) view.findViewById(R.id.channel_list);
     * gvEpgDvr = (GridView) view.findViewById(R.id.gv_prgms);
     * txtPrgmTime = (TextView) view.findViewById(R.id.txt_on_air_prgm_time);
     * txtPrgmName = (TextView) view.findViewById(R.id.txt_on_air_prgm_name);
     * gvDate = (GridView) view.findViewById(R.id.gv_date);
     * layoutDateEpg = (LinearLayout) view.findViewById(R.id.layout_date_epg);
     * txtChannelName = (TextView) view.findViewById(R.id.txt_channel_name);
     * <p>
     * <p>
     * txtOnAir = (TextView) view.findViewById(R.id.txt_on_air);
     * txtDayView = (TextView) view.findViewById(R.id.txt_day);
     * txtDateView = (TextView) view.findViewById(R.id.txt_date);
     */


    @BindView(R.id.channel_list)
    RecyclerView gvChannelList;
    @BindView(R.id.gv_prgms)
    RecyclerView gvEpgDvr;
    @BindView(R.id.txt_on_air_prgm_time)
    TextView txtPrgmTime;
    @BindView(R.id.txt_on_air_prgm_name)
    TextView txtPrgmName;
    @BindView(R.id.gv_date)
    RecyclerView gvDate;
    @BindView(R.id.layout_date_epg)
    LinearLayout layoutDateEpg;
    @BindView(R.id.img_epg_channel_onAir)
    ImageView onAirChlogo;
    @BindView(R.id.txt_on_air)
    TextView txtOnAir;
    @BindView(R.id.txt_day)
    TextView txtDayView;
    @BindView(R.id.txt_date)
    TextView txtDateView;
    @BindView(R.id.progressBar1)
    AVLoadingIndicatorView progressBar;

    @BindView(R.id.noepg)
    TextView nOEpg;

    private EpgViewModel viewModel;
    private ChannelRecyclerAdapter adapter;
    private EpgListAdapter epgListAdapter;
    private Date currentEpgDate;
    private List<Epgs> cuurentEpgList;
    private DateListAdapter dateListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(EpgViewModel.class);
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_epg, container, false);
        ButterKnife.bind(this, view);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentEpgDate = Calendar.getInstance().getTime();
        Date currentDate=Calendar.getInstance().getTime();
        String currentDay=DateUtils.fullDayFormat.format(currentDate);
        String currentExpectedDate = DateUtils.daymonthFormat.format(currentDate);
        txtDayView.setText(currentDay);
        txtDateView.setText(currentExpectedDate);
        adapter = new ChannelRecyclerAdapter(getContext(), this);
        adapter.setSelectedChannelId(selectedChannelId);
        gvChannelList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        gvChannelList.setAdapter(adapter);
        adapter.setChannelList(getFilteredChannelList(allChannelItems));
        adapter.setSelectedChannel(adapter.getChannelPositionById(getCurrentSelectedChannel().getId()));
        gvChannelList.scrollToPosition((adapter.getChannelPositionById(getCurrentSelectedChannel().getId())));

        dateListAdapter = new DateListAdapter(getActivity(), getDateList(), this);
        gvDate.setLayoutManager(new LinearLayoutManager(getActivity()));
        gvDate.setAdapter(dateListAdapter);
        dateListAdapter.setPositionClicked(0);
        epgListAdapter = new EpgListAdapter(getActivity(), this);
        gvEpgDvr.setLayoutManager(new LinearLayoutManager(getActivity()));
        gvEpgDvr.setAdapter(epgListAdapter);

        onChannelClickInteraction(adapter.getChannelById(getCurrentSelectedChannel().getId()), adapter.getChannelPositionById(getCurrentSelectedChannel().getId()));

    }
    private List<ChannelItem> getFilteredChannelList(List<ChannelItem> allChannelItems) {
        List<ChannelItem> filteredItems =  new ArrayList<>();
        for(ChannelItem item:allChannelItems){
            if(item.isHasEpg()){
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }


    private List<Date> getDateList() {
        List<Date> list = new ArrayList<>();
        Calendar date = Calendar.getInstance();
        list.add(date.getTime());
        for (int i = 0; i < 7; i++) {
            date.add(Calendar.DATE, 1);
            list.add(date.getTime());

        }
        return list;
    }


    @Override
    public void onChannelClickInteraction(ChannelItem channel, int adapterPosition) {

        if (epgEntityLive != null && epgEntityLive.hasActiveObservers()) {
            epgEntityLive.removeObservers(this);
            epgEntityLive = null;
        }
        cuurentEpgList = null;
        epgListAdapter.clear();
        GlideApp.with(Objects.requireNonNull(getActivity()))
                .asBitmap()
                .load(LinkConfig.BASE_URL+LinkConfig.CHANNEL_LOGO_URL+channel.getChannelLogo())
                .placeholder(R.drawable.placeholder_logo)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(onAirChlogo);
        resetOnAir(channel);
        currentSelectedChannel = channel;
        Login login = GlobalVariables.login;
        TimeStampEntity utc = GetUtc.getInstance().getTimestamp();
        epgListAdapter.clear();
        gvDate.setVisibility(View.GONE);
        showNoEpg("Loading");
        DisposableManager.disposeEpg();
        LiveData<Boolean> epgFetchData = viewModel.getEpgs(channel.getChannelEpgName(),login.getToken(), String.valueOf(channel.getId()));

        epgFetchData.observe(this, aBoolean -> {
            if (aBoolean != null) {
                getEpgFromDB(currentSelectedChannel);
            }
        });

    }

    private void getEpgFromDB(ChannelItem currentSelectedChannel) {
        epgEntityLive = viewModel.getEpgFromDB(currentSelectedChannel.getId());
        epgEntityLive.observe(this, new Observer<List<Epgs>>() {
            @Override
            public void onChanged(@Nullable List<Epgs> epgs) {
                if (epgs != null) {
                    if (epgs.size() > 0) {
                        setUpAdapter(epgs, false);
                        cuurentEpgList = epgs;
                    } else {
                        nOEpg.setVisibility(View.VISIBLE);
                        gvDate.setVisibility(View.GONE);
                        showNoEpg("No epg found");
                    }
                    epgEntityLive.removeObserver(this);
                }
            }
        });
    }

    private void setUpAdapter(List<Epgs> epgs, boolean showDate) {
        if (epgs != null) {
            List<Epgs> newList = getFilteredEpgs(epgs);
            if (newList.size() > 0) {
                epgListAdapter.setmList(newList);
                nOEpg.setVisibility(View.GONE);
                gvDate.setVisibility(View.VISIBLE);
                gvEpgDvr.setVisibility(View.VISIBLE);

            } else {
                if (!showDate) gvDate.setVisibility(View.GONE);

                showNoEpg("No epg found");
            }
        } else {
            if (!showDate) gvDate.setVisibility(View.GONE);
            showNoEpg("No epg found");


        }


    }

    private void showNoEpg(String message) {

        gvEpgDvr.setVisibility(View.GONE);
        nOEpg.setText(message);
        nOEpg.setVisibility(View.VISIBLE);
    }

    private List<Epgs> getFilteredEpgs(List<Epgs> epgs) {
        List<Epgs> newList = new ArrayList<>();
        for (Epgs epg : epgs) {
            if (checkDate(epg.getDate())) {
                Calendar currentTime = Calendar.getInstance();
                Date currentDate = currentTime.getTime();
                Log.d("currentTime:"+currentTime.getTime(),"EndTime:"+epg.getEndTime());
                if (epg.getEndTime().after(currentDate)) {
                    newList.add(epg);
                }
            }

        }
        return newList;
    }

    private boolean checkDate(Date date) {
        String epgDate = DateUtils.dateAndTime.format(date);
        String current = DateUtils.dateAndTime.format(currentEpgDate);
        return epgDate.equals(current);
    }

    @Override
    public void onClick(int postion, Date date) {
        currentEpgDate = date;
        setUpAdapter(cuurentEpgList, true);
        gvDate.smoothScrollToPosition(postion);
    }


    @Override
    public void onEpgClicked(Epgs epg) {
        mListener.playChannelFromOnAir(adapter.getChannel(epg.getChannelID()),allChannelItems.indexOf(currentSelectedChannel), true);
    }

    @Override
    public void onOnAirSetup(Epgs epgs) {
        if (currentSelectedChannel.getId() == epgs.getChannelID()) {
            txtPrgmName.setText(epgs.getProgramTitle());
            txtPrgmTime.setText(DataUtils.getPrgmTime(epgs.getStartTime(), epgs.getEndTime()));
        } else {
            txtPrgmName.setText("");
            txtPrgmTime.setText("");
        }

    }

    private void resetOnAir(ChannelItem channel) {
//        if(getActivity() != null)
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        txtPrgmName.setText("");
        txtPrgmTime.setText("");
//            }
//        });


    }

    private String getChannelName(int channelID) {
        ChannelItem item = adapter.getChannel(channelID);
        return item.getName();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentMenu.FragmentMenuInteraction) {
            mListener = (FragmentEpgInteraction) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void setAllChannelList(List<ChannelItem> allChannelItems) {
        this.allChannelItems = allChannelItems;
    }


    public interface FragmentEpgInteraction {
        void playChannelFromOnAir(ChannelItem channel, int channelPositionById, boolean onAir);
    }


}
