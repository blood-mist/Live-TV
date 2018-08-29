package androidtv.livetv.stb.ui.videoplay.fragments.menu;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.AllChannelsEvent;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.FavEvent;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.ChannelChangeObserver;
import androidtv.livetv.stb.ui.videoplay.VideoPlayActivity;
import androidtv.livetv.stb.ui.videoplay.adapters.CategoryAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.ChannelListAdapter;


import androidtv.livetv.stb.ui.videoplay.fragments.error.ErrorFragment;
import androidtv.livetv.stb.utils.DisposableManager;
import androidtv.livetv.stb.utils.LinkConfig;

import androidtv.livetv.stb.ui.videoplay.fragments.dvr.DvrFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.GONE;
import static androidtv.livetv.stb.utils.LinkConfig.CATEGORY_FAVORITE;
import static androidtv.livetv.stb.utils.LinkConfig.PLAYED_CATEGORY_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;
import static androidtv.livetv.stb.utils.LinkConfig.SELECTED_CATEGORY_NAME;

/**
 * A simple {@link Fragment} subclass to show menu
 */
public class FragmentMenu extends Fragment implements CategoryAdapter.OnListClickListener, ChannelListAdapter.ChannelListClickListener, Observer {

    private static final int IS_FAV = 1;
    private MenuViewModel menuViewModel;
    private FragmentMenuInteraction mListener;
    private ChannelListAdapter adapter;
    CategoryAdapter categoryAdapter;
    private SharedPreferences lastPlayedPrefs;
    private List<ChannelItem> currentPlayedCategoryItems;
    private View selectedCategoryView;

    private Handler watchPreviewHandler = new Handler();
    View playedCategoryView = null;

    private List<ChannelItem> allChannelItems, allFavItems;
    private boolean isFirstRun = true;

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

    @BindView(R.id.no_channels)
    TextView noChannels;

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

    private int catId = -1;


    private int currentChannelPosition = 0;
    private int selectedChannelPosition = 0;
    private int currentPlayingCategoryPosition = 0;
    private int topView;
    private Login login;
    int lastPlayedId;

    private List<CategoriesWithChannels> allCategoryChannels;
    private ChannelItem currentSelected, currentPlayed;
    private ChannelItem current;
    private ErrorFragment errorFragment;
    private LinearLayoutManager channellayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
        playLastPlayedChannel();
        errorFragment = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Timber.d("onCreateView");
        View v = inflater.inflate(R.layout.fragment_fragment_menu, container, false);
        ButterKnife.bind(this, v);
        this.login = GlobalVariables.login;
        categoryAdapter = new CategoryAdapter(getActivity(), FragmentMenu.this);
        categoryList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        categoryList.setAdapter(categoryAdapter);

        adapter = new ChannelListAdapter(getActivity(), this);
        channellayoutManager = new LinearLayoutManager(getActivity());
        gvChannelsList.setLayoutManager(channellayoutManager);
        gvChannelsList.setAdapter(adapter);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("frag", "view created");
        setUpRecylerViewCategory();
        layoutDvr.setNextFocusUpId(categoryList.getId());
        layoutFav.setNextFocusUpId(categoryList.getId());
        layoutFav.setNextFocusRightId(layoutFav.getId());
        gvChannelsList.setNextFocusRightId(layoutEpg.getId());
        layoutFav.setOnFocusChangeListener((view12, hasFocus) -> {
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

        layoutDvr.setOnFocusChangeListener((view1, hasFocus) -> {
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

        layoutEpg.setOnFocusChangeListener((view1, hasFocus) -> {
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

    public void playLastPlayedChannel() {
        lastPlayedId = lastPlayedPrefs.getInt(CHANNEL_ID, -1);
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
            LiveData<ChannelItem> firstPlayedData = menuViewModel.getFirstChannel();
            firstPlayedData.observe(this, new android.arch.lifecycle.Observer<ChannelItem>() {
                @Override
                public void onChanged(@Nullable ChannelItem channelItem) {
                    if (channelItem != null) {
                        setValues(channelItem);
                        mListener.playChannel(channelItem);
                        currentPlayed = channelItem;
                        firstPlayedData.removeObserver(this);
                    }
                }
            });
//            openErrorFragment();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FavEvent event) {
        if (event.getSuccessStatus() != -1 || event.getSuccessStatus() != 0) {
            Toast.makeText(getActivity(), event.getChannelItem().getName() + " " + (event.getFavStatus() == 0 ? getString(R.string.channel_rm_fav) : getString(R.string.channel_set_fav)), Toast.LENGTH_LONG).show();
            adapter.getmList().get(selectedChannelPosition).setIs_fav(event.getFavStatus());
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), getString(R.string.err_unexpected), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void openErrorFragment() {

        ((VideoPlayActivity) Objects.requireNonNull(getActivity())).setErrorFragment(null, 0, 0);
    }

    /**
     * get Categories along with channel list from database and populate into their respective adapters
     */
    private void setUpRecylerViewCategory() {
        Log.d("frag", "recycle view created");
        LiveData<List<CategoriesWithChannels>> liveData = menuViewModel.getCategoriesWithChannels();
        liveData.observe(this, categoriesWithChannels -> {
            if (categoriesWithChannels != null && categoriesWithChannels.size() >0) {
                categoryAdapter.setCategory(categoriesWithChannels);
                addAllChannels(categoriesWithChannels);
            }
        });

    }


    private void setUpCategoriesToView(List<ChannelItem> channelItemList, CategoryAdapter adapter, List<CategoriesWithChannels> categoriesWithChannels) {
        adapter.setAllChannelList(channelItemList);

        categoryList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    playedCategoryView = categoryList.findViewHolderForAdapterPosition(currentPlayingCategoryPosition).itemView.findViewById(R.id.channelCategory_layout);
                    categoryList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Exception ignored) {
                }
            }
        });
        if (playedCategoryView != null) {
            layoutEpg.setNextFocusUpId(playedCategoryView.getId());
        } else {
            try {
                layoutEpg.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.channelCategory_layout).getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        checkForFavorites(channelItemList, categoriesWithChannels);

    }


    private void updateCategoryUI(List<ChannelItem> allChannelItems, List<ChannelItem> favlItemList, List<CategoriesWithChannels> allCategoryChannels) {
        String lastPlayedCategory = "";
        if (isFirstRun) {
            lastPlayedCategory = lastPlayedPrefs.getString(PLAYED_CATEGORY_NAME, "All Channels");
            isFirstRun = false;
        } else
            lastPlayedCategory = lastPlayedPrefs.getString(SELECTED_CATEGORY_NAME, "All Channels");

        if (lastPlayedCategory.equalsIgnoreCase("All Channels")) {
            onClickCategory("All Channels", 0, allChannelItems);
        } else if (lastPlayedCategory.equalsIgnoreCase(CATEGORY_FAVORITE))
            checkListAndClickFav(allChannelItems, favlItemList);
        else {
            String finalLastPlayedCategory = lastPlayedCategory;
            io.reactivex.Observable.just(allCategoryChannels).map((List<CategoriesWithChannels> allCategoryChannels1) -> selectLastPlayedCatAndChannel(allCategoryChannels1, finalLastPlayedCategory)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(channelItemList -> {
                onClickCategory(finalLastPlayedCategory, currentPlayingCategoryPosition, channelItemList);
            });
        }
    }

    private List<ChannelItem> selectLastPlayedCatAndChannel(List<CategoriesWithChannels> allCategoryChannels, String lastPlayedCategory) {
        List<ChannelItem> selectedChannels = new ArrayList<>();
        for (CategoriesWithChannels catChannelInfo : allCategoryChannels) {
            if (catChannelInfo.categoryItem.getTitle().equalsIgnoreCase(lastPlayedCategory)) {
                selectedChannels = catChannelInfo.channelItemList;
                currentPlayingCategoryPosition = allCategoryChannels.indexOf(catChannelInfo);
                break;
            }
        }
        return selectedChannels;
    }

    private void checkListAndClickFav(List<ChannelItem> allChannelItems, List<ChannelItem> favlItemList) {
        if (favlItemList.isEmpty()) {
            onClickCategory("All Channels", 0, allChannelItems);
        } else {
            onClickCategory(CATEGORY_FAVORITE, 1, favlItemList);
        }
    }


    private void setUpFavToView(List<ChannelItem> allChannelItems, List<ChannelItem> favChannelList, List<CategoriesWithChannels> categoriesWithChannels) {
        this.allChannelItems = allChannelItems;
        this.allCategoryChannels = categoriesWithChannels;
        this.allFavItems = favChannelList;
        categoryAdapter.addFavoriteItem(allFavItems);
        categoryAdapter.notifyDataSetChanged();
        updateCategoryUI(this.allChannelItems, allFavItems, allCategoryChannels);
    }

    private void checkForFavorites(List<ChannelItem> allChannelItems, List<CategoriesWithChannels> categoriesWithChannels) {
        LiveData<List<ChannelItem>> favListData = menuViewModel.getFavChannels();
        favListData.observe(this, new android.arch.lifecycle.Observer<List<ChannelItem>>() {
            @Override
            public void onChanged(List<ChannelItem> channelItemList) {
                if (channelItemList != null) {
                    Timber.d("FavListSize" + channelItemList.size());
                    if (lastPlayedPrefs.getString(PLAYED_CATEGORY_NAME, getString(R.string.all_category)).equalsIgnoreCase(CATEGORY_FAVORITE) && channelItemList.size() > 0) {
                        currentPlayedCategoryItems = channelItemList;
                    } else {
                        currentPlayedCategoryItems = allChannelItems;
                        currentChannelPosition = currentPlayedCategoryItems.indexOf(currentPlayed);
                    }
                    setUpFavToView(allChannelItems, channelItemList, categoriesWithChannels);
                    favListData.removeObserver(this);
                }
            }
        });

//        for (ChannelItem toCheckFavitem : channelItemList) {
//            if (toCheckFavitem.getIs_fav() == IS_FAV) {
//                favChannelList.add(toCheckFavitem);
//            }
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AllChannelsEvent event) {
        setUpCategoriesToView(event.getChannelItemList(), categoryAdapter, event.getCategoriesWithChannels());
    }


    private void addAllChannels(List<CategoriesWithChannels> categoriesWithChannels) {
        Thread thread = new Thread(() -> {
            List<ChannelItem> channelItemList = new ArrayList<>();
            for (CategoriesWithChannels withChannels : categoriesWithChannels) {
                channelItemList.addAll(withChannels.channelItemList);
            }
            EventBus.getDefault().post(new AllChannelsEvent(channelItemList, categoriesWithChannels));
            Thread.currentThread().interrupt();
        });
        thread.start();

    }

    /**
     * fetch channelList from category list with embedded channels and populate into the channel Recycler View
     *
     * @param items
     */
    private void setUpChannelsCategory(String categoryName, int pos, int channelPosition, List<ChannelItem> items) {
        if (items != null) {
            if (items.size() > 0) {
                noChannels.setVisibility(GONE);
                gvChannelsList.setVisibility(View.VISIBLE);
//                gvChannelsList.requestFocus();
                adapter.setChannelItems(categoryName, pos, channelPosition, items);
                gvChannelsList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        gvChannelsList.postDelayed(() -> {
                            if (lastPlayedId != -1) {
                                selectedChannelPosition = adapter.getSelectedChannelPositionViaId(lastPlayedId);
                                currentPlayedCategoryItems = items;
                                lastPlayedId = -1;
                            } else {
                                try {
                                    selectedChannelPosition = items.indexOf(currentPlayed);
                                } catch (Exception ignored) {
                                    selectedChannelPosition = 0;
                                }
                            }
                            try {
                                currentSelected = adapter.getmList().get(selectedChannelPosition);
                            } catch (Exception e) {
                                currentSelected = adapter.getmList().get(0);

                            }
                            try {
                                selectedCategoryView = categoryList.findViewHolderForAdapterPosition(pos).itemView.findViewById(R.id.channelCategory_layout);
                                selectedCategoryView.setNextFocusDownId(gvChannelsList.getChildAt(0).getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                layoutEpg.setNextFocusLeftId(gvChannelsList.findViewHolderForAdapterPosition(selectedChannelPosition).itemView.getId());
                                layoutEpg.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(currentPlayingCategoryPosition).itemView.findViewById(R.id.channelCategory_layout).getId());
                                layoutDvr.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(currentPlayingCategoryPosition).itemView.findViewById(R.id.channelCategory_layout).getId());
                                layoutFav.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(currentPlayingCategoryPosition).itemView.findViewById(R.id.channelCategory_layout).getId());
                            } catch (Exception ignored) {
                                layoutEpg.setNextFocusLeftId(gvChannelsList.getChildAt(0).getId());
                                layoutEpg.setNextFocusUpId(categoryList.getChildAt(0).getId());
                                layoutDvr.setNextFocusUpId(categoryList.getChildAt(0).getId());
                                layoutFav.setNextFocusUpId(categoryList.getChildAt(0).getId());
                            }

                            try {
                                gvChannelsList.findViewHolderForAdapterPosition(selectedChannelPosition).itemView.requestFocus();
                                gvChannelsList.getLayoutManager().scrollToPosition(selectedChannelPosition);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            gvChannelsList.getViewTreeObserver().removeOnGlobalLayoutListener(this);


                        }, 100);
                    }
                });

            } else {
                gvChannelsList.setVisibility(GONE);
                noChannels.setVisibility(View.VISIBLE);
                layoutEpg.setNextFocusLeftId(playedCategoryView.getId());
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
    public void onClickCategory(String categoryName, int pos, List<ChannelItem> mListChannels) {
        stopPreview();
        currentCategoryTitleView.setText(categoryName);
        SharedPreferences.Editor categoryEditor = lastPlayedPrefs.edit();
        categoryEditor.putString(SELECTED_CATEGORY_NAME, categoryName);
        categoryEditor.apply();
        if (mListChannels.contains(currentPlayed)) {
            selectedChannelPosition = mListChannels.indexOf(currentPlayed);
        } else
            selectedChannelPosition = 0;

        setUpChannelsCategory(categoryName, pos, selectedChannelPosition, mListChannels);
    }

    @Override
    public void onSelectCategory(int position, View focusedCatView) {
        stopPreview();
        this.selectedCategoryView = focusedCatView;
//        selectedCategoryView.setNextFocusDownId(gvChannelsList.getChildAt(0).getId());

    }

    /**
     * prompt the listener to initialise channel Play Event on channel clicked
     *
     * @param position
     */
    @Override
    public void onClickChannel(String currentPlayingCategory, int categoryPosition, int position, List<ChannelItem> currentPlayedChannel) {
        if (position != -1) {
            stopPreview();
            currentChannelPosition = position;
            selectedChannelPosition = position;
            currentPlayingCategoryPosition = categoryPosition;
            SharedPreferences.Editor categoryEditor = lastPlayedPrefs.edit();
            categoryEditor.putString(PLAYED_CATEGORY_NAME, currentPlayingCategory);
            categoryEditor.apply();
            Timber.d("position:" + currentChannelPosition);
            currentSelected = currentPlayedChannel.get(position);
            currentPlayed = currentPlayedChannel.get(position);
            currentPlayedCategoryItems = currentPlayedChannel;
            mListener.playChannel(currentPlayed);
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * update UI events and preview status on channel list navigation
     *
     * @param position
     */
    @Override
    public void onChannelFocused(int position) {
        stopPreview();
        try {
            View currentFocusedView = gvChannelsList.findViewHolderForLayoutPosition(position).itemView;
            if (currentFocusedView != null)
                currentFocusedView.setNextFocusRightId(layoutEpg.getId());
            selectedChannelPosition = position;
            setValues(adapter.getmList().get(position));
            currentSelected = adapter.getmList().get(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (currentPlayed != null && currentSelected != null && currentPlayed.getId() != currentSelected.getId())
            startPreview(currentSelected);


    }


    /**
     * stop handler and video playback
     */
    private void stopPreview() {
        //TODO stop preview here
        DisposableManager.dispose();
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
                try {
                    initVideoView(channelLinkResponse.getChannel().getLink());
                } catch (Exception ignored) {
                }
            }
        });

    }

    /**
     * set Handler to stream the fecthed preview link to video view
     *
     * @param channelItem
     */

    private void startPreview(ChannelItem channelItem) {
        //TODO enable preview here
        watchPreviewHandler.postDelayed(() -> fetchPreview(channelItem), TimeUnit.SECONDS.toMillis(5));
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /**
     * initialise video view here
     *
     * @param link
     **/


    private void initVideoView(String link) {
        try {
            previewView.setVideoPath(link);
        } catch (Exception e) {
            e.printStackTrace();
        }
        previewView.setVisibility(View.VISIBLE);
        previewContainer.setVisibility(View.VISIBLE);
        previewView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.start();
        });
        previewView.setOnErrorListener((mp, what, extra) -> {
            previewView.setVisibility(GONE);
            previewContainer.setVisibility(View.INVISIBLE);
//                Toast.makeText(getActivity(), "LoginError Code: \t W"+what+"E"+extra, Toast.LENGTH_SHORT).show();
            Timber.e("Media", "what = " + what + " extra = " + extra);
            return true;
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            stopPreview();
            isFirstRun = true;
        } else {
            if (errorFragment != null) {
                showErrorFrag(errorFragment);
            }
            updateCategoryUI(this.allChannelItems, allFavItems, allCategoryChannels);

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

    @OnClick(R.id.layout_fav)
    public void onFavClick(View view) {
        ChannelItem selectedChannel = adapter.getmList().get(selectedChannelPosition);
        int favStatus = selectedChannel.getIs_fav() == 0 ? 1 : 0;
        selectedChannel.setIs_fav(favStatus);
        menuViewModel.addChannelToFavorite(favStatus, selectedChannel);
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
     * Observe Up & down key press event from activity  and change the currentSelected playing channel accordingly
     *
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof ChannelChangeObserver) {
            DisposableManager.disposeLive();
            if (((ChannelChangeObserver) observable).getChannelNext()) {
                //put condition here so no IoB Exception occurs
                if (currentChannelPosition < currentPlayedCategoryItems.size() - 1)
                    currentChannelPosition++;
                else
                    currentChannelPosition = 0;

            } else if (!((ChannelChangeObserver) observable).getChannelNext()) {
                if (currentChannelPosition > 0)
                    currentChannelPosition--;
                else
                    currentChannelPosition = currentPlayedCategoryItems.size() - 1;

            }
            selectedChannelPosition = currentChannelPosition;
            Timber.d("position:" + currentChannelPosition);
            currentSelected = currentPlayedCategoryItems.get(currentChannelPosition);
            currentPlayed = currentPlayedCategoryItems.get(currentChannelPosition);
            try {
                Objects.requireNonNull(FragmentMenu.this.getActivity()).runOnUiThread(() -> onClickChannel(lastPlayedPrefs.getString(PLAYED_CATEGORY_NAME, SELECTED_CATEGORY_NAME), currentPlayingCategoryPosition, selectedChannelPosition, currentPlayedCategoryItems));
            } catch (Exception ignored) {
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
        fragment.setAllChannelList(allChannelItems);
        mListener.load(fragment, "epg");
    }

    @OnClick(R.id.layout_dvr)
    public void OnDvrClick() {
        DvrFragment fragment = new DvrFragment();
        fragment.setCurrentChannel(currentSelected);
        fragment.setAllChannelList(allChannelItems);
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

    @Override
    public void onResume() {
        super.onResume();

    }
}
