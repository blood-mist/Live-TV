package androidtv.livetv.stb.ui.channelLoad;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.LoginDataDelete;
import io.reactivex.Observable;
import io.reactivex.Observer;

@Dao
public interface CatChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(List<CategoryItem> categoryItemList);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEpgs(List<Epgs> epgList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChannels(List<ChannelItem> channelItemList);

    @Query("SELECT * FROM CATEGORY_TABLE")
    LiveData<List<CategoryItem>> getCategories();


    @Query("SELECT * FROM CHANNEL_TABLE WHERE category_id=:category_id")
    LiveData<List<ChannelItem>> getChannels(int category_id);

    @Query("SELECT * FROM CHANNEL_TABLE ")
    LiveData<List<ChannelItem>> getChannels();

    @Query("SELECT count(*)FROM CATEGORY_TABLE")
    LiveData<Integer> getCatTableSize();

    @Query("SELECT * FROM EPG_TABLE WHERE channel_id = :id")
    LiveData<List<Epgs>> getEpgs(int id);

    @Query("SELECT count(*)FROM CHANNEL_TABLE")
    LiveData<Integer> getChannelTableSize();

    @Query("UPDATE CHANNEL_TABLE SET is_fav = :is_fav  WHERE channel_id = :channel_id")
    long updateFav(int is_fav, int channel_id);


    @Query("SELECT * FROM CHANNEL_TABLE  WHERE channel_id=:channel_id")
    LiveData<ChannelItem> getLastPlayedChannel(int channel_id);

    @Transaction
    @Query("SELECT * FROM CATEGORY_TABLE")
    LiveData<List<CategoriesWithChannels>> getCategoriesWithChannels();

    @Query("SELECT * FROM CHANNEL_TABLE WHERE is_fav = 1")
    LiveData<List<ChannelItem>> getFavChannels();

    @Query("DELETE FROM LOGIN_TABLE")
    int  nukeLoginTable();




}
