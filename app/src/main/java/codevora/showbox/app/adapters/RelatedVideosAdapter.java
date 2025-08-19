package codevora.showbox.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import codevora.showbox.app.R;
import codevora.showbox.app.models.Video;

public class RelatedVideosAdapter extends RecyclerView.Adapter<RelatedVideosAdapter.VideoViewHolder> {

    private List<Video> videos;
    private Context context;
    private OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(Video video);
    }

    // Constructor with listener
    public RelatedVideosAdapter(List<Video> videos, Context context, OnVideoClickListener listener) {
        this.videos = videos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.title.setText(video.getTitle());
        holder.views.setText(formatViews(video.getViews()));

        Glide.with(context)
                .load(video.getThumbnailUrl())
                .centerCrop()
                .placeholder(R.drawable.placeholder_video)
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVideoClick(video);
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    private String formatViews(int views) {
        if (views >= 1000000) return (views / 1000000) + "M views";
        else if (views >= 1000) return (views / 1000) + "K views";
        else return views + " views";
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, views;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);
            views = itemView.findViewById(R.id.video_views);
        }
    }
}
