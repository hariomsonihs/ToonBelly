package codevora.showbox.app.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import codevora.showbox.app.R;
import codevora.showbox.app.adapters.VideoAdapter;
import codevora.showbox.app.models.Video;
import codevora.showbox.app.utils.Utils;

public class CategoryVideosActivity extends AppCompatActivity {

    private RecyclerView videoRecycler;
    private VideoAdapter videoAdapter;
    private List<Video> videos;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        videoRecycler = findViewById(R.id.video_recycler);

        // ✅ Intent se category name lo
        if (getIntent() != null && getIntent().hasExtra("categoryName")) {
            categoryName = getIntent().getStringExtra("categoryName");
        } else {
            categoryName = "Unknown";
        }

        TextView categoryTitle = findViewById(R.id.category_title);
        if (categoryTitle != null) {
            categoryTitle.setText(categoryName);
        }

        // ✅ Load videos for this category
        videos = Utils.loadVideosByCategory(this, categoryName);

        if (videos != null && !videos.isEmpty()) {
            videoAdapter = new VideoAdapter(videos, this);
            videoRecycler.setLayoutManager(new LinearLayoutManager(this));
            videoRecycler.setAdapter(videoAdapter);
        }
    }
}
