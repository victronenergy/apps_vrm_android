package nl.victronenergy.views;

import java.util.HashMap;

import nl.victronenergy.util.MyLog;
import android.content.Context;
import android.graphics.Typeface;

/**
 * Makes sure to load font only once to enhance performance on screens with a lot of TextViews/Buttons/EditTexts
 *
 * @author Lorenz
 */
public class FontLoader {

	/** base path in assets folder where fonts are located */
	private final static String FONT_BASE_PATH = "fonts/";

	/** HashMap to store the typefaces at runtime */
	HashMap<String, Typeface> typeFaces;

	public static FontLoader instance;

	protected FontLoader() {
		typeFaces = new HashMap<String, Typeface>();
	}

	/**
	 * Singleton Constructor.
	 *
	 * @return
	 */
	public static FontLoader getInstance() {
		if (instance == null) {
			instance = new FontLoader();
		}
		return instance;
	}

	/**
	 * Returns the typeface object with the desired font.
	 *
	 * @param fontName
	 *        the name of the font
	 * @param context
	 *        application context
	 * @return
	 */
	public Typeface getFont(String fontName, Context context) {
		// try to find typeface in hashmap
		Typeface typeFace = typeFaces.get(fontName);
		if (typeFace != null) {
			return typeFace;
		} else {
			// load from assets if null
			try {
				typeFace = Typeface.createFromAsset(context.getAssets(), FONT_BASE_PATH + fontName);
				typeFaces.put(fontName, typeFace);
			} catch (Exception e) {
				MyLog.e(getClass().getName(), "Error trying to create Typeface for font: " + FONT_BASE_PATH + fontName);
				e.printStackTrace();
			}
		}

		// return default typeface if null
		if (typeFace == null) {
			return Typeface.DEFAULT;
		} else {
			return typeFace;
		}
	}
}
