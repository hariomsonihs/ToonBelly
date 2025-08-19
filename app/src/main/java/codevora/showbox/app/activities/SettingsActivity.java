package codevora.showbox.app.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.squareup.picasso.BuildConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import codevora.showbox.app.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch themeSwitch, notificationSwitch, soundEffectsSwitch;
    private Button clearCacheButton, rateAppButton, feedbackButton;
    private SeekBar brightnessSeekBar;
    private ImageView backButton;
    private TextView appVersionText;
    private CardView appearanceCard, notificationsCard, displayCard, aboutCard;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "ShowBoxPrefs";
    private static final String KEY_THEME = "theme";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_BRIGHTNESS = "brightness";

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize executor
        executorService = Executors.newSingleThreadExecutor();

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        initializeViews();
        setupAnimations();
        loadSettings();
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        themeSwitch = findViewById(R.id.theme_switch);
        notificationSwitch = findViewById(R.id.notification_switch);
        soundEffectsSwitch = findViewById(R.id.sound_effects_switch);
        brightnessSeekBar = findViewById(R.id.brightness_seekbar);
        clearCacheButton = findViewById(R.id.clear_cache_button);
        rateAppButton = findViewById(R.id.rate_app_button);
        feedbackButton = findViewById(R.id.feedback_button);
        appVersionText = findViewById(R.id.app_version_text);

        appearanceCard = findViewById(R.id.appearance_card);
        notificationsCard = findViewById(R.id.notifications_card);
        displayCard = findViewById(R.id.display_card);
        aboutCard = findViewById(R.id.about_card);

        // Set max value for brightness seekbar
        brightnessSeekBar.setMax(100);
    }

    private void setupAnimations() {
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        appearanceCard.startAnimation(fadeInAnim);
        notificationsCard.startAnimation(fadeInAnim);
        displayCard.startAnimation(fadeInAnim);
        aboutCard.startAnimation(fadeInAnim);

        clearCacheButton.startAnimation(bounceAnim);
        rateAppButton.startAnimation(bounceAnim);
        feedbackButton.startAnimation(bounceAnim);
    }

    private void loadSettings() {
        // Theme setting
        boolean isDarkMode = prefs.getBoolean(KEY_THEME, false);
        themeSwitch.setChecked(isDarkMode);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Notification setting
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        notificationSwitch.setChecked(notificationsEnabled);

        // Sound effects setting
        boolean soundEffectsEnabled = prefs.getBoolean(KEY_SOUND_EFFECTS, true);
        soundEffectsSwitch.setChecked(soundEffectsEnabled);

        // Brightness setting
        int brightness = prefs.getInt(KEY_BRIGHTNESS, 50);
        brightnessSeekBar.setProgress(brightness);
        applyBrightness(brightness);

        // App version
        String versionName = BuildConfig.VERSION_NAME;
        appVersionText.setText("Version: " + versionName);
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Theme switch
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            saveSetting(KEY_THEME, isChecked);
            showToast(isChecked ? "Dark theme activated!" : "Light theme activated!");
        });

        // Notification switch
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_NOTIFICATIONS, isChecked);
            showToast(isChecked ? "Notifications enabled!" : "Notifications disabled!");
            // Note: Actual notification handling should be implemented in your notification service
        });

        // Sound effects switch
        soundEffectsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SOUND_EFFECTS, isChecked);
            showToast(isChecked ? "Sound effects enabled!" : "Sound effects disabled!");
            // Note: Actual sound effect handling should be implemented in relevant activities
        });

        // Brightness seekbar
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    applyBrightness(progress);
                    saveSetting(KEY_BRIGHTNESS, progress);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                showToast("Brightness set to " + seekBar.getProgress() + "%");
            }
        });

        // Clear cache button
        clearCacheButton.setOnClickListener(v -> clearAppCache());

        // Rate app button
        rateAppButton.setOnClickListener(v -> rateApp());

        // Feedback button
        feedbackButton.setOnClickListener(v -> sendFeedback());
    }

    private void applyBrightness(int progress) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = progress / 100.0f;
        getWindow().setAttributes(layoutParams);
    }

    private void clearAppCache() {
        executorService.execute(() -> {
            try {
                // Clear cache directory
                deleteDir(getCacheDir());
                deleteDir(getExternalCacheDir());

                runOnUiThread(() -> showToast("Cache cleared successfully!"));
            } catch (Exception e) {
                runOnUiThread(() -> showToast("Error clearing cache: " + e.getMessage()));
            }
        });
    }

    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new java.io.File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        }
        return dir != null && dir.delete();
    }

    private void rateApp() {
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Fallback to web browser
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        showToast("Thank you for rating our app!");
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hariomsoni0818@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "ShowBox Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, "Please share your feedback:\n\n");

        try {
            startActivity(Intent.createChooser(intent, "Send Feedback"));
            showToast("Opening email client...");
        } catch (ActivityNotFoundException e) {
            showToast("No email client found!");
        }
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void saveSetting(String key, int value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}