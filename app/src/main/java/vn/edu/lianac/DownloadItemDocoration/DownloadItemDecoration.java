package vn.edu.lianac.DownloadItemDocoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DownloadItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable mDivider;

    /**
     * @param context The context to fetch the divider Drawable from.
     * @param dividerResId The resource ID of the Drawable (e.g., R.drawable.divider_line).
     */
    public DownloadItemDecoration(Context context, int dividerResId) {
        mDivider = ContextCompat.getDrawable(context, dividerResId);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            // Don't draw a divider after the last item
            if (i < childCount - 1) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}