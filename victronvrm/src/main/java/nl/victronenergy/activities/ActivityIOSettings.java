package nl.victronenergy.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.models.BaseResponse;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.IoExtenderUtils;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.TakePictureUtils;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * <h1>IO Settings Screen</h1> This screen is about a particular IO settings device. This screen comes up when the user
 * press the settings button on "Site Summary" screen, and allows the user to change the text and picture. The user
 * should press "Save" button to save the name.
 * <p/>
 *
 * @author M2mobi
 */
public class ActivityIOSettings extends ActionBarActivity implements OnClickListener, LoaderCallbacks<RestResponse> {
	private static final String LOG_TAG = "ActivityIOSettings";

	private Site mSite = null;
	private Attribute mIoObject = null;

	private MenuItem mMenuItem;
	private ImageView mImageViewPicture;
	private String mPhotoPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_io);

		mSite = (Site) getIntent().getSerializableExtra(BUNDLE.SITE_OBJECT);
		mIoObject = (Attribute) getIntent().getSerializableExtra(Constants.INTENT_OBJECT_IO);

		mPhotoPath = IoExtenderUtils.getIOPicturePath(this, mSite.getIdSite(), mIoObject.attributeCode);

		initView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_io, menu);
		mMenuItem = menu.findItem(R.id.button_save);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Checks which action bar button is pressed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				onBackPressed();
				break;
			}
			case R.id.button_save: {
				saveOptions();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Initialize the view
	 */
	private void initView() {
		// Hide actionbar title
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		findViewById(R.id.button_io_update_image).setOnClickListener(this);
		mImageViewPicture = (ImageView) findViewById(R.id.iv_io_photo);

		// Init IO Data
		((EditText) findViewById(R.id.et_io_name)).setText(mIoObject.getLabel());

		// Set the actionbar title according to the type of input
		ActionBar actionbar = getSupportActionBar();
		if (mIoObject.attributeCode.startsWith(Constants.OUTPUT_CODE_PREFIX)) {
			actionbar.setTitle(R.string.io_header_output);
		} else if (mIoObject.attributeCode.startsWith(Constants.INPUT_CODE_PREFIX)) {
			actionbar.setTitle(R.string.io_header_input);
		} else if (mIoObject.attributeCode.equals(Constants.ATTRIBUTE.IO_TEMPERATURE)) {
			actionbar.setTitle(R.string.io_header_temperature);
		}

		IoExtenderUtils.setPic(mPhotoPath, mImageViewPicture);
	}

	/**
	 * Save the options
	 */
	private void saveOptions() {
		EditText editTextName = (EditText) findViewById(R.id.et_io_name);

		// If the name has changed try to save it
		if (!editTextName.getText().equals(mIoObject.getLabel())) {
			mMenuItem.setEnabled(false);
			callIONameLoader(editTextName.getText().toString());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case TakePictureUtils.REQUEST_TAKE_PHOTO:
					break;
				case TakePictureUtils.REQUEST_PICK_FROM_GALLERY:
					TakePictureUtils.savePictureToExternalStorage(TakePictureUtils.getBitmapFromIntent(this, intent), mPhotoPath);
					break;
				case TakePictureUtils.REQUEST_MULTI_GET_PICTURE:
					// If the intent is null a picture was taken if it's not a picture was picked from the gallery
					if (intent != null) {
						TakePictureUtils.savePictureToExternalStorage(TakePictureUtils.getBitmapFromIntent(this, intent), mPhotoPath);
					}
					break;
			}
			IoExtenderUtils.setPic(mPhotoPath, mImageViewPicture);
		} else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, getString(R.string.io_pick_picture_canceled), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.error_take_picture), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View pView) {
		TakePictureUtils.takePicture(this, mPhotoPath);
	}

	/**
	 * Call the loader to change the io name
	 *
	 * @param newIoName
	 *        The new IO Name to set
	 */
	private void callIONameLoader(String newIoName) {
		if (mSite == null || mIoObject == null) {
			return;
		}

		Bundle params = new Bundle();
		params.putString(Constants.POST.URI, Constants.SET_LABEL);
		params.putInt(Constants.POST.SITE_ID, mSite.getIdSite());
		params.putInt(Constants.POST.ATTRIBUTE_ID, mIoObject.attributeId);
		params.putString(Constants.POST.LABEL, newIoName);

		if (getSupportLoaderManager().getLoader(Constants.LOADER_ID.IO_NAME) == null) {
			getSupportLoaderManager().initLoader(Constants.LOADER_ID.IO_NAME, params, this);
		} else {
			getSupportLoaderManager().restartLoader(Constants.LOADER_ID.IO_NAME, params, this);
		}
	}

	/**
	 * Parse the IO Name response
	 *
	 * @param pRestResponse
	 *        The response returned by the IO Name webservice
	 */
	private void parseIoNameResponse(RestResponse pRestResponse) {
		BaseResponse baseResponse = JsonParserHelper.getInstance().parseJson(pRestResponse, BaseResponse.class);
		if (baseResponse != null && baseResponse.status.code == Constants.RESPONSE_CODE.RESPONSE_OK) {
			Toast.makeText(ActivityIOSettings.this, getString(R.string.io_success_name), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(ActivityIOSettings.this, getString(R.string.io_error_name), Toast.LENGTH_LONG).show();
		}
		mMenuItem.setEnabled(true);
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int arg0, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(ActivityIOSettings.this);
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		if (loader.getId() == Constants.LOADER_ID.IO_NAME) {
			parseIoNameResponse(response);
		} else {
			MyLog.e(LOG_TAG, "Unknown loader finished: " + loader.getId());
		}
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> loader) {
		// Do nothing
	}
}
