package androidtv.livetv.stb.ui.videoplay.fragments.menu;


import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.ui.videoplay.adapters.CategoryAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.ChannelListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMenu extends Fragment implements CategoryAdapter.OnListClickListener, ChannelListAdapter.ChannelListClickListener {

    private MenuViewModel menuViewModel;
    private FragmentMenuInteraction mListener;
    private List<CategoryItem> mListCategories;
    private List<ChannelItem> mListChannels;
    private int channelIds;
    private ChannelListAdapter adapter;

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

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    private int catId = -1;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fragment_menu, container, false);
        menuViewModel= ViewModelProviders.of(this).get(MenuViewModel.class);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("frag","view created");

        menuViewModel.getCategoriesWithChannels().observe(this, new Observer<List<CategoriesWithChannels>>() {
            @Override
            public void onChanged(@Nullable List<CategoriesWithChannels> categoriesWithChannels) {
                if(categoriesWithChannels!=null) {
                    CategoriesWithChannels item = categoriesWithChannels.get(1);
                    List<ChannelItem> channelItems=item.channelItemList;
                    Log.d(TAG, channelItems.size()+"");
                }
            }
        });
//        setUpRecylerViewCategory();


//        setUpRecylerViewCategory();
//        setUpChannelsCategory(catId);
//>>>>>>> issue

    }





    private void setUpRecylerViewCategory() {
        Toast.makeText(getActivity(), "Setting recycleing view for category", Toast.LENGTH_SHORT).show();
        Log.d("frag","recycle view created");
        CategoryAdapter adapter = new CategoryAdapter(getActivity(),this);
        categoryList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        categoryList.setAdapter(adapter);
        menuViewModel.getCategoryData().observe(this, categoryItems -> adapter.setCategory(categoryItems));




    }

    private void setUpChannelsCategory(int id){
        adapter = new ChannelListAdapter(getActivity(),this);
        gvChannelsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        gvChannelsList.setAdapter(adapter);
//        menuViewModel.getChannels(id).observe(this, channelItems -> {
//            adapter.setChannelItems(channelItems);
//
//        });
    }



    @Override
    public void onClickCategory(CategoryItem categoryItem) {
      setUpChannelsCategory(categoryItem.getId());
    }

    @Override
    public void onClickChannel(int position) {
       onChannelFocused(position);
       mListener.playChannel(adapter.getmList().get(position));
    }

    @Override
    public void onChannelFocused(int position) {
     setValues(adapter.getmList().get(position));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentMenuInteraction) {
            mListener = (FragmentMenuInteraction) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    public interface FragmentMenuInteraction{
        void playChannel(ChannelItem item);
    }

    private void setValues(ChannelItem item){
        if(item != null){
            channelName.setText(item.getName());
            channelDescription.setText(item.getChannelDesc());

        }
    }




}
