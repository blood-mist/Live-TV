package androidtv.livetv.stb.ui.videoplay.fragments.menu;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import androidtv.livetv.stb.R;
import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.ChannelLinkResponse;
import androidtv.livetv.stb.entity.FavoriteResponse;
import androidtv.livetv.stb.entity.GlobalVariables;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.ui.utc.GetUtc;
import androidtv.livetv.stb.ui.videoplay.ChannelChangeObserver;
import androidtv.livetv.stb.ui.videoplay.VideoPlayActivity;
import androidtv.livetv.stb.ui.videoplay.adapters.CategoryAdapter;
import androidtv.livetv.stb.ui.videoplay.adapters.ChannelListAdapter;


import androidtv.livetv.stb.utils.LinkConfig;

import androidtv.livetv.stb.ui.videoplay.fragments.dvr.DvrFragment;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import androidtv.livetv.stb.ui.videoplay.fragments.epg.EpgFragment;
import static android.support.constraint.Constraints.TAG;
import static androidtv.livetv.stb.utils.LinkConfig.CHANNEL_ID;

/**
 * A simple {@link Fragment} subclass to show menu
 */
public class FragmentMenu extends Fragment implements CategoryAdapter.OnListClickListener, ChannelListAdapter.ChannelListClickListener, Observer {

    private MenuViewModel menuViewModel;
    private FragmentMenuInteraction mListener;
    private int channelIds;
    private ChannelListAdapter adapter;
    private SharedPreferences lastPlayedPrefs;
    private int selectedCurrentChannelId;
    private int lastPlayedPosition = 0;

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


    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    private int catId = -1;


    private int currentChannelPosition = -1;
    private int selectedChannelPosition = -1;
    private Login login;

    private Handler watchPreviewHandler = new Handler();

    private ChannelItem current;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fragment_menu, container, false);
        ButterKnife.bind(this, v);
        this.login = GlobalVariables.login;
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<ChannelItem> allChannelItem = new ArrayList<>();
        super.onViewCreated(view, savedInstanceState);
        Log.d("frag", "view created");
        menuViewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
        menuViewModel.getCategoriesWithChannels().observe(this, categoriesWithChannels -> {
            if (categoriesWithChannels != null) {
                Log.d(TAG, categoriesWithChannels.get(0).channelItemList.size() + "");
                for (CategoriesWithChannels catWithCh : categoriesWithChannels) {
                    allChannelItem.addAll(catWithCh.channelItemList);
                }
            }
        });


        setUpRecylerViewCategory();
        playLastPlayedChannel();
        gvChannelsList.setNextFocusRightId(layoutEpg.getId());
        txtEpg.setNextFocusUpId(categoryList.getId());
        txtDvr.setNextFocusUpId(categoryList.getId());
        txtFavUnfav.setNextFocusUpId(categoryList.getId());
        focusHelper.setOnFocusChangeListener((view1, b) -> {
            if (b)
                layoutEpg.requestFocus();
        });

        layoutEpg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    txtEpg.setTextScaleX(1.5f);
                }else{
                    txtEpg.setTextScaleX(1.0f);
                }
            }
        });

        layoutDvr.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    txtDvr.setTextScaleX(1.5f);
                }else {
                    txtDvr.setTextScaleX(1.0f);
                }
            }
        });


    }

    private void playLastPlayedChannel() {
        int lastPlayedId = lastPlayedPrefs.getInt(CHANNEL_ID, -1);
        if (lastPlayedId != -1) {
            menuViewModel.getLastPlayedChannel(lastPlayedId).observe(this, channelItem -> {
                if (channelItem != null) {
                    setValues(channelItem);
                    mListener.playChannel(channelItem);
                }
            });
        } else {
            openErrorFragment();
        }
    }

    private void openErrorFragment() {
        ((VideoPlayActivity) Objects.requireNonNull(getActivity())).openErrorFragment();
    }

    /**
     * get Categories along with channel list from database and populate into their respective adapters
     */
    private void setUpRecylerViewCategory() {
        Toast.makeText(getActivity(), "Setting recycleing view for category", Toast.LENGTH_SHORT).show();
        Log.d("frag", "recycle view created");
        CategoryAdapter adapter = new CategoryAdapter(getActivity(), this);
        categoryList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        categoryList.setAdapter(adapter);

        menuViewModel.getCategoriesWithChannels().observe(this, categoriesWithChannels -> {
            if (categoriesWithChannels != null) {
                adapter.setCategory(categoriesWithChannels);

                CategoriesWithChannels item = categoriesWithChannels.get(1);
                List<ChannelItem> channelItems = item.channelItemList;
                Log.d(TAG, channelItems.size() + "");
                List<ChannelItem> channelItemList = new ArrayList<>();
                for (CategoriesWithChannels withChannels : categoriesWithChannels) {
                    channelItemList.addAll(withChannels.channelItemList);
                }
                adapter.setAllChannelList(channelItemList);
                onClickCategory("All Channels", channelItemList);
            }
        });


    }

    /**
     * fetch channelList from category list with embedded channels and populate into the channel Recycler View
     *
     * @param items
     */
    private void setUpChannelsCategory(List<ChannelItem> items) {
        if (items != null) {
            if(items.size()>0) {
                adapter = new ChannelListAdapter(getActivity(), this);
                gvChannelsList.setLayoutManager(new LinearLayoutManager(getActivity()));
                gvChannelsList.setAdapter(adapter);
                adapter.setChannelItems(items);
                selectedCurrentChannelId = adapter.getmList().get(lastPlayedPosition).getId();
                current = adapter.getmList().get(lastPlayedPosition);
            }
        }
        //        menuViewModel.getChannels(id).observe(this, channelItems -> {
//            adapter.setChannelItems(channelItems);
//
//        });
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
        setUpChannelsCategory(mListChannels);
    }


    /**
     * prompt the listener to initialise channel Play Event on channel clicked
     *
     * @param position
     */
    @Override
    public void onClickChannel(int position) {
        onChannelFocused(position);
        currentChannelPosition = position;
        mListener.playChannel(adapter.getmList().get(currentChannelPosition));
        selectedCurrentChannelId = adapter.getmList().get(position).getId();
        current = adapter.getmList().get(position);
        mListener.playChannel(adapter.getmList().get(position));
    }


    /**
     * update UI events and preview status on channel list navigation
     *
     * @param position
     */
    @Override
    public void onChannelFocused(int position) {
        selectedChannelPosition = position;
        setValues(adapter.getmList().get(position));
        stopPreview();
        current = adapter.getmList().get(position);
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
     */
    private void initVideoView(String link) {
        previewView.setVideoPath(link);
        previewView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.start();
            previewContainer.setVisibility(View.VISIBLE);
        });
        previewView.setOnErrorListener((mp, what, extra) -> {
            previewContainer.setVisibility(View.INVISIBLE);
//                Toast.makeText(getActivity(), "Error Code: \t W"+what+"E"+extra, Toast.LENGTH_SHORT).show();
            Timber.e("Media Error: ", "what = " + what + " extra = " + extra);
            return true;
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden)
            stopPreview();
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

    @OnClick(R.id.txt_fav_unfav)
    public void onFavClick() {
        ChannelItem selectedChannel=adapter.getmList().get(selectedChannelPosition);
        int toFavUnfavId = selectedChannel.getId();
        int favStatus=selectedChannel.getIs_fav()==0?1:0;
        menuViewModel.addChannelToFavorite(favStatus,toFavUnfavId);
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
     * Observe Up & down key press event from activity  and change the current playing channel accordingly
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


    public interface FragmentMenuInteraction {
        void playChannel(ChannelItem item);
        void load(Fragment epgFragment,String tag);


    }

    private void setValues(ChannelItem item) {
        if (item != null) {
            channelName.setText(item.getName());
            channelDescription.setText(item.getChannelDesc());
            amount.setText(item.getPrice());
            channelNo.setText(String.valueOf(item.getChannelPriority()));

        }
    }


    @OnClick(R.id.layout_epg)
    public void OnEpgClick(){
        EpgFragment fragment = new EpgFragment();
        fragment.setSelectedChannelId(selectedCurrentChannelId);
        mListener.load(fragment,"epg");
    }

    @OnClick(R.id.layout_dvr)
    public void OnDvrClick(){
        DvrFragment fragment = new DvrFragment();
        fragment.setCurrentChannel(current);
        mListener.load(fragment,"Dvr");
    }


}
