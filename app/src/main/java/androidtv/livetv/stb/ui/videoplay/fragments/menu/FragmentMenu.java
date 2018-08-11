package androidtv.livetv.stb.ui.videoplay.fragments.menu;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Index;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.ChannelChangeObserver;
import androidtv.livetv.stb.ui.videoplay.VideoPlayActivity;
import androidtv.livetv.stb.ui.videoplay.adapters.CategoryAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.ChannelListAdapter;


import androidtv.livetv.stb.ui.videoplay.fragments.error.ErrorFragment;
import androidtv.livetv.stb.utils.LinkConfig;

import androidtv.livetv.stb.ui.videoplay.fragments.dvr.DvrFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static androidtv.livetv.stb.utils.LinkConfig.CATEGORY_FAVORITE;
import static androidtv.livetv.stb.utils.LinkConfig.CATEGORY_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;

/**
 * A simple {@link Fragment} subclass to show menu
 */
public class FragmentMenu extends Fragment implements CategoryAdapter.OnListClickListener, ChannelListAdapter.ChannelListClickListener, Observer {

    private static final int IS_FAV = 1;
    private MenuViewModel menuViewModel;
    private FragmentMenuInteraction mListener;
    private int channelIds;
    private ChannelListAdapter adapter;
    private SharedPreferences lastPlayedPrefs;
    private int selectedCurrentChannelId;

    Gson gson = new Gson();
    private List<ChannelItem> allChannelItems;

    public FragmentMenu() {
        // Required empty public constructor
    }


    @BindView(R.id.btn_focus)
    Button focusHelper;
    @BindView(R.id.layout_epg)
    LinearLayout layoutEpg;
    @BindView(R.id.layout_dvr)
    LinearLayout layoutDvr;
    @BindView(R.id.layout_fav)
    LinearLayout layoutFav;

    @BindView(R.id.txt_title)
    TextView channelName;
    @BindView(R.id.ChannelDescription)
    TextView channelDescription;
    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.channelNo)
    TextView channelNo;
    @BindView(R.id.currentcategory)
    TextView currentCategoryTitleView;
    @BindView(R.id.categoryList)
    RecyclerView categoryList;
    @BindView(R.id.txt_fav_unfav)
    TextView txtFavUnfav;
    @BindView(R.id.txt_dvr)
    TextView txtDvr;
    @BindView(R.id.txt_epg)
    TextView txtEpg;
    @BindView(R.id.gv_channels)
    RecyclerView gvChannelsList;
    @BindView(R.id.preview_view)
    VideoView previewView;
    @BindView(R.id.preview_container)
    FrameLayout previewContainer;
    @BindView(R.id.epg)
    ImageView btnEpg;

    @BindView(R.id.dvr)
    ImageView btnDvr;

    @BindView(R.id.fav)
    ImageView btnFav;


    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    private int catId = -1;


    private int currentChannelPosition = 0;
    private int selectedChannelPosition = 0;
    private int lastPlayedPosition = 0;
    private Login login;
    int lastPlayedId;

    private Handler watchPreviewHandler = new Handler();

    private ChannelItem currentSelected, currentPlayed;
    private ChannelItem current;
    private ErrorFragment errorFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
        playLastPlayedChannel();
        errorFragment = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Timber.d("onCreateView");
        View v = inflater.inflate(R.layout.fragment_fragment_menu, container, false);
        ButterKnife.bind(this, v);
        this.login = GlobalVariables.login;
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("frag", "view created");
        setUpRecylerViewCategory();
        categoryList.setNextFocusDownId(gvChannelsList.getId());
        btnEpg.setNextFocusUpId(categoryList.getId());
        btnDvr.setNextFocusUpId(categoryList.getId());
        btnFav.setNextFocusUpId(categoryList.getId());
        btnFav.setOnFocusChangeListener((view12, hasFocus) -> {
            if (hasFocus) {
                btnFav.setScaleX(1.4f);
                btnFav.setScaleY(1.4f);
                txtFavUnfav.setScaleX(1.3f);
                txtFavUnfav.setScaleY(1.3f);
                txtFavUnfav.setTextColor(getResources().getColor(R.color.red_selector));
                btnFav.setColorFilter(getResources().getColor(R.color.red_selector));

            } else {
                txtFavUnfav.setTextColor(getResources().getColor(R.color.dvr_text_color));
                btnFav.setColorFilter(getResources().getColor(R.color.dvr_text_color));
                btnFav.setScaleX(1.0f);
                btnFav.setScaleY(1.0f);
                txtFavUnfav.setScaleX(1.0f);
                txtFavUnfav.setScaleY(1.0f);
            }
        });

        btnDvr.setOnFocusChangeListener((view1, hasFocus) -> {
            if (hasFocus) {
                btnDvr.setScaleX(1.4f);
                btnDvr.setScaleY(1.4f);
                txtDvr.setScaleX(1.3f);
                txtDvr.setScaleY(1.3f);
                txtDvr.setTextColor(getResources().getColor(R.color.red_selector));
                btnDvr.setColorFilter(getResources().getColor(R.color.red_selector));

            } else {
                txtDvr.setTextColor(getResources().getColor(R.color.dvr_text_color));
                btnDvr.setColorFilter(getResources().getColor(R.color.dvr_text_color));
                btnDvr.setScaleX(1.0f);
                btnDvr.setScaleY(1.0f);
                txtDvr.setScaleX(1.0f);
                txtDvr.setScaleY(1.0f);
            }


        });

        btnEpg.setOnFocusChangeListener((view1, hasFocus) -> {
            if (hasFocus) {
                btnEpg.setScaleX(1.4f);
                btnEpg.setScaleY(1.4f);
                txtEpg.setScaleX(1.3f);
                txtEpg.setScaleY(1.3f);
                txtEpg.setTextColor(getResources().getColor(R.color.red_selector));
                btnEpg.setColorFilter(getResources().getColor(R.color.red_selector));

            } else {
                txtEpg.setTextColor(getResources().getColor(R.color.dvr_text_color));
                btnEpg.setColorFilter(getResources().getColor(R.color.dvr_text_color));
                btnEpg.setScaleX(1.0f);
                btnEpg.setScaleY(1.0f);
                txtEpg.setScaleX(1.0f);
                txtEpg.setScaleY(1.0f);

            }
        });

    }

    private void playLastPlayedChannel() {
        lastPlayedId = lastPlayedPrefs.getInt(CHANNEL_ID, -1);
        selectedCurrentChannelId = lastPlayedId;
        if (lastPlayedId != -1) {
            LiveData<ChannelItem> lastPlayedChData = menuViewModel.getLastPlayedChannel(lastPlayedId);
            lastPlayedChData.observe(this, new android.arch.lifecycle.Observer<ChannelItem>() {
                @Override
                public void onChanged(@Nullable ChannelItem channelItem) {
                    if (channelItem != null) {
                        setValues(channelItem);
                        mListener.playChannel(channelItem);
                        currentPlayed = channelItem;
                        lastPlayedChData.removeObserver(this);
                    }
                }
            });
        } else {
            openErrorFragment();
        }
    }

    private void openErrorFragment() {

        ((VideoPlayActivity) Objects.requireNonNull(getActivity())).setErrorFragment(null,0,0);
    }

    /**
     * get Categories along with channel list from database and populate into their respective adapters
     */
    private void setUpRecylerViewCategory() {
        Log.d("frag", "recycle view created");
        CategoryAdapter adapter = new CategoryAdapter(getActivity(), this);
        categoryList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        categoryList.setAdapter(adapter);

        menuViewModel.getCategoriesWithChannels().observe(this, (List<CategoriesWithChannels> categoriesWithChannels) -> {
            if (categoriesWithChannels != null && categoriesWithChannels.size() != 0) {
                adapter.setCategory(categoriesWithChannels);
                CategoriesWithChannels item = categoriesWithChannels.get(1);
                List<ChannelItem> channelItems = item.channelItemList;
                Log.d("channelSize", channelItems.size() + "");
                io.reactivex.Observable.just(categoriesWithChannels).map(this::addAllChannels).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(channelItemList -> setUpCategoriesToView(channelItemList, adapter));

            }
        });


    }



    private void setUpCategoriesToView(List<ChannelItem> channelItemList, CategoryAdapter adapter) {
        allChannelItems = channelItemList;
        adapter.setAllChannelList(channelItemList);
        io.reactivex.Observable.just(channelItemList).map(this::checkForFavorites).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(favChannelList -> setUpFavToView(favChannelList, adapter));
        updateCategoryUI(channelItemList);
    }

    private void updateCategoryUI(List<ChannelItem> channelItemList) {
        String lastPlayedCategory = lastPlayedPrefs.getString(CATEGORY_NAME, "All Channels");
        if (lastPlayedCategory.equalsIgnoreCase("All Channels"))
            onClickCategory("All Channels", channelItemList);
        else {
            String channelJson = lastPlayedPrefs.getString(lastPlayedCategory, "");
            if (channelJson.isEmpty()) {
                if (lastPlayedCategory.equalsIgnoreCase(CATEGORY_FAVORITE))
                    onClickCategory("All Channels", channelItemList);
                else
                    channelItemList = new ArrayList<>();
            } else {
                Type type = new TypeToken<List<ChannelItem>>() {
                }.getType();
                channelItemList = gson.fromJson(channelJson, type);
            }
            onClickCategory(lastPlayedCategory, channelItemList);
        }
    }


    private void setUpFavToView(List<ChannelItem> favChannelList, CategoryAdapter adapter) {
        if (favChannelList.size() != 0) {
            CategoriesWithChannels favoriteCatCh = new CategoriesWithChannels();
            favChannelList.sort(Comparator.comparing(ChannelItem::getChannelPriority));
            CategoryItem catItem = new CategoryItem();
            catItem.setTitle(CATEGORY_FAVORITE);
            favoriteCatCh.categoryItem = catItem;
            favoriteCatCh.channelItemList = favChannelList;
            adapter.addFavoriteItem(favoriteCatCh);
        } else {
            adapter.removeFavoriteItem();
        }
    }

    private List<ChannelItem> checkForFavorites(List<ChannelItem> channelItemList) {
        List<ChannelItem> favChannelList = new ArrayList<>();
        for (ChannelItem toCheckFavitem : channelItemList) {
            if (toCheckFavitem.getIs_fav() == IS_FAV) {
                favChannelList.add(toCheckFavitem);
            }
        }
        return favChannelList;

    }

    private List<ChannelItem> addAllChannels(List<CategoriesWithChannels> categoriesWithChannels) {
        List<ChannelItem> channelItemList = new ArrayList<>();
        for (CategoriesWithChannels withChannels : categoriesWithChannels) {
            channelItemList.addAll(withChannels.channelItemList);
        }
        return channelItemList;
    }

    /**
     * fetch channelList from category list with embedded channels and populate into the channel Recycler View
     *
     * @param items
     */
    private void setUpChannelsCategory(List<ChannelItem> items) {
        if (items != null) {
            if (items.size() > 0) {
                adapter = new ChannelListAdapter(getActivity(), this);
                gvChannelsList.setLayoutManager(new LinearLayoutManager(getActivity()));
                gvChannelsList.setAdapter(adapter);
                gvChannelsList.requestFocus();
                adapter.setChannelItems(items);
                if (lastPlayedId != -1) {
                    selectedChannelPosition = adapter.getSelectedChannelPositionViaId(lastPlayedId);
                    lastPlayedId = -1;
                }
                adapter.setPositionSelected(selectedChannelPosition);
                try {
                    currentSelected = adapter.getmList().get(selectedChannelPosition);
                }catch (Exception e){
                    currentSelected=adapter.getmList().get(0);

                }
                gvChannelsList.getLayoutManager().scrollToPosition(selectedChannelPosition);
                adapter.notifyDataSetChanged();
            }
        }

    }


    /**
     * populate channels of clicked category into channel Recycler View
     *
     * @param categoryName
     * @param mListChannels
     */
    @Override
    public void onClickCategory(String categoryName, List<ChannelItem> mListChannels) {
        currentCategoryTitleView.setText(categoryName);
        if (mListChannels.contains(currentPlayed)) {
            selectedChannelPosition = mListChannels.indexOf(currentPlayed);
            String channelJson = gson.toJson(mListChannels);
            SharedPreferences.Editor categoryEditor = lastPlayedPrefs.edit();
            categoryEditor.putString(CATEGORY_NAME, categoryName);
            categoryEditor.putString(categoryName, channelJson);
            categoryEditor.commit();
        }
        else
            selectedChannelPosition = 0;
        setUpChannelsCategory(mListChannels);
    }

    /**
     * prompt the listener to initialise channel Play Event on channel clicked
     *
     * @param position
     */
    @Override
    public void onClickChannel(int position) {
        currentChannelPosition = position;
        selectedChannelPosition = position;
        Timber.d("position:" + currentChannelPosition);
        selectedCurrentChannelId = adapter.getmList().get(position).getId();
        mListener.playChannel(adapter.getmList().get(currentChannelPosition));
        currentSelected = adapter.getmList().get(position);
        currentPlayed = adapter.getmList().get(position);
        mListener.playChannel(adapter.getmList().get(position));

    }


    /**
     * update UI events and preview status on channel list navigation
     *
     * @param position
     */
    @Override
    public void onChannelFocused(int position) {
        View currentFocusedView = gvChannelsList.findViewHolderForLayoutPosition(position).itemView;
        if (currentFocusedView != null)
            currentFocusedView.setNextFocusRightId(btnEpg.getId());
        selectedChannelPosition = position;
        setValues(adapter.getmList().get(position));
        stopPreview();
        currentSelected = adapter.getmList().get(position);
        fetchPreview(adapter.getmList().get(position));

    }


    /**
     * stop handler and video playback
     */
    private void stopPreview() {
        //TODO stop preview here
        previewView.stopPlayback();
        previewContainer.setVisibility(View.INVISIBLE);
        watchPreviewHandler.removeCallbacksAndMessages(null);
    }

    /**
     * fetch preview of the selected channel item from server
     *
     * @param channelItem
     */
    private void fetchPreview(ChannelItem channelItem) {
        long utc = GetUtc.getInstance().getTimestamp().getUtc();
        menuViewModel.getPreviewLink(login.getToken(), utc, String.valueOf(login.getId()),
                LinkConfig.getHashCode(String.valueOf(login.getId()), String.valueOf(utc), login.getSession()), channelItem.getId()).observe(this, channelLinkResponse -> {
            if (channelLinkResponse != null) {
                startPreview(channelLinkResponse.getChannel().getLink());
            }
        });

    }

    /**
     * set Handler to stream the fecthed preview link to video view
     *
     * @param link
     */
    private void startPreview(String link) {
        //TODO enable preview here
        watchPreviewHandler.postDelayed(() -> initVideoView(link), TimeUnit.SECONDS.toMillis(5));
    }

   /**
     * initialise video view here
     *
     * @param link
     **/
    private void initVideoView(String link) {
        try {
            previewView.setVideoPath(link);
            previewView.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setVolume(0f, 0f);
                mediaPlayer.start();
                previewContainer.setVisibility(View.VISIBLE);
            });
            previewView.setOnErrorListener((mp, what, extra) -> {
                previewContainer.setVisibility(View.INVISIBLE);
//                Toast.makeText(getActivity(), "LoginError Code: \t W"+what+"E"+extra, Toast.LENGTH_SHORT).show();
                Timber.e("Media LoginError: ", "what = " + what + " extra = " + extra);
                return true;
            });
        } catch (Exception e) {
            Timber.wtf(e);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden)
            stopPreview();
        else {
            if(errorFragment != null)
                showErrorFrag(errorFragment);
            updateCategoryUI(allChannelItems);

        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
        stopPreview();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.fav)
    public void onFavClick() {
        ChannelItem selectedChannel = adapter.getmList().get(selectedChannelPosition);
        int toFavUnfavId = selectedChannel.getId();
        int favStatus = selectedChannel.getIs_fav() == 0 ? 1 : 0;
        menuViewModel.addChannelToFavorite(favStatus, toFavUnfavId);
        Toast.makeText(getActivity(), selectedChannel.getName() + " " + (favStatus == 0 ? getString(R.string.channel_rm_fav) : getString(R.string.channel_set_fav)), Toast.LENGTH_LONG).show();
        gvChannelsList.getAdapter().notifyDataSetChanged();
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
        lastPlayedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
<<<<<<< HEAD
     * Observe Up & down key press event from activity  and change the currentSelected playing channel accordingly
     *
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof ChannelChangeObserver) {
            if (((ChannelChangeObserver) observable).getChannelNext()) {
                //put condition here so no IoB Exception occurs
                if (currentChannelPosition < adapter.getItemCount() - 1)
                    currentChannelPosition++;
                else
                    currentChannelPosition = 0;
                mListener.playChannel(adapter.getmList().get(currentChannelPosition));
            } else if (!((ChannelChangeObserver) observable).getChannelNext()) {
                if (currentChannelPosition > 0)
                    currentChannelPosition--;
                else
                    currentChannelPosition = adapter.getItemCount() - 1;
                   mListener.playChannel(adapter.getmList().get(currentChannelPosition));

            }
        }
    }


    private void setValues(ChannelItem item) {
        if (item != null) {
            channelName.setText(item.getName());
            channelDescription.setText(item.getChannelDesc());
            amount.setText(item.getPrice());
            channelNo.setText(String.valueOf(item.getChannelPriority()));
            txtFavUnfav.setText(item.getIs_fav() == 0 ? "SET FAV" : "UNSET FAV");

        }
    }


    @OnClick(R.id.layout_epg)
    public void OnEpgClick() {
        EpgFragment fragment = new EpgFragment();
        fragment.setCurrentSelectedChannel(currentSelected);
        mListener.load(fragment, "epg");
    }

    @OnClick(R.id.layout_dvr)
    public void OnDvrClick() {
        DvrFragment fragment = new DvrFragment();
        fragment.setCurrentChannel(currentSelected);

        mListener.load(fragment, "Dvr");
    }

    public void showErrorFrag(ErrorFragment errorFragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.error_layout, errorFragment).commit();
    }

    public void hideErrorFrag() {
        ErrorFragment menuFrag = (ErrorFragment) getChildFragmentManager().findFragmentById(R.id.error_layout);
        if (menuFrag != null) {
            getChildFragmentManager().beginTransaction().hide(menuFrag).commit();
            errorFragment = null;
        }
    }

    public void setErrorFragMent(ErrorFragment errorFragment) {
        this.errorFragment = errorFragment;
    }


    public interface FragmentMenuInteraction {
        void playChannel(ChannelItem item);

        void load(Fragment epgFragment, String tag);


    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

    }
}
