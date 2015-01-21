package nl.victronenergy.views;

import nl.victronenergy.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Custom Button (Remix of Piet's TextView) - fontFace attribute for easy setting of font via xml
 * 
 * @author lorenz *
 */
public class BetterButton extends Button {

	public BetterButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		customize(context, attrs);
	}

	public BetterButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		customize(context, attrs);
	}

	public BetterButton(Context context) {
		super(context);
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

			TypedArray a = ctx.obtainStyledAttributes(attribs, R.styleable.BetterButton);
			final int count = a.getIndexCount();

			for (int i = 0; i < count; ++i) {

				int attr = a.getIndex(i);

				switch (attr) {
					case R.styleable.BetterButton_fontFace:
						String fontFace = a.getString(attr);
						if (fontFace.length() > 0) {
							Typeface typeFace = FontLoader.getInstance().getFont(fontFace, ctx);
							setTypeface(typeFace);
						}
						break;
				}
				a.recycle();
			}
		}
	}
}
