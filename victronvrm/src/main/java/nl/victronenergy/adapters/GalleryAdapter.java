/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.adapters;

import nl.victronenergy.R;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

/**
 * Used to show a grid of images in a grid
 * <p/>
 * User: Victron Energy<br/>
 * Date: 12-5-2014<br/>
 * Time: 11:34<br/>
 */
public class GalleryAdapter extends BaseAdapter {

	/** Tag used for logging in this class */
	private static final String LOG_TAG = "GalleryAdapter";

	/** Prefix for the thumbnail images */
	private static final String THUMB_PREFIX = "/thumb_";

	/** Options used to load the images */
	private final DisplayImageOptions mDisplayImageOptions;

	/** Site object that contains the required data for the gallery */
	private Site mSite;

	public GalleryAdapter(final Site pSite) {
		mSite = pSite;

		mDisplayImageOptions = new DisplayImageOptions.Builder() // --
				.showImageOnFail(R.drawable.warning) // --
				.cacheOnDisc(true) // --
				.considerExifParams(true) // --
				.bitmapConfig(Bitmap.Config.RGB_565) // --
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT) // --
				.build(); // --
	}

	@Override
	public int getCount() {
		int galleryImageCount = 0;
		if (mSite.getImages() != null) {
			galleryImageCount = mSite.getImages().size();
		}

		// If a user can edit add an extra item to the grid that is used to add a new picture
		if (mSite.canEdit()) {
			galleryImageCount += 1;
		}
		return galleryImageCount;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_gallery, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.imageview_grid_picture);
			viewHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progressbar_grid_loading);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (position < mSite.getImages().size()) {
			// Load the image from the web
			ImageLoader.getInstance().displayImage(Constants.GET_GALLERY_IMAGE + mSite.getIdSite() + THUMB_PREFIX + mSite.getImages().get(position),
					viewHolder.mImageView, mDisplayImageOptions, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							viewHolder.mProgressBar.setProgress(0);
							viewHolder.mProgressBar.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
							viewHolder.mProgressBar.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							viewHolder.mProgressBar.setVisibility(View.GONE);
						}
					});
		} else {
			// Show the add icon
			viewHolder.mProgressBar.setVisibility(View.GONE);
			viewHolder.mImageView.setImageResource(R.drawable.ic_gallery_add_picture);
		}

		return convertView;
	}

	/**
	 * Basic viewholder class
	 */
	private final class ViewHolder {
		private ImageView mImageView;
		private ProgressBar mProgressBar;
	}

	/**
	 * Set image urls
	 *
	 * @param pSite
	 *        New array of urls to images
	 */
	public void setSite(Site pSite) {
		mSite = pSite;
		notifyDataSetChanged();
	}
}
