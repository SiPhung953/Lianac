package vn.edu.lianac.Download.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.lianac.Download.DownloadItem.DownloadItem;

@Dao
public interface DownloadDao {
    @Query("SELECT * FROM downloads")
    LiveData<List<DownloadItem>> getAllDownloads();

    @Query("SELECT * FROM downloads WHERE url = :url LIMIT 1")
    DownloadItem findByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DownloadItem download);

    @Update
    void update(DownloadItem download);

    @Delete
    void delete(DownloadItem download);

    @Query("DELETE FROM downloads WHERE url = :url")
    void deleteByUrl(String url);
}
