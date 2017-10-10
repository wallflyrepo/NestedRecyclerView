package nestedrecyclerview.wallfly.com.nestedrecyclerview;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by Josh Williams on 10/9/17.
 */

class ViewUtils {

    private final Context mContext;

    public ViewUtils(Context context) {
        mContext = context;
    }

    /**
     * Get height of screen when in portrait (we want largest screen size)
     */
    public int getScreenHeight() {
        DisplayMetrics screenDimensions = mContext.getResources().getDisplayMetrics();
        int deviceHeight;
        if (screenDimensions.heightPixels > screenDimensions.widthPixels) {
            deviceHeight = screenDimensions.heightPixels;
        } else  {
            deviceHeight = screenDimensions.widthPixels;
        }
        return deviceHeight;
    }


    public boolean hasParent(View view, Class clazz) {
        ViewParent parent = view.getParent();
        boolean hasParent = false;
        while (parent != null) {
            if(clazz.isInstance(parent)) {
                hasParent = true;
                break;
            }
            parent = parent.getParent();
        }
        return hasParent;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getParent(View view, Class clazz) {
        ViewParent parent = view.getParent();
        ViewParent targetParent = null;
        while (parent != null) {
            if(clazz.isInstance(parent)) {
                targetParent = parent;
                break;
            }
            parent = parent.getParent();
        }
        return (T) targetParent;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getParent(View view) {
        ViewParent parent = view.getParent();
        ViewParent actualParent = parent;
        while (parent != null) {
            actualParent = parent;
            parent = parent.getParent();
        }
        return (T) actualParent;
    }


    public void enableAppBarExpansion(AppBarLayout appBarLayout, boolean isEnabled) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            if (!isEnabled) {
                behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                    @Override
                    public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                        return false;
                    }
                });
            } else {
                behavior.setDragCallback(null);
            }
        }
    }

    public ViewGroup getRootView(Activity activity) {
        return activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    public boolean hasChild(ViewGroup parent, Class clazz) {
        boolean hasChild = false;
        for(int i = 0; i < parent.getChildCount() - 1; i++) {
            View view = parent.getChildAt(i);
            if (clazz.isInstance(view)) {
                hasChild = true;
            } else if (view instanceof ViewGroup) {
                hasChild = hasChild((ViewGroup) view, clazz);
            }
            if(hasChild) {
                break;
            }
        }
        return hasChild;
    }


    public int[] getTopLeftPoint(View view) {
        int[] location = new int[Point.NUMBER_OF_POINTS];
        view.getLocationOnScreen(location);
        return location;
    }

    public int getTopLeftPointX(View view) {
        return getTopLeftPoint(view)[Point.X];
    }

    public int getTopLeftPointY(View view) {
        return getTopLeftPoint(view)[Point.Y];
    }

    public static class Defaults {

        public static final int NO_ATTR_VALUE = -1;

    }

    public static class Point {

        public static final int NUMBER_OF_POINTS = 2;

        public static final int X = 0;

        public static final int Y = 1;

    }

}
