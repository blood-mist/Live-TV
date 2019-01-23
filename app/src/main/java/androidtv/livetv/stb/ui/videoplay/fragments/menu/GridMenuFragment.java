package androidtv.livetv.stb.ui.videoplay.fragments.menu;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.ui.videoplay.VideoPlayActivity;
import androidtv.livetv.stb.ui.videoplay.adapters.CategoryAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.GridCategoryAdapter;
import androidtv.livetv.stb.ui.videoplay.fragments.error.ErrorFragment;
import androidtv.livetv.stb.utils.ItemOffsetDecoration;
import androidtv.livetv.stb.utils.SpacesItemDecoration;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;
import static androidtv.livetv.stb.utils.LinkConfig.SELECTED_CATEGORY_NAME;


public class GridMenuFragment extends Fragment implements CategoryAdapter.OnListClickListener, GridCategoryAdapter.OnListClickListener {


    private OnFragmentInteractionListener mListener;
    @BindView(R.id.menu_grid)
    RecyclerView genreMenuList;

    private MenuViewModel menuViewModel;
    GridCategoryAdapter categoryAdapter;
    private ErrorFragment errorFragment;
    private SharedPreferences lastPlayedPrefs;
    private GridLayoutManager categoryLayoutManager;
    private List<ChannelItem> allChannelItems;
    SharedPreferences.Editor categoryEditor;


    public GridMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);

        errorFragment = null;
    }
    public void setErrorFragMent(ErrorFragment errorFragment) {
        this.errorFragment = errorFragment;
    }

    public void hideErrorFrag() {
        if(!GridMenuFragment.this.isDetached()) {
            ErrorFragment menuFrag = (ErrorFragment) getChildFragmentManager().findFragmentById(R.id.error_layout);
            if (menuFrag != null && !menuFrag.isDetached()) {
                getChildFragmentManager().beginTransaction().hide(menuFrag).commit();
                errorFragment = null;
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
        } else {
            if (errorFragment != null)
                showErrorFrag(errorFragment);
        }
        super.onHiddenChanged(hidden);
    }

    public void showErrorFrag(ErrorFragment errorFragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.error_layout, errorFragment).commit();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View gridMenuView= inflater.inflate(R.layout.fragment_grid_menu, container, false);
        ButterKnife.bind(this,gridMenuView);
        return gridMenuView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        categoryLayoutManager = new GridLayoutManager(getActivity(),5,LinearLayoutManager.VERTICAL,false);
        categoryAdapter = new GridCategoryAdapter(getActivity(), this);
        genreMenuList.setLayoutManager(categoryLayoutManager);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        genreMenuList.addItemDecoration(new ItemOffsetDecoration(getActivity(),R.dimen.spacing));
        genreMenuList.setAdapter(categoryAdapter);
        getAllChannels();
        checkForFavorites();
        LiveData<List<CategoriesWithChannels>> liveData = menuViewModel.getCategoriesWithChannels();
        setUpGridCategories(liveData);
    }

    private void checkForFavorites() {
        LiveData<List<ChannelItem>> favListData = menuViewModel.getFavChannels();
        favListData.observe(this, new android.arch.lifecycle.Observer<List<ChannelItem>>() {
            @Override
            public void onChanged(List<ChannelItem> channelItemList) {
                if (channelItemList != null) {
                    categoryAdapter.addFavoriteItem(channelItemList);
                }
            }
        });
    }

    private void getAllChannels() {
        LiveData<List<ChannelItem>> allchannelData = menuViewModel.getAllChannel();
        allchannelData.observe(this, channelItemList -> {
            if (channelItemList != null && channelItemList.size() > 0) {
                allChannelItems = channelItemList;
               categoryAdapter.setAllChannelList(allChannelItems);
            }

        });
    }
    private void setUpGridCategories(LiveData<List<CategoriesWithChannels>> liveData) {
        liveData.observe(this, new Observer<List<CategoriesWithChannels>>() {
            @Override
            public void onChanged(@Nullable List<CategoriesWithChannels> categoriesWithChannels) {
                if (categoriesWithChannels != null && categoriesWithChannels.size() != 0) {
                    categoryAdapter.setCategory(categoriesWithChannels);
                    liveData.removeObserver(this);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        lastPlayedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClickCategory(String categoryName, int categoryPosition, List<ChannelItem> channels) {
        Toast.makeText(getActivity(),categoryName,Toast.LENGTH_SHORT).show();
        categoryEditor = lastPlayedPrefs.edit();
        categoryEditor.putString(SELECTED_CATEGORY_NAME, categoryName);
        categoryEditor.apply();
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction().hide(GridMenuFragment.this).commit();
        }
        ((VideoPlayActivity) Objects.requireNonNull(getActivity())).openListMenu();



    }

    @Override
    public void onSelectCategory(int position, View focusedCatView) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void playChannel(ChannelItem item);
    }
}
