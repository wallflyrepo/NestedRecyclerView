package nestedrecyclerview.wallfly.com.nestedrecyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by Josh Williams on 10/9/17.
 */

public class NestedRecyclerView extends RecyclerView {

    private static final String TAG = NestedRecyclerView.class.getSimpleName();

    private static final int TOP_LEFT_OF_SCREEN_Y = 0;

    private static final int SCROLL_DIRECTION_UP = -1;

    private ViewUtils mViewUtils = new ViewUtils(getContext());

    private TypedArray mAttrs;

    private AppBarLayout mAppBarLayout;

    private AppBarLayout.OnOffsetChangedListener mAppBarChangeListener;

    private NestedScrollView.OnScrollChangeListener mNestedScrollViewOnScrollChangeListener;

    private OnScrollListener mOnScrollListener;

    private boolean mAppBarCollapsed;

    public NestedRecyclerView(Context context) {
        super(context);
    }

    public NestedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.NestedRecyclerView);
    }

    public NestedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.NestedRecyclerView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, getHeightMeasureSpec(heightMeasureSpec));
    }

    private int getHeightMeasureSpec(int heightMeasureSpec) {
        return shouldEnableEfficientScrolling() ? MeasureSpec.makeMeasureSpec(mViewUtils.getScreenHeight(),
                MeasureSpec.EXACTLY) : heightMeasureSpec;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setAppBarFromAttributeIfNeeded();
        setEfficientScrollingEnabled(shouldEnableEfficientScrolling());
        mAttrs.recycle();
    }

    private boolean shouldEnableEfficientScrolling() {
        return isInsideNestedScrollView();
    }

    public void setAppBar(AppBarLayout appBar) {
        mAppBarLayout = appBar;
        setEfficientScrollingEnabled(shouldEnableEfficientScrolling());
    }

    public void setNestedScrollViewOnScrollChangeListener(NestedScrollView.OnScrollChangeListener listener) {
        mNestedScrollViewOnScrollChangeListener = listener;
    }

    private void setEfficientScrollingEnabled(boolean enabled) {
        if(enabled) {
            enableEfficientScrolling();
        }
    }

    private void disableEfficientScrolling() {
        detachListeners();
    }

    private void enableEfficientScrolling() {
        if(hasAppBar() && appBarIsCollapsible()) {
            setUpAppBarLayoutListener();
            disableAppBar();
        } else {
            setUpNestedScrollViewListener();
        }
        addOnScrollEnabledListener();
    }

    private void setAppBarFromAttributeIfNeeded() {
        int appBarId = mAttrs.getResourceId(R.styleable.NestedRecyclerView_app_bar_layout, ViewUtils.Defaults.NO_ATTR_VALUE);
        if(appBarId != ViewUtils.Defaults.NO_ATTR_VALUE) {
            Activity activity = getActivity();
            setAppBar((AppBarLayout) activity.findViewById(appBarId));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpNestedScrollViewListener() {
        NestedScrollView nestedScrollView = getNestedScrollView();
        nestedScrollView.setOnScrollChangeListener(setNestedScrollingEnabledFromScrollPosition());
    }

    private NestedScrollView.OnScrollChangeListener setNestedScrollingEnabledFromScrollPosition() {
        return new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                setNestedScrollingEnabledCompat(shouldNestedScrollingBeEnabled());
                sendNestedScrollViewOnScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
            }
        };
    }

    private void sendNestedScrollViewOnScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if(mNestedScrollViewOnScrollChangeListener != null) {
            mNestedScrollViewOnScrollChangeListener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
        }
    }

    private NestedScrollView getNestedScrollView() {
        return mViewUtils.getParent(this, NestedScrollView.class);
    }

    private boolean appBarIsCollapsible() {
        return activityHasView(CollapsingToolbarLayout.class);
    }

    private boolean isInsideNestedScrollView() {
        return mViewUtils.hasParent(this, NestedScrollView.class);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disableEfficientScrolling();
    }

    private void disableAppBar() {
        mViewUtils.enableAppBarExpansion(mAppBarLayout, false);
    }

    private void addOnScrollEnabledListener() {
        mOnScrollListener = new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                setNestedScrollingEnabledCompat(shouldNestedScrollingBeEnabled());
            }

        };
        this.addOnScrollListener(mOnScrollListener);
    }

    private boolean shouldNestedScrollingBeEnabled() {
        boolean shouldNestedScrollingBeEnabled = true;
        if(determineFromAppBar()) {
            shouldNestedScrollingBeEnabled = canScrollUp();
        } else if(determineFromNestedScrollView())  {
            shouldNestedScrollingBeEnabled = hasReachedTopOfScreen();
        }
        return shouldNestedScrollingBeEnabled;
    }

    private boolean activityHasView(Class clazz) {
        Activity activity = getActivity();
        ViewGroup rootView = mViewUtils.getRootView(activity);
        return mViewUtils.hasChild(rootView, clazz);
    }

    private boolean determineFromNestedScrollView() {
        return !appBarIsCollapsible();
    }

    private boolean hasReachedTopOfScreen() {
        return mViewUtils.getTopLeftPointY(this) == TOP_LEFT_OF_SCREEN_Y;
    }

    private boolean determineFromAppBar() {
        return hasAppBar() && appBarIsCollapsible() && !mAppBarCollapsed;
    }

    private boolean canScrollUp() {
        return this.canScrollVertically(SCROLL_DIRECTION_UP);
    }

    private void detachListeners() {
        removeAppBarListeners();
        removeListenersFromSelf();
        removeListenersFromNestedScrollView();
    }

    private void removeListenersFromNestedScrollView() {
        if(isInsideNestedScrollView()) {
            NestedScrollView nestedScrollView = getNestedScrollView();
            //This removes our listener from the nested scroll view, if the user has one set,
            //it will just user theirs, if it is not set, this will be null, which will set the listener
            //to none anyways
            nestedScrollView.setOnScrollChangeListener(mNestedScrollViewOnScrollChangeListener);
        }
    }

    private void removeListenersFromSelf() {
        this.removeOnScrollListener(mOnScrollListener);
    }

    private void removeAppBarListeners() {
        if(hasAppBar()) {
            mAppBarLayout.removeOnOffsetChangedListener(mAppBarChangeListener);
        }
    }

    private boolean hasAppBar() {
        return mAppBarLayout != null;
    }

    private void setUpAppBarLayoutListener() {
        mAppBarChangeListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBar, int verticalOffset) {
                mAppBarCollapsed = Math.abs(verticalOffset) == appBar.getTotalScrollRange();
                mOnScrollListener.onScrolled(NestedRecyclerView.this, -1, -1);
            }
        };
        mAppBarLayout.addOnOffsetChangedListener(mAppBarChangeListener);
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    public void setNestedScrollingEnabledCompat(boolean enable) {
        if(enable != this.isNestedScrollingEnabled()) {
            ViewCompat.setNestedScrollingEnabled(this, enable);
        }
    }

}
