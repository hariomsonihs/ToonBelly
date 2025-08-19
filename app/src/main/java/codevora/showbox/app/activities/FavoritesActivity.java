package codevora.showbox.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codevora.showbox.app.R;
import codevora.showbox.app.adapters.FavoritesAdapter;
import codevora.showbox.app.models.Video;
import codevora.showbox.app.utils.Utils;

public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteVideoListener {

    private RecyclerView favoritesRecycler;
    private FavoritesAdapter favoritesAdapter;
    private List<Video> favoriteVideos = new ArrayList<>();
    private TextView emptyMessage;
    private static final String PREFS_NAME = "FavoriteVideos";
    private static final String KEY_FAVORITE_VIDEOS = "favorite_video_ids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initializeViews();
        loadFavoriteVideos();
    }

    private void initializeViews() {
        favoritesRecycler = findViewById(R.id.favorites_recycler);
        emptyMessage = findViewById(R.id.empty_message);
        favoritesRecycler.setLayoutManager(new LinearLayoutManager(this));
        favoritesAdapter = new FavoritesAdapter(favoriteVideos, this);
        favoritesRecycler.setAdapter(favoritesAdapter);
    }

    private void loadFavoriteVideos() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> favoriteIds = prefs.getStringSet(KEY_FAVORITE_VIDEOS, new HashSet<>());
        favoriteVideos.clear();

        for (String videoId : favoriteIds) {
            Video video = Utils.loadVideoById(this, videoId);
            if (video != null) {
                favoriteVideos.add(video);
            }
        }

        if (favoriteVideos.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            favoritesRecycler.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            favoritesRecycler.setVisibility(View.VISIBLE);
            favoritesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onVideoClick(Video video) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("videoId", video.getYoutubeLink());
        intent.putExtra("title", video.getTitle());
        intent.putExtra("views", video.getViews());
        intent.putExtra("uploadDate", video.getUploadDate());
        intent.putExtra("description", video.getDescription());
        intent.putExtra("category", video.getCategory());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onFavoriteToggle(Video video) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> favoriteIds = new HashSet<>(prefs.getStringSet(KEY_FAVORITE_VIDEOS, new HashSet<>()));

        favoriteIds.remove(video.getYoutubeLink());
        editor.putStringSet(KEY_FAVORITE_VIDEOS, favoriteIds);
        editor.apply();

        favoriteVideos.remove(video);
        if (favoriteVideos.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            favoritesRecycler.setVisibility(View.GONE);
        } else {
            favoritesAdapter.notifyDataSetChanged();
        }
    }
}