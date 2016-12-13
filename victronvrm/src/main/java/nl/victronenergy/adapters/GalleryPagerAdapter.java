package nl.victronenergy.adapters;

import android.view.View.OnClickListener;
import nl.victronenergy.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import nl.victronenergy.activities.ActivityGallery;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.MyLog;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;

/**
 * Gallery pager adapter takes care of loading the mImageUrls in the viewpager gallery
 * <p/>
 * User: M2Mobi<br/>
 * Date: 12-5-2014<br/>
 * Time: 15:35<br/>
 */
public class GalleryPagerAdapter extends PagerAdapter {

	/** Tag used for logging in this class */
	private static final String LOG_TAG = "GalleryPagerAdapter";

	/** Options for downloading the images */
	private DisplayImageOptions mDisplayOptions;

	/** Id of the site we want to show the images for */
	private Site mSite;

	/** Layout inflater required to inflate the layout of the pages */
	private LayoutInflater mLayoutInflater;

	/** Reference to the gallery activity, used to pass the click event */
	private ActivityGallery mGalleryActivity;

	public GalleryPagerAdapter(ActivityGallery pGalleryActivity, Site pSite) {
		mGalleryActivity = pGalleryActivity;
		mSite = pSite;
		mLayoutInflater = LayoutInflater.from(pGalleryActivity);

		mDisplayOptions = new DisplayImageOptions.Builder() // --
				.showImageOnFail(R.drawable.ic_alert_red)// --
				.resetViewBeforeLoading(true)// --
				.cacheOnDisc(true)// --
				.imageScaleType(ImageScaleType.EXACTLY)// --
				.bitmapConfig(Bitmap.Config.RGB_565)// --
				.considerExifParams(true)// --
				.displayer(new FadeInBitmapDisplayer(300))// --
				.build();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		if (mSite.getImages() != null) {
			return mSite.getImages().size();
		}
		return 0;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(ViewGroup view, int position) {
		View imageLayout = mLayoutInflater.inflate(R.layout.pager_item_gallery, view, false);
		PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.imageview_gallery_pager);
		imageView.setOnViewTapListener(new OnViewTapListener() {
			@Override
			public void onViewTap(View view, float v, float v2) {
				mGalleryActivity.onClick(view);
			}
		});
		final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.progressbar_gallery_pager);

		ImageLoader.getInstance().displayImage(Constants.GET_GALLERY_IMAGE + mSite.getIdSite() + "/" + mSite.getImages().get(position), imageView,
				mDisplayOptions, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						spinner.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						spinner.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						spinner.setVisibility(View.GONE);
					}
				});

		view.addView(imageLayout, 0);
		return imageLayout;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	/**
	 * Removes an image from the viewpager
	 *
	 * @param pSite
	 *        The new site data
	 */
	public void setSite(Site pSite) {
		mSite = pSite;
		notifyDataSetChanged();
	}
}
