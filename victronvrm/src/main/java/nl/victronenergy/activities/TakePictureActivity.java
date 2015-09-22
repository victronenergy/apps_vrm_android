/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.activities;

import java.io.ByteArrayOutputStream;

import nl.victronenergy.R;
import nl.victronenergy.models.BaseResponse;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.IO_PICTURE;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.TakePictureUtils;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

/**
 * Activity that lets a user take a picture and uploads it to the victron server
 * <p/>
 * User: Victron Energy<br/>
 * Date: 16-5-2014<br/>
 * Time: 10:58<br/>
 */
public class TakePictureActivity extends FragmentActivity implements LoaderCallbacks<RestResponse> {

	/** Tag used for logging */
	private static final String LOG_TAG = "TakePictureActivity";

	/** Key to pass boolean around that indicates that a picture should be taken */
	private static final String KEY_TAKE_PICTURE_STARTED = "KEY_TAKE_PICTURE_STARTED";

	/** Location where the picture can be stored temporarily */
	private static final String GALLERY_PREFIX = "/gallery_";

	/** Boolean that indicates if the take picture intent has already started or not */
	private boolean mIsTakePictureStarted;

	/** Path to the current selected photo */
	private String mPhotoPath;

	/** The id of the site this picture will be taken for */
	private int mSiteId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);

		mSiteId = getIntent().getIntExtra(Constants.INTENT_SITE_ID, -1);
		mPhotoPath = getExternalFilesDir(null).getAbsolutePath() + GALLERY_PREFIX + IO_PICTURE.EXTENSION;

		if (savedInstanceState != null) {
			mIsTakePictureStarted = savedInstanceState.getBoolean(KEY_TAKE_PICTURE_STARTED);
		} else {
			if (mSiteId != -1) {
				mIsTakePictureStarted = true;
				if (!TakePictureUtils.takePicture(this, mPhotoPath)) {
					finish();
				}
			} else {
				MyLog.e(LOG_TAG, "Can't take picture for unknown site id");
				finish();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(KEY_TAKE_PICTURE_STARTED, mIsTakePictureStarted);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case TakePictureUtils.REQUEST_TAKE_PHOTO:
					callImageUploadLoader(decodeFile(mPhotoPath));
					break;
				case TakePictureUtils.REQUEST_PICK_FROM_GALLERY:
					callImageUploadLoader(TakePictureUtils.getBitmapFromIntent(this, intent));
					break;
				case TakePictureUtils.REQUEST_MULTI_GET_PICTURE:
					// If the intent is null a picture was taken if it's not, a picture was picked from the gallery
					if (intent != null) {
						callImageUploadLoader(TakePictureUtils.getBitmapFromIntent(this, intent));
					} else {
						callImageUploadLoader(decodeFile(mPhotoPath));
					}
					break;
			}
		} else if (resultCode == RESULT_CANCELED) {
			finish();
		} else {
			Toast.makeText(this, getString(R.string.error_take_picture), Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * The method decodes the image file to avoid out of memory issues. Sets the selected image in to the ImageView.
	 *
	 * @param pFilePath
	 *        The path where the file is saved
	 */
	public Bitmap decodeFile(String pFilePath) {
		// Decode image size
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pFilePath, bitmapOptions);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;

		// Find the correct scale value. It should be the power of 2.
		int widthTemp = bitmapOptions.outWidth;
		int heightTemp = bitmapOptions.outHeight;
		int scale = 1;
		while (true) {
			if (widthTemp < REQUIRED_SIZE && heightTemp < REQUIRED_SIZE) {
				break;
			}
			widthTemp /= 2;
			heightTemp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options bitmapOptions2 = new BitmapFactory.Options();
		bitmapOptions2.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(pFilePath, bitmapOptions2);

		// Rotate the bitmap if required
		bitmap = TakePictureUtils.imageOreintationValidator(bitmap, pFilePath);

		return bitmap;
	}

	/**
	 * Calls the image upload webservice and uploads an image to a certain site
	 *
	 * @param pBitmap
	 *        The bitmap that needs to be uploaded
	 */
	private void callImageUploadLoader(Bitmap pBitmap) {
		if (pBitmap == null) {
			Toast.makeText(this, R.string.error_take_picture, Toast.LENGTH_SHORT);
			finish();
		}

		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.UPLOAD_IMAGE);
		params.putInt(POST.SITE_ID, mSiteId);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pBitmap.compress(CompressFormat.JPEG, 90, bos);
		byte[] data = bos.toByteArray();
		params.putByteArray(POST.IMAGE, data);

		if (getSupportLoaderManager().getLoader(LOADER_ID.UPLOAD_IMAGE) == null) {
			getSupportLoaderManager().initLoader(LOADER_ID.UPLOAD_IMAGE, params, this);
		} else {
			getSupportLoaderManager().restartLoader(LOADER_ID.UPLOAD_IMAGE, params, this);
		}
	}

	/**
	 * Parse the upload picture response
	 *
	 * @param pRestResponse
	 *        The response returned by the upload picture webservice
	 */
	private void parseUploadPictureResponse(RestResponse pRestResponse) {
		BaseResponse baseResponse = JsonParserHelper.getInstance().parseJson(pRestResponse, BaseResponse.class);
		if (baseResponse == null || baseResponse.status == null || baseResponse.status.code != RESPONSE_CODE.RESPONSE_OK) {
			Toast.makeText(this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int arg0, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(this);
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		if (loader.getId() == LOADER_ID.UPLOAD_IMAGE) {
			parseUploadPictureResponse(response);
		} else {
			MyLog.e(LOG_TAG, "Unknown loader finished: " + loader.getId());
		}
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> loader) {
		// Do nothing
	}
}
