package nl.victronenergy.views;

import nl.victronenergy.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Custom EditText (Piet's TextView Remix) - fontFace attribute for easy setting of font via xml
 *
 * @author lorenz *
 */
public class BetterEditText extends EditText {

	public BetterEditText(Context context) {
		super(context);
	}

	public BetterEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		customize(context, attrs);
	}

	public BetterEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		customize(context, attrs);
	}

	/**
	 * Processing of custom styling attributes is done here
	 *
	 * @param ctx
	 * @param attribs
	 */
	private void customize(Context ctx, AttributeSet attribs) {

		// big if clause to prevent component from not being rendered in graphical layout editor
		if (!isInEditMode()) {

			TypedArray a = ctx.obtainStyledAttributes(attribs, R.styleable.BetterEditText);
			final int count = a.getIndexCount();

			for (int i = 0; i < count; ++i) {

				int attr = a.getIndex(i);

				switch (attr) {
					case R.styleable.BetterEditText_fontFace:
						String fontFace = a.getString(attr);
						if (fontFace.length() > 0) {
							Typeface typeFace = FontLoader.getInstance().getFont(fontFace, ctx);
							setTypeface(typeFace);
						}
						break;
				}
			}
			a.recycle();
		}
	}
}
