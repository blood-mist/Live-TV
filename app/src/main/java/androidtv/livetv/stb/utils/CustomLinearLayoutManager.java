package androidtv.livetv.stb.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import timber.log.Timber;

public class CustomLinearLayoutManager extends LinearLayoutManager {

    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        if (direction == View.FOCUS_DOWN) {
            int pos = getPosition(focused);
            if (pos == getItemCount() - 1)
                return focused;
        }
        if (direction == View.FOCUS_UP) {
            int pos = getPosition(focused);
            if (pos == 0)
                return focused;
        }
        return super.onInterceptFocusSearch(focused, direction);
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_DOWN) {
            int pos = getPosition(focused);
            if (pos == getItemCount() - 1)
                return focused;
        }
        if (focusDirection == View.FOCUS_UP) {
            int pos = getPosition(focused);
            if (pos == 0)
                return focused;
        }
        return super.onFocusSearchFailed(focused, focusDirection, recycler, state);
    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {

        CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }
    }
}