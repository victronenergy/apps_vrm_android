package nl.victronenergy.views;

import nl.victronenergy.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Custom TextView component, that expands the existing one by adding: - fontFace attribute for easy setting of font by
 * xml - fakeFocused attribute for easy force-marqueeing
 *
 * @author piet
 */
public class BetterTextView extends TextView {

	/* boolean indicating whether this component should always pretend to be focused or not */
	private boolean fakeFocused;

	public BetterTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		customize(context, attrs);
	}

	public BetterTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		customize(context, attrs);
	}

	public BetterTextView(Context context) {
		super(context);
	}

	/**
	 * Processing of custom styling attributes is done here
	 *
	 * @param ctx
	 * @param attribs
	 */
	private void customize(Context ctx, AttributeSet attribs) {

		// big if clause to prevent component from not being rendered in Eclipse graphical layout editor
		if (!isInEditMode()) {

			TypedArray a = ctx.obtainStyledAttributes(attribs, R.styleable.BetterTextView);
			final int count = a.getIndexCount();
			for (int i = 0; i < count; ++i) {

				int attr = a.getIndex(i);

				switch (attr) {
					case R.styleable.BetterTextView_fontFace:
						String fontFace = a.getString(attr);
						if (fontFace.length() > 0) {
							Typeface typeFace = FontLoader.getInstance().getFont(fontFace, ctx);
							setTypeface(typeFace);
						}
						break;
					case R.styleable.BetterTextView_fakeFocused:
						fakeFocused = a.getBoolean(attr, false);
						requestFocus();	// not sure if needed...
						break;
				}
			}
			a.recycle();
		}
	}

	/** Method overridden to enable focus-spoofing */
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		if (fakeFocused) {
			super.onFocusChanged(true, direction, previouslyFocusedRect);
		} else {
			super.onFocusChanged(focused, direction, previouslyFocusedRect);
		}
	}

	/** Method overridden to enable focus-spoofing */
	@Override
	public void onWindowFocusChanged(boolean focused) {
		if (fakeFocused) {
			super.onWindowFocusChanged(true);
		} else {
			super.onWindowFocusChanged(focused);
		}
	}

	/** Method overridden to enable focus-spoofing */
	@Override
	public boolean isFocused() {
		if (fakeFocused) {
			return true;
		} else {
			return super.isFocused();
		}
	}
}
