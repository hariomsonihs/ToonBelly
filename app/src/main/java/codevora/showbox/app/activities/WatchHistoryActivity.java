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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import codevora.showbox.app.R;
import codevora.showbox.app.adapters.HistoryAdapter;
import codevora.showbox.app.models.Video;
import codevora.showbox.app.utils.Utils;

public class WatchHistoryActivity extends AppCompatActivity implements HistoryAdapter.OnHistoryVideoListener {

    private RecyclerView historyRecycler;
    private HistoryAdapter historyAdapter;
    private List<Video> historyVideos = new ArrayList<>();
    private TextView emptyMessage;
    private static final String PREFS_HISTORY = "WatchHistory";
    private static final String KEY_HISTORY_LIST = "history_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_history);

        initializeViews();
        loadWatchHistory();
    }

    private void initializeViews() {
        historyRecycler = findViewById(R.id.history_recycler);
        emptyMessage = findViewById(R.id.empty_message);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(historyVideos, this);
        historyRecycler.setAdapter(historyAdapter);
    }

    private void loadWatchHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY_LIST, "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> historyIds = gson.fromJson(json, type);
        historyVideos.clear();

        for (String videoId : historyIds) {
            Video video = Utils.loadVideoById(this, videoId);
            if (video != null) {
                historyVideos.add(video);
            }
        }

        if (historyVideos.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            historyRecycler.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            historyRecycler.setVisibility(View.VISIBLE);
            historyAdapter.notifyDataSetChanged();
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
    public void onRemove(Video video) {
        SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY_LIST, "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> historyList = gson.fromJson(json, type);

        historyList.remove(video.getYoutubeLink());
        json = gson.toJson(historyList);
        prefs.edit().putString(KEY_HISTORY_LIST, json).apply();

        // Also remove progress if desired
        SharedPreferences progressPrefs = getSharedPreferences("VideoProgress", MODE_PRIVATE);
        progressPrefs.edit().remove("progress_" + video.getYoutubeLink()).apply();

        historyVideos.remove(video);
        if (historyVideos.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            historyRecycler.setVisibility(View.GONE);
        } else {
            historyAdapter.notifyDataSetChanged();
        }
    }
}