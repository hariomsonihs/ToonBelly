package codevora.showbox.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import codevora.showbox.app.R;
import codevora.showbox.app.models.Video;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private List<Video> videos;
    private OnFavoriteVideoListener listener;
    private Context context;

    public interface OnFavoriteVideoListener {
        void onVideoClick(Video video);
        void onFavoriteToggle(Video video);
    }

    public FavoritesAdapter(List<Video> videos, OnFavoriteVideoListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.videoTitle.setText(video.getTitle());
        holder.videoViews.setText(formatViews(video.getViews()));

        // Load thumbnail using Glide
        String thumbnailUrl = "https://img.youtube.com/vi/" + video.getYoutubeLink() + "/hqdefault.jpg";
        Glide.with(context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.cartoon_thumbnail_bg)
                .into(holder.videoThumbnail);

        holder.itemView.setOnClickListener(v -> listener.onVideoClick(video));
        holder.btnFavorite.setOnClickListener(v -> listener.onFavoriteToggle(video));
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnail;
        TextView videoTitle, videoViews;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoTitle = itemView.findViewById(R.id.video_title);
            videoViews = itemView.findViewById(R.id.video_views);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}