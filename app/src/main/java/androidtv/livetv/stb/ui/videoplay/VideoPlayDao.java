package androidtv.livetv.stb.ui.videoplay;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Login;

@Dao
public interface VideoPlayDao {

    @Query("SELECT * FROM CATEGORY_TABLE")
    LiveData<CategoryItem> getCategories();

    @Query("SELECT * FROM CHANNEL_TABLE")
    LiveData<ChannelItem> getChannels();

    @Query("SELECT * FROM CHANNEL_TABLE WHERE category_id = :id")
    LiveData<ChannelItem> getChannels(int id);
}
