package androidtv.livetv.stb.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import androidtv.livetv.stb.entity.CategoryItem;
import androidtv.livetv.stb.entity.ChannelItem;
import androidtv.livetv.stb.entity.Login;
import androidtv.livetv.stb.entity.LoginInfo;
import androidtv.livetv.stb.ui.channelLoad.CatChannelDao;
import androidtv.livetv.stb.ui.login.LoginDao;

import static androidtv.livetv.stb.utils.LinkConfig.DATABASE_NAME;

@Database(entities = {Login.class, CategoryItem.class, ChannelItem.class}, version = 4,exportSchema = false)
public abstract class AndroidTvDatabase extends RoomDatabase {
    private static AndroidTvDatabase INSTANCE;

    public abstract LoginDao loginDao();

    public abstract CatChannelDao catChannelDao();

    public static AndroidTvDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AndroidTvDatabase.class) {
                if (INSTANCE == null) {
                    //create Database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AndroidTvDatabase.class, DATABASE_NAME)
//                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();

                }
            }
        }
        return INSTANCE;
    }

   /* //To delete all content and repopulate the database whenever the app is started, you create a RoomDatabase.Callback and override onOpen().
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };*/
}
