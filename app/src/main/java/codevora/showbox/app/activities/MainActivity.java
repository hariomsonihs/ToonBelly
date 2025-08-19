package codevora.showbox.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import codevora.showbox.app.R;
import codevora.showbox.app.adapters.VideoAdapter;
import codevora.showbox.app.models.Video;
import codevora.showbox.app.utils.GradientItemDecoration;
import codevora.showbox.app.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private RecyclerView videoRecycler;
    private ImageButton searchIcon;
    private FloatingActionButton fabRandom;
    private VideoAdapter adapter;
    private List<Video> videos;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        videoRecycler = findViewById(R.id.video_recycler);
        searchIcon = findViewById(R.id.header_search_icon);
        fabRandom = findViewById(R.id.fab_random);
        bottomNav = findViewById(R.id.bottom_nav);

        // Setup animations
        setupAnimations();

        // Load videos from videos.json
        videos = Utils.loadVideosFromAssets(this, "videos.json");
        if (videos != null && !videos.isEmpty()) {
            adapter = new VideoAdapter(videos, this);
            videoRecycler.setLayoutManager(new LinearLayoutManager(this));
            videoRecycler.addItemDecoration(new GradientItemDecoration(this)); // Add custom decoration
            videoRecycler.setAdapter(adapter);
        } else {
            Log.e("MainActivity", "Failed to load videos or videos list is empty");
        }

        // Search icon click with animation
        searchIcon.setOnClickListener(v -> {
            Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
            searchIcon.startAnimation(bounceAnim);
            showSearchDialog();
        });

        // Random video FAB
        fabRandom.setOnClickListener(v -> {
            Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
            fabRandom.startAnimation(rotateAnim);
            playRandomVideo();
        });

        // Bottom Navigation with animation
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivityWithAnimation(new Intent(this, FavoritesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivityWithAnimation(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_categories) {
                startActivityWithAnimation(new Intent(this, CategoryActivity.class));
                return true;
            } else if (id == R.id.nav_watch_history) {
                startActivityWithAnimation(new Intent(this, WatchHistoryActivity.class));
            }  return true;
        });
    }

    private void setupAnimations() {
        // Logo bounce animation
        ImageView logo = findViewById(R.id.logo);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        bounce.setStartOffset(300);
        logo.startAnimation(bounce);

        // Decorative elements animation
        ImageView starDecoration = findViewById(R.id.star_decoration);
        Animation starPulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        starDecoration.startAnimation(starPulse);
    }

    private void startActivityWithAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // ✅ Updated method to play random video
    private void playRandomVideo() {
        if (videos != null && !videos.isEmpty()) {
            int randomIndex = (int) (Math.random() * videos.size());
            Video randomVideo = videos.get(randomIndex);

            Log.d("MainActivity", "Playing random video: " + randomVideo.getTitle());

            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("videoId", randomVideo.getYoutubeLink());
            intent.putExtra("title", randomVideo.getTitle());
            intent.putExtra("views", randomVideo.getViews());
            intent.putExtra("uploadDate", randomVideo.getUploadDate());
            intent.putExtra("description", randomVideo.getDescription());
            intent.putExtra("category", randomVideo.getCategory());
            startActivity(intent);
        }
    }

    private void showSearchDialog() {
        // (Unchanged code for search dialog)
        Dialog searchDialog = new Dialog(this, R.style.CartoonDialogTheme);
        searchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        searchDialog.setContentView(R.layout.dialog_search);

        Window window = searchDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setWindowAnimations(R.style.DialogAnimation);
        }

        SearchView searchView = searchDialog.findViewById(R.id.search_view);
        RecyclerView searchRecycler = searchDialog.findViewById(R.id.search_results_recycler);
        MaterialCardView searchCard = searchDialog.findViewById(R.id.search_card);
        ImageView closeButton = searchDialog.findViewById(R.id.close_button);

        VideoAdapter searchAdapter = new VideoAdapter(videos, this);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchRecycler.addItemDecoration(new GradientItemDecoration(this));
        searchRecycler.setAdapter(searchAdapter);
        searchRecycler.setHasFixedSize(true);

        searchRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 5) {
                    searchCard.animate().translationY(-20f).setDuration(200).start();
                } else if (dy < -5) {
                    searchCard.animate().translationY(0f).setDuration(200).start();
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query, searchAdapter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText, searchAdapter);
                return true;
            }
        });

        closeButton.setOnClickListener(v -> {
            Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
            searchCard.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) { searchDialog.dismiss(); }
                @Override public void onAnimationRepeat(Animation animation) {}
            });
        });

        searchView.postDelayed(() -> {
            searchView.requestFocus();
            searchView.onActionViewExpanded();
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.search_bounce);
            searchCard.startAnimation(bounce);
        }, 100);

        searchDialog.show();
    }

    private void performSearch(String query, VideoAdapter adapter) {
        List<Video> filtered = Utils.searchVideos(videos, query);
        adapter.updateList(filtered);

        if (filtered.size() > 0) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            adapter.notifyItemRangeInserted(0, filtered.size());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView logo = findViewById(R.id.logo);
        logo.clearAnimation();
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        bounce.setStartOffset(300);
        logo.startAnimation(bounce);
    }
}
