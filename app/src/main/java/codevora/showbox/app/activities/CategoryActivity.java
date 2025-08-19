package codevora.showbox.app.activities;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import codevora.showbox.app.R;
import codevora.showbox.app.adapters.CategoryAdapter;
import codevora.showbox.app.models.Category;
import codevora.showbox.app.utils.Utils;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        RecyclerView categoryRecycler = findViewById(R.id.video_recycler);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Load categories with cartoon-style animation
        List<Category> categories = Utils.loadCategoriesFromAssets(this);
        if (categories != null && !categories.isEmpty()) {
            CategoryAdapter adapter = new CategoryAdapter(categories, this);
            categoryRecycler.setAdapter(adapter);

            // Add item animation
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(
                    this, R.anim.layout_animation_fall_down);
            categoryRecycler.setLayoutAnimation(animation);
        }
    }
}