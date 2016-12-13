package nl.victronenergy.models.widgets;

import java.io.Serializable;

import nl.victronenergy.models.AttributeData;
import android.content.Context;

/**
 * Created by M2Mobi on 25-2-14.
 */
public abstract class SummaryWidget implements Serializable {
	/**
	 * Generated serial version Id
	 */
	private static final long serialVersionUID = 8220890193598216086L;

	public int mIcon;
	public String mText;
	public String mTitle;

	public void initValues(Context pContext, AttributeData pAttributeData) {
		initIcon(pContext, pAttributeData);
		initText(pContext, pAttributeData);
		initTitle(pContext, pAttributeData);
	}

	protected abstract void initIcon(Context pContext, AttributeData pAttributeData);

	protected abstract void initText(Context pContext, AttributeData pAttributeData);

	protected abstract void initTitle(Context pContext, AttributeData pAttributeData);

	/**
	 * Checks if all values that are required for this widget to show are available
	 *
	 * @param pAttributeData
	 *        The attributes that are available
	 * @return True if all required attributes are available, false if not
	 */
	public abstract boolean areRequiredAttributesAvailable(AttributeData pAttributeData);

	/**
	 * Returns the icon that should be used for this widget
	 *
	 * @return The icon drawable
	 */
	public int getIcon() {
		return mIcon;
	}

	/**
	 * The text that should be displayed
	 *
	 * @return The text that should be displayed in the widget
	 */
	public String getText() {
		return mText;
	}

	/**
	 * @return The text that should be displayed in the widget
	 */
	public String getTitle() {
		return mTitle;
	}

}
