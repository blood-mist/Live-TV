package androidtv.livetv.stb.ui.videoplay.fragments.menu;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import androidtv.livetv.stb.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMenu extends Fragment {


    public FragmentMenu() {
        // Required empty public constructor
    }
    @BindView(R.id.txt_title) TextView channelName;
    @BindView(R.id.ChannelDescription) TextView channelDescription;
    @BindView(R.id.amount) TextView amount;
    @BindView(R.id.channelNo) TextView channelNo;
    @BindView(R.id.currentcategory) TextView currentCategoryTitleView;
    @BindView(R.id.categoryList) RecyclerView categoryList;
    @BindView(R.id.fav) ImageView btnFav;
    @BindView(R.id.txt_fav_unfav) TextView txtFavUnfav;
    @BindView(R.id.txt_dvr) TextView txtDvr;
    @BindView(R.id.dvr) ImageView btnDvr;
    @BindView(R.id.epg) ImageView btnEpg;
    @BindView(R.id.txt_epg) TextView txtEpg;
    @BindView(R.id.container_error) FrameLayout errorFrameLayout;
    @BindView(R.id.gv_channels) RecyclerView gvChannelsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fragment_menu, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
