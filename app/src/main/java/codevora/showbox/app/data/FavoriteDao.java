package codevora.showbox.app.data;

import androidx.room.*;

import java.util.List;

import codevora.showbox.app.models.Video;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Video video);

    @Delete
    void delete(Video video);

    @Query("SELECT * FROM Video")
    List<Video> getAll();
}
