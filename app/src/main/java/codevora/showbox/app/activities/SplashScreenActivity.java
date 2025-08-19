package codevora.showbox.app.activities;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import codevora.showbox.app.R;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Animated background gradient
        ImageView background = findViewById(R.id.background);
        AnimationDrawable animationDrawable = (AnimationDrawable) background.getDrawable();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        // Logo animation
        ImageView logo = findViewById(R.id.logo);
        logo.setAlpha(0f);
        logo.animate()
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(500)
                .start();

        // Title animation
        TextView appName = findViewById(R.id.app_name);
        appName.setAlpha(0f);
        appName.setTranslationY(50);
        appName.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(1000)
                .setStartDelay(1000)
                .start();

        // Progress bar animation
        progressBar = findViewById(R.id.progress_bar);
        progressBar.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.progressBarColor),
                android.graphics.PorterDuff.Mode.SRC_IN);

        // Simulate loading progress
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;
                handler.post(() -> progressBar.setProgress(progressStatus));
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // When loading is complete, start main activity
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }).start();
    }
}
