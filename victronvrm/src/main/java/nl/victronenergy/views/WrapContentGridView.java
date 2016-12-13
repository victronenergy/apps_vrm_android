package nl.victronenergy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Custom grid view that overrides the wrap_content functionality and now actually wraps the content<br/>
 *
 * @see <a
 *      href="http://www.jayway.com/2012/10/04/how-to-make-the-height-of-a-gridview-wrap-its-content/">http://www.jayway.com/2012/10/04/how-to-make-the-height-of-a-gridview-wrap-its-content/</a>
 *      User: M2Mobi<br/>
 *      Date: 12-5-2014<br/>
 *      Time: 13:40<br/>
 */
public class WrapContentGridView extends GridView {

	public WrapContentGridView(Context context) {
		super(context);
	}

	public WrapContentGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WrapContentGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightSpec;

		if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
			// The great Android "hackatlon", the love, the magic.
			// The two leftmost bits in the height measure spec have
			// a special meaning, hence we can't use them to describe height.
			heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		} else {
			// Any other height should be respected as is.
			heightSpec = heightMeasureSpec;
		}

		super.onMeasure(widthMeasureSpec, heightSpec);
	}
}
