package androidtv.livetv.stb.ui.login;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import androidtv.livetv.stb.entity.Login;
@Dao
public interface LoginDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Login login);

    @Query("DELETE FROM LOGIN_TABLE")
    void deleteAll();

    @Query("SELECT * FROM LOGIN_TABLE")
    LiveData< Login> getLoginData();

    @Query("SELECT count(*)FROM LOGIN_TABLE")
    LiveData<Integer> getTableSize();
}
