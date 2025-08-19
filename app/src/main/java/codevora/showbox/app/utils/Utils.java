package codevora.showbox.app.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import codevora.showbox.app.models.Category;
import codevora.showbox.app.models.Video;

public class Utils {

    // Load categories.json
    public static List<Category> loadCategoriesFromAssets(Context context) {
        List<Category> categories = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("categories.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            Type listType = new TypeToken<List<Category>>() {}.getType();
            categories = new Gson().fromJson(json, listType);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Utils", "Error loading categories: " + e.getMessage());
        }
        return categories;
    }

    // Load videos.json
    public static List<Video> loadVideosFromAssets(Context context, String fileName) {
        List<Video> videos = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            Type listType = new TypeToken<List<Video>>() {}.getType();
            videos = new Gson().fromJson(json, listType);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Utils", "Error loading videos: " + e.getMessage());
        }
        return videos;
    }

    // Load videos by category
    public static List<Video> loadVideosByCategory(Context context, String category) {
        List<Video> allVideos = loadVideosFromAssets(context, "videos.json");
        List<Video> filtered = new ArrayList<>();
        for (Video v : allVideos) {
            if (v.getCategory() != null && v.getCategory().equalsIgnoreCase(category)) {
                filtered.add(v);
            }
        }
        return filtered;
    }

    // Load video by ID
    public static Video loadVideoById(Context context, String videoId) {
        try {
            List<Video> allVideos = loadVideosFromAssets(context, "videos.json");
            for (Video video : allVideos) {
                if (video.getYoutubeLink() != null && video.getYoutubeLink().equals(videoId)) {
                    return video;
                }
            }
            Log.w("Utils", "Video with ID " + videoId + " not found");
            return null;
        } catch (Exception e) {
            Log.e("Utils", "Error loading video by ID: " + e.getMessage());
            return null;
        }
    }

    // Search categories
    public static List<Category> searchCategories(List<Category> categories, String query) {
        if (query == null || query.trim().isEmpty()) return categories;
        String lowerQuery = query.toLowerCase();
        List<Category> filtered = new ArrayList<>();
        for (Category c : categories) {
            if (c.getName() != null && c.getName().toLowerCase().contains(lowerQuery)) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    // Search videos
    public static List<Video> searchVideos(List<Video> videos, String query) {
        if (query == null || query.trim().isEmpty()) return videos;
        String lowerQuery = query.toLowerCase();
        List<Video> filtered = new ArrayList<>();
        for (Video v : videos) {
            if ((v.getTitle() != null && v.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (v.getDescription() != null && v.getDescription().toLowerCase().contains(lowerQuery)) ||
                    (v.getCategory() != null && v.getCategory().toLowerCase().contains(lowerQuery))) {
                filtered.add(v);
            }
        }
        return filtered;
    }
}