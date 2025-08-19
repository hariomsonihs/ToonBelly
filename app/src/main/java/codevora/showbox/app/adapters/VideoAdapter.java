package codevora.showbox.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import codevora.showbox.app.activities.VideoPlayerActivity;
import codevora.showbox.app.models.Video;
import codevora.showbox.app.R;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private List<Video> videos;
    private Context context;

    public VideoAdapter(List<Video> videos, Context context) {
        this.videos = videos;
        this.context = context;
    }

    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VideoAdapter.ViewHolder holder, int position) {
        Video video = videos.get(position);

        holder.title.setText(video.getTitle() != null ? video.getTitle() : "Unknown Title");
        holder.views.setText(formatViews(video.getViews()));
        holder.date.setText(video.getUploadDate() != null ? video.getUploadDate() : "");
        holder.description.setText(video.getDescription() != null ? video.getDescription() : "");

        if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
            Picasso.get()
                    .load(video.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_video)
                    .error(R.drawable.placeholder_video)
                    .into(holder.thumbnail);
        } else {
            holder.thumbnail.setImageResource(R.drawable.placeholder_video);
        }

        holder.itemView.setOnClickListener(v -> {
            if (video.getYoutubeLink() == null || video.getYoutubeLink().isEmpty()) {
                return;
            }
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("videoId", video.getYoutubeLink());
            intent.putExtra("title", video.getTitle() != null ? video.getTitle() : "Unknown Title");
            intent.putExtra("views", video.getViews());
            intent.putExtra("uploadDate", video.getUploadDate() != null ? video.getUploadDate() : "");
            intent.putExtra("description", video.getDescription() != null ? video.getDescription() : "");
            intent.putExtra("category", video.getCategory() != null ? video.getCategory() : "");
            intent.putExtra("thumbnailUrl", video.getThumbnailUrl() != null ? video.getThumbnailUrl() : "");
            context.startActivity(intent);
        });
    }

    private String formatViews(int views) {
        if (views >= 1000000) {
            return String.format("%.1fM views", views / 1000000.0);
        } else if (views >= 1000) {
            return String.format("%.1fK views", views / 1000.0);
        }
        return views + " views";
    }

    @Override
    public int getItemCount() {
        return videos != null ? videos.size() : 0;
    }

    public void updateList(List<Video> filteredVideos) {
        this.videos = filteredVideos != null ? filteredVideos : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, views, date, description;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);
            views = itemView.findViewById(R.id.video_views);
            date = itemView.findViewById(R.id.video_date);
            description = itemView.findViewById(R.id.video_description);
        }
    }
}