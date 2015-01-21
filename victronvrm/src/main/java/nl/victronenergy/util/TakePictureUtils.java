/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import nl.victronenergy.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * <p/>
 * User: Victron Energy<br/>
 * Date: 13-5-2014<br/>
 * Time: 9:46<br/>
 */
public class TakePictureUtils {

	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_PICK_FROM_GALLERY = 2;
	public static final int REQUEST_MULTI_GET_PICTURE = 0;

	/** Tag used for logging in this class */
	private static final String LOG_TAG = "TakePictureUtils";

	/**
	 * Creates a new image file that will be used by the camera intent
	 *
	 * @param pPicturePath
	 *        The path where the picture should be stored
	 * @return The image file that has been created or null if it's not possible to create a file
	 */
	private static File createImageFile(String pPicturePath) {
		// Create an image file name
		File image = new File(pPicturePath);
		try {
			image.createNewFile();
		} catch (IOException e) {
			MyLog.e(LOG_TAG, "Unable to create image file", e);
		}

		return image;
	}

	/**
	 * Indicates whether the specified action can be used as an intent example: isIntentAvailable(this,
	 * "com.google.zxing.client.android.SCAN");
	 *
	 * @param pContext
	 * @param pAction
	 * @return
	 */
	private static boolean isIntentAvailable(Context pContext, String pAction) {
		final PackageManager packageManager = pContext.getPackageManager();
		final Intent intent = new Intent(pAction);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	/**
	 * Tries to take a picture from a fragment
	 *
	 * @param pActivity
	 *        The activity the picture is taken in
	 * @param pPicturePath
	 *        The path where the picture should be stored
	 * @return Returns a boolean indicating that the user is able to take a picture or not
	 */
	public static boolean takePicture(Activity pActivity, String pPicturePath) {
		// First check if it's possible to store a picture on the device
		File photoFile = createImageFile(pPicturePath);
		if (photoFile == null) {
			Toast.makeText(pActivity, pActivity.getString(R.string.error_take_picture), Toast.LENGTH_SHORT).show();
			return false;
		}

		// Setup take picture intent
		Intent intentTakePicture = null;
		if (pActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
				&& isIntentAvailable(pActivity, MediaStore.ACTION_IMAGE_CAPTURE)) {
			intentTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intentTakePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		}

		// Setup pick picture intent
		Intent intentPickFromGallery = null;
		if (isIntentAvailable(pActivity, Intent.ACTION_PICK)) {
			intentPickFromGallery = new Intent(Intent.ACTION_PICK);
			intentPickFromGallery.setType("image/*");
		}

		// Create the intent according to what is available on the phone
		if (intentTakePicture != null && intentPickFromGallery != null) {
			// Create a multi intent
			Intent getPictureIntent = Intent.createChooser(intentTakePicture, pActivity.getString(R.string.io_get_picture));
			getPictureIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intentPickFromGallery });
			pActivity.startActivityForResult(getPictureIntent, REQUEST_MULTI_GET_PICTURE);
		} else if (intentTakePicture != null) {
			// Take picture
			pActivity.startActivityForResult(intentTakePicture, REQUEST_TAKE_PHOTO);
		} else if (intentPickFromGallery != null) {
			// Pick picture
			pActivity.startActivityForResult(intentPickFromGallery, REQUEST_PICK_FROM_GALLERY);
		} else {
			Toast.makeText(pActivity, pActivity.getString(R.string.error_take_picture), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * Returns a bitmap from a gallery Uri
	 *
	 * @param pContext
	 *        Context required to access the content resolver
	 * @param pIntent
	 *        The Uri of the picker image
	 * @return The picked image as a bitmap
	 */
	public static Bitmap getBitmapFromIntent(Context pContext, Intent pIntent) {
		Bitmap bitmapPickedImage = null;

		Uri pickedImageUri = pIntent.getData();

		// If the URI is not null try to decode it to a bitmap else try to get the bitmap data from the intent
		// http://stackoverflow.com/questions/17123083/null-pointer-exception-while-taking-pictures-from-camera-android-htc
		if (pickedImageUri != null) {
			try {
				InputStream imageStream = pContext.getContentResolver().openInputStream(pickedImageUri);
				bitmapPickedImage = BitmapFactory.decodeStream(imageStream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			if (pIntent.getExtras() != null && pIntent.getExtras().get("data") instanceof Bitmap) {
				bitmapPickedImage = (Bitmap) pIntent.getExtras().get("data");
			}
		}

		return bitmapPickedImage;
	}

	/**
	 * Tries to save the picked picture to external storage, this makes it possible to use the same function for
	 * retrieving the picture
	 *
	 * @param pBitmap
	 *        The Bitmap we want to save to external storage
	 * @param pPhotoPath
	 *        The path the photo should be saved to
	 */
	public static void savePictureToExternalStorage(Bitmap pBitmap, String pPhotoPath) {
		if (pBitmap == null) {
			return;
		}

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(pPhotoPath);
			pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Throwable ignore) {
				// We don't have to do anything
			}
		}
	}

	/**
	 * Checks if the orientation of the image is correct, if it's not it will rotate the image
	 *
	 * @param pBitmap
	 *        The bitmap to check the orientation for
	 * @param pPath
	 *        The path to the image, used to load the exif interface
	 * @return Bitmap in the correct orientation
	 */
	public static Bitmap imageOreintationValidator(Bitmap pBitmap, String pPath) {
		ExifInterface ei;
		try {
			ei = new ExifInterface(pPath);
			int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					pBitmap = rotateImage(pBitmap, 90);
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					pBitmap = rotateImage(pBitmap, 180);
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					pBitmap = rotateImage(pBitmap, 270);
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return pBitmap;
	}

	/**
	 * Rotates a bitmap by a certain angle
	 *
	 * @param pSourceBitmap
	 *        The bitmap that needs to be rotated
	 * @param pAngle
	 *        The angle the bitmap should be rotated to
	 * @return A rotated bitmap
	 */
	private static Bitmap rotateImage(Bitmap pSourceBitmap, float pAngle) {
		Bitmap bitmap = null;
		Matrix matrix = new Matrix();
		matrix.postRotate(pAngle);
		try {
			bitmap = Bitmap.createBitmap(pSourceBitmap, 0, 0, pSourceBitmap.getWidth(), pSourceBitmap.getHeight(), matrix, true);
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
		}
		return bitmap;
	}
}
