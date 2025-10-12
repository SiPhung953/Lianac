package vn.edu.lianac.Download.db.converters;

import androidx.room.TypeConverter;
import vn.edu.lianac.Download.DownloadState.DownloadState;

public class DownloadStateConverter {
    @TypeConverter
    public static DownloadState toState(String state) {
        return state == null ? null : DownloadState.valueOf(state);
    }

    @TypeConverter
    public static String fromState(DownloadState state) {
        return state == null ? null : state.name();
    }
}
