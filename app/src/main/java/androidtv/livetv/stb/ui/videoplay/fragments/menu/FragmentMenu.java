package androidtv.livetv.stb.ui.videoplay.fragments.menu;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.UdpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import androidtv.livetv.stb.ApplicationMain;
import androidtv.livetv.stb.R;
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
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.error.ErrorFragment;
import androidtv.livetv.stb.utils.DisposableManager;
import androidtv.livetv.stb.utils.LinkConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.GONE;
import static androidtv.livetv.stb.utils.LinkConfig.CATEGORY_FAVORITE;
import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;
import static androidtv.livetv.stb.utils.LinkConfig.PLAYED_CATEGORY_NAME;
import static androidtv.livetv.stb.utils.LinkConfig.SELECTED_CATEGORY_NAME;

/**
 * A simple {@link Fragment} subclass to show menu
 */
public class FragmentMenu extends Fragment implements CategoryAdapter.OnListClickListener, ChannelListAdapter.ChannelListClickListener, Observer {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final int ALL_CHANNELS_ADDED = 99;
    private static final int CHANNEL_ITEMS_SPECIFIER = 9;
    private MenuViewModel menuViewModel;
    private FragmentMenuInteraction mListener;
    private DefaultTrackSelector trackSelector;
    private ChannelListAdapter adapter;
    CategoryAdapter categoryAdapter;
    private SharedPreferences lastPlayedPrefs;
    private List<ChannelItem> currentPlayedCategoryItems;
    private View selectedCategoryView;
    private SimpleExoPlayer player;
    DataSource.Factory dataSourceFactory;

    private Handler watchPreviewHandler = new Handler();

    SharedPreferences.Editor categoryEditor;

    private List<ChannelItem> allChannelItems, allFavItems;
    private boolean isFirstRun = false;

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
    PlayerView previewView;
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
    private int selectedCategoryPosition = 0;
    private int currentPlayingCategoryPosition = 0;
    private Login login;
    int lastPlayedId;

    private List<CategoriesWithChannels> allCategoryChannels;
    private ChannelItem currentSelected, currentPlayed;
    private ChannelItem current;
    private ErrorFragment errorFragment;
    private LinearLayoutManager categoryLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
//        playLastPlayedChannel();
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

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("frag", "view created");
        getLastPlayedChannel();
        categoryLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        categoryAdapter = new CategoryAdapter(getActivity(), FragmentMenu.this);
        categoryList.setLayoutManager(categoryLayoutManager);
        categoryList.setAdapter(categoryAdapter);
        adapter = new ChannelListAdapter(getActivity(), this);
        gvChannelsList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        gvChannelsList.setAdapter(adapter);
        getAllChannels();
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
                try {
                    int index = categoryLayoutManager.findFirstVisibleItemPosition();
                    View v = categoryLayoutManager.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - categoryLayoutManager.getPaddingTop());
                    categoryLayoutManager.scrollToPositionWithOffset(index, top);
                    layoutFav.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(selectedCategoryPosition).itemView.findViewById(R.id.channelCategory_layout).getId());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
          /*  try {
                layoutFav.setNextFocusUpId(categoryList.getChildAt(0).getId());
            } catch (Exception ignored) {
            }*/
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

                try {
                    int index = categoryLayoutManager.findFirstVisibleItemPosition();
                    View v = categoryLayoutManager.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - categoryLayoutManager.getPaddingTop());
                    categoryLayoutManager.scrollToPositionWithOffset(index, top);
                    layoutDvr.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(selectedCategoryPosition).itemView.findViewById(R.id.channelCategory_layout).getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
           /* try {
                layoutDvr.setNextFocusUpId(categoryList.getChildAt(0).getId());
            } catch (Exception ignored) {
            }*/


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
                try {
                    int index = categoryLayoutManager.findFirstVisibleItemPosition();
                    View v = categoryLayoutManager.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - categoryLayoutManager.getPaddingTop());
                    categoryLayoutManager.scrollToPositionWithOffset(index, top);
                    layoutEpg.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(selectedCategoryPosition).itemView.findViewById(R.id.channelCategory_layout).getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
          /*  try {
                layoutEpg.setNextFocusUpId(categoryList.getChildAt(0).getId());
            } catch (Exception ignored) {
            }*/
        });

    }

    private void getAllChannels() {
        LiveData<List<ChannelItem>> allchannelData = menuViewModel.getAllChannel();
        allchannelData.observe(this, channelItemList -> {
            if (channelItemList != null && channelItemList.size() > 0) {
                allChannelItems = channelItemList;
                setUpRecylerViewCategory(allChannelItems);

            }

        });
    }

    public void getLastPlayedChannel() {
        lastPlayedId = lastPlayedPrefs.getInt(CHANNEL_ID, -1);
        if (lastPlayedId != -1) {
            LiveData<ChannelItem> lastPlayedChData = menuViewModel.getLastPlayedChannel(lastPlayedId);
            lastPlayedChData.observe(this, new android.arch.lifecycle.Observer<ChannelItem>() {
                @Override
                public void onChanged(@Nullable ChannelItem channelItem) {
                    if (channelItem != null) {
                        setValues(channelItem);
//                        mListener.playChannel(channelItem);
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
//                        mListener.playChannel(channelItem);
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
        stopPreview();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void openErrorFragment() {

        ((VideoPlayActivity) Objects.requireNonNull(getActivity())).setErrorFragment(null, 0, 0);
    }

    /**
     * get Categories along with channel list from database and populate into their respective adapters
     */
    private void setUpRecylerViewCategory(List<ChannelItem> allChannelItems) {
        Log.d("frag", "recycle view created");
        LiveData<List<CategoriesWithChannels>> liveData = menuViewModel.getCategoriesWithChannels();
        liveData.observe(this, new android.arch.lifecycle.Observer<List<CategoriesWithChannels>>() {
            @Override
            public void onChanged(@Nullable List<CategoriesWithChannels> categoriesWithChannels) {

                if (categoriesWithChannels != null && categoriesWithChannels.size() != 0) {
                    liveData.removeObserver(this);
                    categoryAdapter.setCategory(categoriesWithChannels);
//                    categoryAdapter.notifyDataSetChanged();
                    setUpCategoriesToView(allChannelItems, categoryAdapter, categoriesWithChannels);
                }
            }
        });

    }


    private void setUpCategoriesToView(List<ChannelItem> channelItemList, CategoryAdapter adapter, List<CategoriesWithChannels> categoriesWithChannels) {
        adapter.setAllChannelList(channelItemList);
        /*if (playedCategoryView != null) {
            layoutEpg.setNextFocusUpId(playedCategoryView.getId());
        } else {
            try {
                layoutEpg.setNextFocusUpId(categoryList.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.channelCategory_layout).getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        checkForFavorites(channelItemList, categoriesWithChannels);

    }


    private void updateCategoryUI(List<ChannelItem> allChannelItems, List<ChannelItem> favlItemList, List<CategoriesWithChannels> allCategoryChannels) {
        String lastPlayedCategory = "";
        if (isFirstRun) {
            lastPlayedCategory = lastPlayedPrefs.getString(PLAYED_CATEGORY_NAME, "All Channels");
            isFirstRun = false;
        } else {
            lastPlayedCategory = lastPlayedPrefs.getString(SELECTED_CATEGORY_NAME, "All Channels");
        }

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
                    if (lastPlayedPrefs.getString(PLAYED_CATEGORY_NAME, getString(R.string.all_category)).equalsIgnoreCase(CATEGORY_FAVORITE)) {
                        if (channelItemList.size() > 0) {
                            currentPlayedCategoryItems = channelItemList;
                        } else {
                            currentPlayedCategoryItems = allChannelItems;
                            categoryEditor = lastPlayedPrefs.edit();
                            categoryEditor.putString(PLAYED_CATEGORY_NAME, getString(R.string.all_category));
                            categoryEditor.apply();
                            currentChannelPosition = currentPlayedCategoryItems.indexOf(currentPlayed);
                        }
                    }
                    setUpFavToView(allChannelItems, channelItemList, categoriesWithChannels);
                }
            }
        });
    }

    /**
     * fetch channelList from category list with embedded channels and populate into the channel Recycler View
     *
     * @param items
     */
    private void setUpChannelsCategory(String categoryName, int pos, List<ChannelItem> items) {
        if (items != null) {
            if (items.size() > 0) {
                noChannels.setVisibility(GONE);
                gvChannelsList.setVisibility(View.VISIBLE);
//                gvChannelsList.requestFocus();
                Collections.sort(items, new Comparator<ChannelItem>() {
                    @Override
                    public int compare(ChannelItem item, ChannelItem t1) {
                        Log.d("item priority", item.getChannelPriority() + "<===>" + t1.getChannelPriority());
                        if (item.getChannelPriority() > t1.getChannelPriority()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                adapter.setChannelItems(categoryName, pos, items);
                if (lastPlayedId != -1) {
                    selectedChannelPosition = adapter.getSelectedChannelPositionViaId(lastPlayedId);
                    currentPlayedCategoryItems = items;
                    lastPlayedId = -1;
                }
                gvChannelsList.getLayoutManager().scrollToPosition(selectedChannelPosition);
                adapter.notifyDataSetChanged();
                gvChannelsList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!gvChannelsList.isComputingLayout()) {
                            gvChannelsList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
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
                                    } catch (Exception ignored) {
                                        try {
                                            layoutEpg.setNextFocusLeftId(gvChannelsList.getLayoutManager().findViewByPosition(selectedChannelPosition).getId());
                                        } catch (Exception ignored2) {
                                            layoutEpg.setNextFocusLeftId(gvChannelsList.getChildAt(0).getId());
                                        }
                                    }

                                    try {
                                        gvChannelsList.findViewHolderForAdapterPosition(selectedChannelPosition).itemView.findViewById(R.id.relative_layout).requestFocus();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        try {
                                            gvChannelsList.getLayoutManager().findViewByPosition(selectedChannelPosition).findViewById(R.id.relative_layout).requestFocus();
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                            try {
                                                gvChannelsList.getChildAt(0).requestFocus();
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                                gvChannelsList.requestFocus();
                                            }
                                        }
                                    }

                                }
                            }, 1000);
                        }
                    }

                });

            } else {
                gvChannelsList.setVisibility(GONE);
                noChannels.setVisibility(View.VISIBLE);
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
        selectedCategoryPosition = pos;
        currentCategoryTitleView.setText(categoryName);
        currentCategoryTitleView.setSelected(true);
        categoryEditor = lastPlayedPrefs.edit();
        categoryEditor.putString(SELECTED_CATEGORY_NAME, categoryName);
        categoryEditor.apply();
        if (mListChannels.contains(currentPlayed)) {
            selectedChannelPosition = mListChannels.indexOf(currentPlayed);
        } else
            selectedChannelPosition = 0;

        setUpChannelsCategory(categoryName, pos, mListChannels);
    }

    @Override
    public void onSelectCategory(int position, View focusedCatView) {
//        stopPreview();
        this.selectedCategoryView = focusedCatView;
        this.selectedCategoryPosition = position;
        try {
            selectedCategoryView.setNextFocusDownId(gvChannelsList.getChildAt(0).getId());
        } catch (Exception e) {
            try {
                selectedCategoryView.setNextFocusDownId(gvChannelsList.getLayoutManager().findViewByPosition(0).getId());
            } catch (Exception ignored) {
            }
        }


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
            categoryEditor = lastPlayedPrefs.edit();
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

    public boolean isDvr() {
        return isDvr;
    }

    public void setDvr(boolean dvr) {
        isDvr = dvr;
    }

    private boolean isDvr;


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
        DisposableManager.dispose();
        releasePlayer();
        previewView.setVisibility(View.INVISIBLE);
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
                } catch (Exception e) {
                    initVideoView("");
                    e.printStackTrace();
                }
            }
            initVideoView("");
        });

    }

    /**
     * set Handler to stream the fecthed preview link to video view
     *
     * @param channelItem
     */

    private void startPreview(ChannelItem channelItem) {
        //TODO enable preview here
        watchPreviewHandler.postDelayed(() -> fetchPreview(channelItem), TimeUnit.SECONDS.toMillis(3));
    }

    private DataSource.Factory buildDataSourceFactory() {
        return ((ApplicationMain) ((Objects.requireNonNull(getActivity())).getApplication())).buildDataSourceFactory(BANDWIDTH_METER);
    }
    /**
     * initialise video view here
     *
     * @param link
     **/
    private void initVideoView(String link) {
        boolean needNewPlayer = player == null;
        if(needNewPlayer){
             dataSourceFactory=buildDataSourceFactory();;
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory();
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            DefaultTrackSelector.Parameters trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
            trackSelector.setParameters(trackSelectorParameters);
            DefaultAllocator allocator = new DefaultAllocator(true, 64 * 1024);
            DefaultLoadControl defaultLoadControl = new DefaultLoadControl();
            DefaultLoadControl.Builder loadControl = new DefaultLoadControl.Builder().setAllocator(allocator).setBufferDurationsMs(5000, 60000, 1000, 1000);
            defaultLoadControl = loadControl.createDefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(getActivity(), DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON,DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
            player = ExoPlayerFactory.newSimpleInstance(getActivity(), renderersFactory, trackSelector, defaultLoadControl);
        }
        player.setVolume(0f);
        player.setPlayWhenReady(true);
        previewView.setPlayer(player);
        previewView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        player.setVideoSurfaceView((SurfaceView) previewView.getVideoSurfaceView());
        String splitUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8";
        MediaSource mediaSource = buildMediaSource(Uri.parse(splitUrl));
        player.prepare(mediaSource);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                player.setPlayWhenReady(false);
                Toast.makeText(getActivity(),"Preview not available",Toast.LENGTH_SHORT).show();
                player.removeListener(this);
                releasePlayer();
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playWhenReady && playbackState==Player.STATE_READY){
                    previewView.setVisibility(View.VISIBLE);
                    previewContainer.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector=null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(dataSourceFactory),
                        buildDataSourceFactory())
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(dataSourceFactory),
                        buildDataSourceFactory())
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                    return new HlsMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(uri);
            case C.TYPE_OTHER:
                try {
                    UdpDataSource.Factory udpDataSource = dataSourceFactory;
                    return new ExtractorMediaSource.Factory(udpDataSource)
                            /*.setExtractorsFactory(new DefaultExtractorsFactory().setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
                                    | DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS))*/
                            .createMediaSource(uri);
                } catch (Exception e) {
                    return new ExtractorMediaSource.Factory(dataSourceFactory)
                            /*.setExtractorsFactory(new DefaultExtractorsFactory().setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
                                    | DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS))*/
                            .createMediaSource(uri);
                }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            stopPreview();
            isFirstRun = false;
        } else {
            if (errorFragment != null)
                showErrorFrag(errorFragment);
            updateCategoryUI(this.allChannelItems, allFavItems, allCategoryChannels);


        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
//        stopPreview();
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
                currentChannelPosition = adapter.getSelectedChannelPositionViaId(currentPlayed.getId());
                //put condition here so no IoB Exception occurs
                if (currentChannelPosition < currentPlayedCategoryItems.size() - 1)
                    currentChannelPosition++;
                else
                    currentChannelPosition = 0;

            } else if (!((ChannelChangeObserver) observable).getChannelNext()) {
                currentChannelPosition = adapter.getSelectedChannelPositionViaId(currentPlayed.getId());
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
                Objects.requireNonNull(FragmentMenu.this.getActivity()).runOnUiThread(() -> onClickChannel(lastPlayedPrefs.getString(PLAYED_CATEGORY_NAME, getString(R.string.all_channels)), currentPlayingCategoryPosition, selectedChannelPosition, currentPlayedCategoryItems));
            } catch (Exception ignored) {
            }


        }
    }


    private void setValues(ChannelItem item) {
        if (item != null) {
            channelName.setText(item.getName());
            channelDescription.setText(item.getChannelDesc());
            channelNo.setText(String.valueOf(item.getChannelPriority()));
            txtFavUnfav.setText(item.getIs_fav() == 0 ? "SET FAV" : "UNSET FAV");

        }
    }


    @OnClick(R.id.layout_epg)
    public void OnEpgClick() {
        if (currentSelected.isHasEpg()) {
            EpgFragment fragment = new EpgFragment();
            fragment.setCurrentSelectedChannel(currentSelected);
            fragment.setAllChannelList(allChannelItems);
            mListener.load(fragment, "epg");
        } else {
            Toast.makeText(getActivity(), "Epg not available for this channel", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.layout_dvr)
    public void OnDvrClick() {
        if (currentSelected.isHasDvr()) {
            mListener.loadDvr(currentSelected, allChannelItems, "dvr");
        } else {
            Toast.makeText(getActivity(), "Dvr not available for this channel", Toast.LENGTH_SHORT).show();
        }
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

    public ChannelItem getDvrPlayedChannel() {
        return dvrPlayedChannel;
    }

    public void setDvrPlayedChannel(ChannelItem dvrPlayedChannel) {
        this.dvrPlayedChannel = dvrPlayedChannel;
        if (dvrPlayedChannel != null) {
            int position = allChannelItems.indexOf(dvrPlayedChannel);
            currentChannelPosition = position;
            selectedChannelPosition = position;
            currentPlayingCategoryPosition = 0;
            categoryEditor = lastPlayedPrefs.edit();
            categoryEditor.putString(PLAYED_CATEGORY_NAME, getString(R.string.all_category));
            categoryEditor.apply();
            Timber.d("position:" + currentChannelPosition);
            currentSelected = allChannelItems.get(position);
            currentPlayed = allChannelItems.get(position);
            currentPlayedCategoryItems = allChannelItems;
            adapter.notifyDataSetChanged();
        }
    }


    public interface FragmentMenuInteraction {
        void playChannel(ChannelItem item);

        void loadDvr(ChannelItem current, List<ChannelItem> allChannelItems, String dvr);

        void load(Fragment epgFragment, String tag);

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (dvrPlayedChannel != null)
            onClickChannel(getString(R.string.all_channels), 0, allChannelItems.indexOf(getDvrPlayedChannel()), allChannelItems);

    }


    private ChannelItem dvrPlayedChannel;
}
