package androidtv.livetv.stb.ui.videoplay.adapters;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

public class MyCustomLayoutManager extends LinearLayoutManager {
    public MyCustomLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MyCustomLayoutManager(Context context) {
        super(context);
    }

    public MyCustomLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {
        private static  float MILLISECONDS_PER_PX = 0;
        private static float MILLISECONDS_PER_INCH = 100f;

        CenterSmoothScroller(Context context) {
            super(context);
            MILLISECONDS_PER_PX=calculateSpeedPerPixel(context.getResources().getDisplayMetrics());
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }
        @Override
        protected float calculateSpeedPerPixel
                (DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH/displayMetrics.densityDpi;
        }
        @Override
        protected int calculateTimeForScrolling(int dx) {
            return (int) Math.ceil(Math.abs(dx) * MILLISECONDS_PER_PX);
        }
    }
}