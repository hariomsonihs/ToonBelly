package codevora.showbox.app.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codevora.showbox.app.R;
import codevora.showbox.app.adapters.RelatedVideosAdapter;
import codevora.showbox.app.models.Video;
import codevora.showbox.app.utils.Utils;

public class VideoPlayerActivity extends AppCompatActivity implements RelatedVideosAdapter.OnVideoClickListener {

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;
    private ImageButton btnFullscreen, btnFavorite;
    private TextView videoTitle, videoViews, videoDate, videoDescription;
    private RecyclerView relatedVideosRecycler;
    private RelatedVideosAdapter relatedVideosAdapter;
    private List<Video> relatedVideos = new ArrayList<>();
    private boolean isFullscreen = false;
    private String currentVideoId;
    private float currentSeconds = 0f;
    private boolean isFavorite = false;
    private boolean addedToHistory = false;
    private ConstraintLayout rootLayout;
    private static final String KEY_CURRENT_SECONDS = "current_seconds";
    private static final String KEY_IS_FULLSCREEN = "is_fullscreen";
    private static final String PREFS_FAVORITES = "FavoriteVideos";
    private static final String KEY_FAVORITE_VIDEOS = "favorite_video_ids";
    private static final String PREFS_PROGRESS = "VideoProgress";
    private static final String PREFS_HISTORY = "WatchHistory";
    private static final String KEY_HISTORY_LIST = "history_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        currentVideoId = getIntent().getStringExtra("videoId");

        // Restore state if available
        if (savedInstanceState != null) {
            currentSeconds = savedInstanceState.getFloat(KEY_CURRENT_SECONDS, 0f);
            isFullscreen = savedInstanceState.getBoolean(KEY_IS_FULLSCREEN, false);
            if (isFullscreen) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else if (currentVideoId != null) {
            // Load persistent progress if not restoring from config change
            SharedPreferences prefs = getSharedPreferences(PREFS_PROGRESS, MODE_PRIVATE);
            currentSeconds = prefs.getFloat("progress_" + currentVideoId, 0f);
        }

        initializeViews();
        setupAnimations();
        setupYouTubePlayer();
        setupClickListeners();
        loadRelatedVideos();
        checkFavoriteStatus();

        // Apply fullscreen state after views are initialized
        if (isFullscreen) {
            enterFullscreen();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(KEY_CURRENT_SECONDS, currentSeconds);
        outState.putBoolean(KEY_IS_FULLSCREEN, isFullscreen);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (youTubePlayer != null) {
            youTubePlayer.pause();
        }
        saveProgress();
    }

    private void saveProgress() {
        if (currentVideoId != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_PROGRESS, MODE_PRIVATE);
            prefs.edit().putFloat("progress_" + currentVideoId, currentSeconds).apply();
        }
    }

    private void initializeViews() {
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        btnFullscreen = findViewById(R.id.btn_fullscreen);
        btnFavorite = findViewById(R.id.btn_favorite);
        videoTitle = findViewById(R.id.video_title);
        videoViews = findViewById(R.id.video_views);
        videoDate = findViewById(R.id.video_date);
        videoDescription = findViewById(R.id.video_description);
        relatedVideosRecycler = findViewById(R.id.related_videos_recycler);
        rootLayout = findViewById(R.id.constraint_layout);

        relatedVideosRecycler.setLayoutManager(new LinearLayoutManager(this));
        relatedVideosAdapter = new RelatedVideosAdapter(relatedVideos, this, this);
        relatedVideosRecycler.setAdapter(relatedVideosAdapter);
    }

    private void setupAnimations() {
        try {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_slow);
            findViewById(R.id.related_videos_header).startAnimation(pulse);

            relatedVideosRecycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(
                    this, R.anim.layout_animation_slide_up));
        } catch (Exception e) {
            Log.e("VideoPlayerActivity", "Animation setup failed: " + e.getMessage());
        }
    }

    private void setupYouTubePlayer() {
        getLifecycle().addObserver(youTubePlayerView);

        String title = getIntent().getStringExtra("title");
        int views = getIntent().getIntExtra("views", 0);
        String uploadDate = getIntent().getStringExtra("uploadDate");
        String description = getIntent().getStringExtra("description");

        videoTitle.setText(title != null ? title : "No Title");
        videoViews.setText(formatViews(views));
        videoDate.setText(uploadDate != null ? uploadDate : "");
        videoDescription.setText(description != null ? description : "");

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                youTubePlayer = player;
                if (currentVideoId != null && !currentVideoId.isEmpty()) {
                    player.loadVideo(currentVideoId, currentSeconds);
                    try {
                        Animation bounce = AnimationUtils.loadAnimation(
                                VideoPlayerActivity.this, R.anim.bounce);
                        findViewById(R.id.youtube_container_card).startAnimation(bounce);
                    } catch (Exception e) {
                        Log.e("VideoPlayerActivity", "Bounce animation failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer player, float second) {
                currentSeconds = second;
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                if (state == PlayerConstants.PlayerState.PLAYING && !addedToHistory) {
                    addToHistory();
                    addedToHistory = true;
                }
            }
        });
    }

    private void addToHistory() {
        if (currentVideoId == null) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY_LIST, "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> historyList = gson.fromJson(json, type);

        historyList.remove(currentVideoId); // Remove if exists to move to top
        historyList.add(0, currentVideoId); // Add to beginning (recent first)

        json = gson.toJson(historyList);
        prefs.edit().putString(KEY_HISTORY_LIST, json).apply();
    }

    private void setupClickListeners() {
        btnFullscreen.setOnClickListener(v -> {
            try {
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                v.startAnimation(rotate);
                toggleFullscreen();
            } catch (Exception e) {
                Log.e("VideoPlayerActivity", "Fullscreen toggle failed: " + e.getMessage());
                Toast.makeText(this, "Failed to toggle fullscreen", Toast.LENGTH_SHORT).show();
            }
        });

        btnFavorite.setOnClickListener(v -> {
            try {
                Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale_up);
                v.startAnimation(scale);
                toggleFavorite();
            } catch (Exception e) {
                Log.e("VideoPlayerActivity", "Favorite toggle failed: " + e.getMessage());
            }
        });
    }

    private void checkFavoriteStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_FAVORITES, MODE_PRIVATE);
        Set<String> favoriteIds = prefs.getStringSet(KEY_FAVORITE_VIDEOS, new HashSet<>());
        isFavorite = favoriteIds.contains(currentVideoId);
        btnFavorite.setImageResource(isFavorite ?
                R.drawable.ic_cartoon_heart_filled : R.drawable.ic_cartoon_heart);
        try {
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.cartoon_primary));
        } catch (Exception e) {
            Log.e("VideoPlayerActivity", "Color filter failed: " + e.getMessage());
        }
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    private void enterFullscreen() {
        if (isFullscreen) return;

        try {
            isFullscreen = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) getSupportActionBar().hide();

            // Hide other views
            findViewById(R.id.video_info_card).setVisibility(View.GONE);
            findViewById(R.id.related_videos_header).setVisibility(View.GONE);
            relatedVideosRecycler.setVisibility(View.GONE);

            // Adjust YouTube player container
            ConstraintLayout.LayoutParams cardParams = (ConstraintLayout.LayoutParams) findViewById(R.id.youtube_container_card).getLayoutParams();
            cardParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
            cardParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            cardParams.dimensionRatio = null;
            findViewById(R.id.youtube_container_card).setLayoutParams(cardParams);

            // Ensure YouTubePlayerView fills the container
            FrameLayout.LayoutParams playerParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            youTubePlayerView.setLayoutParams(playerParams);

            // Ensure fullscreen button remains visible
            btnFullscreen.setVisibility(View.VISIBLE);
            FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            btnParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
            btnParams.setMargins(32, 32, 32, 32);
            btnFullscreen.setLayoutParams(btnParams);

            // Move focus to the player
            youTubePlayerView.requestFocus();
        } catch (Exception e) {
            Log.e("VideoPlayerActivity", "Enter fullscreen failed: " + e.getMessage());
            Toast.makeText(this, "Failed to enter fullscreen", Toast.LENGTH_SHORT).show();
        }
    }

    private void exitFullscreen() {
        if (!isFullscreen) return;

        try {
            isFullscreen = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) getSupportActionBar().show();

            // Show other views
            findViewById(R.id.video_info_card).setVisibility(View.VISIBLE);
            findViewById(R.id.related_videos_header).setVisibility(View.VISIBLE);
            relatedVideosRecycler.setVisibility(View.VISIBLE);

            // Restore YouTube player container
            ConstraintLayout.LayoutParams cardParams = (ConstraintLayout.LayoutParams) findViewById(R.id.youtube_container_card).getLayoutParams();
            cardParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
            cardParams.height = 0;
            cardParams.dimensionRatio = "16:9";
            findViewById(R.id.youtube_container_card).setLayoutParams(cardParams);

            // Restore YouTubePlayerView
            FrameLayout.LayoutParams playerParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            youTubePlayerView.setLayoutParams(playerParams);

            // Restore fullscreen button
            btnFullscreen.setVisibility(View.VISIBLE);
            FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            btnParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
            btnParams.setMargins(12, 12, 12, 12);
            btnFullscreen.setLayoutParams(btnParams);
        } catch (Exception e) {
            Log.e("VideoPlayerActivity", "Exit fullscreen failed: " + e.getMessage());
            Toast.makeText(this, "Failed to exit fullscreen", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite() {
        SharedPreferences prefs = getSharedPreferences(PREFS_FAVORITES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> favoriteIds = new HashSet<>(prefs.getStringSet(KEY_FAVORITE_VIDEOS, new HashSet<>()));

        isFavorite = !isFavorite;
        if (isFavorite) {
            favoriteIds.add(currentVideoId);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            favoriteIds.remove(currentVideoId);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }

        editor.putStringSet(KEY_FAVORITE_VIDEOS, favoriteIds);
        editor.apply();

        btnFavorite.setImageResource(isFavorite ?
                R.drawable.ic_cartoon_heart_filled : R.drawable.ic_cartoon_heart);
        try {
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.cartoon_primary));
        } catch (Exception e) {
            Log.e("VideoPlayerActivity", "Color filter failed: " + e.getMessage());
        }
    }

    private String formatViews(int views) {
        if (views >= 1000000) return (views / 1000000) + "M views";
        else if (views >= 1000) return (views / 1000) + "K views";
        else return views + " views";
    }

    private void loadRelatedVideos() {
        String category = getIntent().getStringExtra("category");
        if (category == null) {
            Log.e("VideoPlayerActivity", "No category provided");
            return;
        }

        relatedVideos.clear();
        List<Video> categoryVideos = Utils.loadVideosByCategory(this, category);
        if (categoryVideos != null) {
            for (Video video : categoryVideos) {
                if (!video.getYoutubeLink().equals(currentVideoId)) {
                    relatedVideos.add(video);
                }
            }
            relatedVideosAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onVideoClick(Video video) {
        if (youTubePlayer != null) {
            youTubePlayer.pause();
        }

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
    public void onBackPressed() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveProgress();
        try {
            youTubePlayerView.release();
        } catch (Exception e) {
            Log.e("VideoPlayerActivity", "YouTubePlayerView release failed: " + e.getMessage());
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!isFullscreen) {
                enterFullscreen();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isFullscreen) {
                exitFullscreen();
            }
        }
    }
}