package com.chris.apps.uiscroll;

import com.chris.apps.uiscroll.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class UIScrollLayout extends ViewGroup {

	private final static String TAG = "UIScrollLayout";
	private int mCurScreen = 0;
	
	private final static String ATTR_NAVIGATOR	= "navigator";
	private final static String ATTR_SLIDEMENU	= "slidemenu";
	public final static int VIEW_NAVIGATOR 		= 0;
	public final static int VIEW_MAIN_SLIDEMENU	= 1;
	private int mViewType = VIEW_NAVIGATOR;

	private int mTouchSlop = 0;
	private int mLastX = 0;
	private VelocityTracker mVelocityTracker = null;
	private final static int VELOCITY_X_DISTANCE = 1000;

	private Scroller mScroller = null;
	
	public UIScrollLayout(Context context) {
		this(context, null);
	}
	
	public UIScrollLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public UIScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UIScroll);
		String type = a.getString(R.styleable.UIScroll_view_type);
		a.recycle();
		
		Log.d(TAG, "type = " + type);
		if(type.equals(ATTR_NAVIGATOR)){
			mViewType = VIEW_NAVIGATOR;
		}else if(type.equals(ATTR_SLIDEMENU)){
			mViewType = VIEW_MAIN_SLIDEMENU;
		}

		mScroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop(); 
		Log.d(TAG, "mTouchSlop = " + mTouchSlop);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if(mViewType == VIEW_NAVIGATOR){
			for(int i = 0; i < getChildCount(); i ++){
				getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
			}
		}else if(mViewType == VIEW_MAIN_SLIDEMENU){
			for(int i = 0; i < getChildCount(); i ++){
				View child = getChildAt(i);
				LayoutParams lp = child.getLayoutParams();
				int widthSpec = 0;
				if(lp.width > 0){
					widthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
				}else{
					widthSpec = widthMeasureSpec;
				}
				
				child.measure(widthSpec, heightMeasureSpec);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(changed){
			int n = getChildCount();
			View child = null;
			int childLeft = 0;
			mCurScreen = 0;
			
			for(int i = 0; i < n; i ++){
				child = getChildAt(i);
				child.layout(childLeft, 0, 
						childLeft + child.getMeasuredWidth(), 
						child.getMeasuredHeight());
				childLeft += child.getMeasuredWidth();
			}
			
			if(mViewType == VIEW_MAIN_SLIDEMENU){
				if(n > 3){
					Log.d(TAG, "error: Main SlideMenu num must <= 3");
					return;
				}
				if(getChildAt(0).getMeasuredWidth() < getMeasuredWidth()){
					mCurScreen = 1;
					scrollTo(getChildAt(0).getMeasuredWidth(), 0);
				}else{
					mCurScreen = 0;
				}
			}
			Log.d(TAG, "mCurScreen = " + mCurScreen);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			mLastX = (int) ev.getX();
			break;
			
		case MotionEvent.ACTION_MOVE:
			int x = (int) ev.getX();
			if(Math.abs(x - mLastX) > mTouchSlop){
				return true;
			}
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// TODO: clean or reset
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * 使用VelocityTracker来记录每次的event，
	 * 并在ACTION_UP时computeCurrentVelocity，
	 * 得出X,Y轴方向上的移动速率
	 * velocityX > 0 向右移动, velocityX < 0 向左移动
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mVelocityTracker == null){
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			mLastX = (int) event.getX();
			break;
			
		case MotionEvent.ACTION_MOVE:
			int deltaX = mLastX - (int)event.getX(); // delta > 0向右滚动
			mLastX = (int) event.getX();
			scrollChild(deltaX, 0);
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mVelocityTracker.computeCurrentVelocity(VELOCITY_X_DISTANCE);
			int velocityX = (int) mVelocityTracker.getXVelocity();
			animateChild(velocityX);
			if(mVelocityTracker != null){
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			break;
		}
		return true;
	}

	private void scrollChild(int distanceX, int distanceY){
		int firstChildPosX = getChildAt(0).getLeft() - getScrollX();
		int lastChildPosX = getChildAt(getChildCount()-1).getLeft() - getScrollX();
		
		if(mViewType == VIEW_MAIN_SLIDEMENU){
			lastChildPosX -= (getWidth() - getChildAt(getChildCount()-1).getWidth());
		}
		
		if(firstChildPosX != 0 && Math.abs(firstChildPosX) < Math.abs(distanceX)){
			distanceX = firstChildPosX;
		}else if(lastChildPosX != 0 && Math.abs(lastChildPosX) < Math.abs(distanceX)){
			distanceX = lastChildPosX;
		}

		if(firstChildPosX == 0 && distanceX < 0){
			return;
		}else if(lastChildPosX == 0 && distanceX > 0){
			return;
		}
		scrollBy(distanceX, 0);
	}

	private void animateChild(int velocityX){
		int width = 0;
		int offset = 0;
		if(mViewType == VIEW_NAVIGATOR){
			width = getWidth();
		}else if(mViewType == VIEW_MAIN_SLIDEMENU){
			// 默认左右两页菜单宽度一致
			width = getChildAt(0).getWidth();
		}
		
		/*
		 * velocityX > 0, 向右滚动; velocityX < 0, 向左滚动
		 */
		if(velocityX > VELOCITY_X_DISTANCE && mCurScreen > 0){
			offset = (--mCurScreen) * width - getScrollX();
		}else if(velocityX < -VELOCITY_X_DISTANCE && mCurScreen < getChildCount()-1){
			offset = (++mCurScreen) * width - getScrollX();
		}else{
			mCurScreen = (getScrollX() + width/2) / width;
			offset = mCurScreen * width - getScrollX();
		}

		//Log.d(TAG, "offset = " + offset);
		mScroller.startScroll(getScrollX(), 0, offset, 0, Math.abs(offset));
		invalidate();
	}

	@Override
	public void computeScroll() {
		if(mScroller.computeScrollOffset()){
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
		super.computeScroll();
	}
}
