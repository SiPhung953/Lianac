package vn.edu.lianac.Download.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.lianac.Download.DownloadItem.DownloadItem;
import vn.edu.lianac.Download.db.converters.DownloadStateConverter;

@Database(entities = {DownloadItem.class}, version = 1, exportSchema = false)
@TypeConverters({DownloadStateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract DownloadDao downloadDao();

    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "download_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
