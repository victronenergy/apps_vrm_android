package nl.victronenergy.views;

import nl.victronenergy.util.MyLog;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Custom viewpager that prevents a crash when zooming in the photoview.<br/>
 * http://stackoverflow.com/questions/18383083/arrayindexoutofboundsexception-in-photoview-viewpager<br/>
 * <p/>
 * Created by Victron Energy<br/>
 * 6/17/ 2014<br/>
 * 13:23<br/>
 */
public class CustomViewPager extends ViewPager {

	/** Tag used for logging in this class */
	private static final String LOG_TAG = "CustomViewPager";

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Wrapped in try catch to prevent crash
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			MyLog.e(LOG_TAG, "Intercept touch event exception", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			MyLog.e(LOG_TAG, "ArrayIndexOutOfBoundsException", e);
		}
		return false;
	}
}
