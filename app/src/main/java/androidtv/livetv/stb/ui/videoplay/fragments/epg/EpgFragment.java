package androidtv.livetv.stb.ui.videoplay.fragments.epg;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.adapters.viewholder.ChannelRecyclerAdapter;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class EpgFragment extends Fragment implements ChannelRecyclerAdapter.OnChannelListInteractionListener {


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
    GridView gvEpgDvr;
    @BindView(R.id.txt_on_air_prgm_time)
    TextView txtPrgmTime;
    @BindView(R.id.txt_on_air_prgm_name)
    TextView txtPrgmName;
    @BindView(R.id.gv_date)
    GridView gvDate;
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

    private EpgViewModel viewModel;
    private ChannelRecyclerAdapter adapter;

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
        adapter = new ChannelRecyclerAdapter(getContext(),this);
        gvChannelList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        gvChannelList.setAdapter(adapter);
        viewModel.getChannels().observe(this, new Observer<List<ChannelItem>>() {
            @Override
            public void onChanged(@Nullable List<ChannelItem> channelItems) {
                adapter.setChannelList(channelItems);
            }
        });
    }

    @Override
    public void onChannelClickInteraction(ChannelItem channel, int adapterPosition) {
        //TODO Call api
        Login login = GlobalVariables.login;
        long utc = GetUtc.getInstance().getTimestamp().getUtc();
     viewModel.getEpgs(login.getToken(),utc, String.valueOf(login.getId()), LinkConfig.getHashCode(String.valueOf(login.getId())
             ,String.valueOf(utc),login.getSession()),String.valueOf(channel.getId())).observe(this, new Observer<List<Epgs>>() {
         @Override
         public void onChanged(@Nullable List<Epgs> epgs) {
             setUpAdapter(epgs);
         }
     });

    }

    private void setUpAdapter(List<Epgs> epgs) {
        if(epgs != null)
        Log.d("epgs","epg not null +"+ String.valueOf(epgs.size()));
    }
}
