package codevora.showbox.app.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import codevora.showbox.app.models.Video;

@Database(entities = {Video.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteDao favoriteDao();
}
