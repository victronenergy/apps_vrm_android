package nl.victronenergy.adapters;

import java.util.ArrayList;

import nl.victronenergy.R;
import nl.victronenergy.fragments.FragmentSiteSummary;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.models.widgets.SummaryWidget;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Viewpager that shows the widgets of a site<br/>
 * Created by M2Mobi on 1-5-2014.
 */
public class WidgetViewPagerAdapter extends PagerAdapter {

	/** Log tag used for logging in this class */
	private static final String LOG_TAG = "WidgetViewPagerAdapter";

	/** Amount of widgets visible per page */
	private static final int WIDGET_PAGE_COUNT = 3;

	/** Indices of the 3 widgets shown per page */
	private static final int WIDGET_INDEX_1 = 0;
	private static final int WIDGET_INDEX_2 = 1;
	private static final int WIDGET_INDEX_3 = 2;

	/** Context used to inflate the widget layout */
	private final Context mContext;

	/** The id of the site this viewpager of widgets belongs to */
	private final int mSiteId;

	/** Reference to the site summary fragment, used to pass the click event to */
	private final FragmentSiteSummary mFragmentSiteSummary;

	/** ArrayList of widgets for this site */
	private SummaryWidget[] mWidgets;

	public WidgetViewPagerAdapter(FragmentSiteSummary pFragmentSiteSummary, ArrayList<SummaryWidget> pWidgets, int pSiteId) {
		mFragmentSiteSummary = pFragmentSiteSummary;
		mContext = mFragmentSiteSummary.getActivity();
		mWidgets = new SummaryWidget[pWidgets.size()];
		mWidgets = pWidgets.toArray(mWidgets);
		mSiteId = pSiteId;
	}

	@Override
	public int getCount() {
		return (mWidgets.length + WIDGET_PAGE_COUNT - 1) / WIDGET_PAGE_COUNT;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int pPageIndex) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.layout_site_summary_widgets, container, false);
		view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mFragmentSiteSummary.onWidgetOfSiteItemClicked(mSiteId);
			}
		});

		/**
		 * On every page there are WIDGET_PAGE_COUNT amount of widgets. By multiplying the page index with the amount of
		 * widgets we know the index of the first widget we should show on the current page.<br/>
		 * By adding the WIDGET_INDEX we know the indices of the other widgets we should show.<br/>
		 */
		int firstWidgetIndexForPage = pPageIndex * WIDGET_PAGE_COUNT;
		initWidget(view, firstWidgetIndexForPage + WIDGET_INDEX_1, WIDGET_INDEX_1);
		initWidget(view, firstWidgetIndexForPage + WIDGET_INDEX_2, WIDGET_INDEX_2);
		initWidget(view, firstWidgetIndexForPage + WIDGET_INDEX_3, WIDGET_INDEX_3);

		container.addView(view, 0);
		return view;
	}

	/**
	 * Initialize the view for the widget
	 *
	 * @param pView
	 *        The view that contains all 3 widgets
	 * @param pWidgetPosition
	 *        The position of the current widget
	 * @param pWidgetIndexOnPage
	 *        The index of the widget on the current page (1 to 3)
	 */
	private void initWidget(View pView, int pWidgetPosition, int pWidgetIndexOnPage) {
		if (pWidgetPosition < mWidgets.length) {
			View viewWidget;
			switch (pWidgetIndexOnPage) {
				case WIDGET_INDEX_1:
					viewWidget = pView.findViewById(R.id.widget_1);
					break;
				case WIDGET_INDEX_2:
					viewWidget = pView.findViewById(R.id.widget_2);
					break;
				case WIDGET_INDEX_3:
					viewWidget = pView.findViewById(R.id.widget_3);
					break;
				default:
					MyLog.e(LOG_TAG, "Unavailable widget index");
					return;
			}

			((TextView) viewWidget.findViewById(R.id.textview_widget_title)).setText(mWidgets[pWidgetPosition].getTitle());
			((TextView) viewWidget.findViewById(R.id.textview_widget_value)).setText(mWidgets[pWidgetPosition].getText());
			((TextView) viewWidget.findViewById(R.id.textview_widget_value)).setCompoundDrawablesWithIntrinsicBounds(
					mWidgets[pWidgetPosition].getIcon(), 0, 0, 0);

			viewWidget.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
}
