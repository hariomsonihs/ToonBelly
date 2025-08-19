package codevora.showbox.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import codevora.showbox.app.R;

public class GradientItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint paint;
    private final int dividerHeight;

    public GradientItemDecoration(Context context) {
        paint = new Paint();
        dividerHeight = (int) (16 * context.getResources().getDisplayMetrics().density); // 16dp
        int startColor = ContextCompat.getColor(context, R.color.cartoon_highlight);
        int endColor = ContextCompat.getColor(context, android.R.color.transparent);
        paint.setShader(new LinearGradient(
                0, 0, 0, dividerHeight,
                startColor, endColor,
                Shader.TileMode.CLAMP
        ));
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < parent.getChildCount() - 1; i++) {
            View child = parent.getChildAt(i);
            int bottom = child.getBottom();
            int top = bottom;
            c.drawRect(left, top, right, top + dividerHeight, paint);
        }
    }

    @Override
    public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = dividerHeight;
    }
}