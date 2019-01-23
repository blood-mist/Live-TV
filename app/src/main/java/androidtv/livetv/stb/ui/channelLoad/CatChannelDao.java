package androidtv.livetv.stb.ui.channelLoad;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.entity.CategoriesWithChannels;
import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Epgs;
import androidtv.livetv.stb.entity.LoginDataDelete;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;

@Dao
public interface CatChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertCategory(List<CategoryItem> categoryItemList);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEpgs(List<Epgs> epgList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertChannels(List<ChannelItem> channelItemList);

    @Query("SELECT * FROM CATEGORY_TABLE")
    LiveData<List<CategoryItem>> getCategories();


    @Query("SELECT * FROM CHANNEL_TABLE ORDER BY channel_priority ASC")
    LiveData<List<ChannelItem>> getChannels();

    @Query("SELECT count(*)FROM CATEGORY_TABLE")
    LiveData<Integer> getCatTableSize();

    @Query("SELECT * FROM EPG_TABLE WHERE channel_id = :id")
    LiveData<List<Epgs>> getEpgs(int id);

    @Query("SELECT count(*)FROM CHANNEL_TABLE")
    LiveData<Integer> getChannelTableSize();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateFav(ChannelItem channelItem);

    @Query("SELECT * FROM CHANNEL_TABLE  WHERE channel_id=:channel_id")
    LiveData<ChannelItem> getLastPlayedChannel(int channel_id);

    @Transaction
    @Query("SELECT * FROM CATEGORY_TABLE ORDER BY displayOrder ASC")
    LiveData<List<CategoriesWithChannels>> getCategoriesWithChannels();

    @Query("SELECT * FROM CHANNEL_TABLE WHERE is_fav = 1")
    LiveData<List<ChannelItem>> getFavChannels();

    @Query("DELETE FROM LOGIN_TABLE")
    int  nukeLoginTable();

    @Query("SELECT category_id FROM CATEGORY_TABLE WHERE category_name=:categoryName")
    Flowable<Integer> getCategoryIdFromName(String categoryName);

    @Query("SELECT * FROM CHANNEL_TABLE WHERE category_id=:categoryId ORDER BY channel_priority ASC")
    Flowable<List<ChannelItem>>getChannelsOfCategory(int categoryId);


    @Query("SELECT * FROM EPG_TABLE")
    LiveData<List<Epgs>> getAllEpgs();


    @Query("DELETE FROM EPG_TABLE WHERE epg_id = :id")
    void deleteEpg(String id);

    @Query("DELETE  FROM EPG_TABLE WHERE channel_id = :id")
    void deleteEpgByChannel(int id);

    @Query("DELETE FROM CATEGORY_TABLE")
    void deleteCategory();

    @Query("DELETE FROM CHANNEL_TABLE")
    void deleteChannel();

    @Query("SELECT * FROM CHANNEL_TABLE  LIMIT 1")
    LiveData<ChannelItem> getFirstChannel();
}
