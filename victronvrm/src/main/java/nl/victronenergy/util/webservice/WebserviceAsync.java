package nl.victronenergy.util.webservice;

import java.net.URI;
import javax.net.ssl.SSLPeerUnverifiedException;
import nl.victronenergy.BuildConfig;
import nl.victronenergy.R;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.UserUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Loader that performs a webservice call async and will return a RestResponse
 *
 * @author M2Mobi
 */
public class WebserviceAsync extends AsyncTaskLoader<RestResponse> {
	private final static String LOG_TAG = "WebserviceAsync";
	private String mURI;
	private Bundle mParams;
	private final Context mContext;
	private String message = null;

	private WebserviceAsync(Context pContext) {
		super(pContext);

		mContext = pContext;
	}

	public static WebserviceAsync newInstance(Context pContext) {
		MyLog.i(LOG_TAG, "Creating new webservice instance");
		WebserviceAsync loader = new WebserviceAsync(pContext);
		return loader;
	}

	/**
	 * Set arguments to be passed to the webservice
	 *
	 * @param args
	 *        Bundle containing arguments to pass to the webservice
	 */
	public void setParams(Bundle args) {
		// Get the URI from the args
		mURI = args.get(POST.URI).toString();

		MyLog.d(LOG_TAG, "URI: " + mURI);

		// Add api version to the parameters
		args.putString(POST.APIVERSION, Constants.API_VERSION);
		args.putString(POST.SESSION_ID, UserUtils.getSessionID(mContext));
		args.putString(POST.VERIFICATION_TOKEN, Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID));
		mParams = args;
	}

	@Override
	public RestResponse loadInBackground() {

		RestResponse restResponse = null;
		Throwable throwable = null;

		try {
			// At the very least we always need an action.
			if (TextUtils.isEmpty(mURI)) {
				MyLog.e(LOG_TAG, "You did not define an URI. REST call canceled.");
				return new RestResponse();
			}

			HttpPost request = new HttpPost();
			request.setURI(new URI(mURI));

			// Convert the params to a list of params
			request.setEntity(paramsToEntity(mParams));
			// UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(mParams));
			// builder.addformEntity.request.setEntity(formEntity);

			// Setup the connection parameters
			HttpParams httpParameters = new BasicHttpParams();
			ConnManagerParams.setTimeout(httpParameters, Constants.TIMEOUT);
			HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParameters, Constants.TIMEOUT);
			HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(httpParameters, HTTP.UTF_8);

			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpResponse response = httpClient.execute(request);
			MyLog.d(LOG_TAG, "Executing request: " + mURI.toString());

			HttpEntity responseEntity = response.getEntity();
			StatusLine responseStatus = response.getStatusLine();

			// Set the status code
			int statusCode = 0;
			if (responseStatus != null) {
				statusCode = responseStatus.getStatusCode();
			}

			// Set the response data
			String responseData = null;
			if (responseEntity != null) {
				responseData = EntityUtils.toString(responseEntity);
			}

			// If this is a debug build we print the formatted JSON to logcat
			if (BuildConfig.DEBUG) {
				if (responseData != null) {
					try {
						JSONObject jObject = new JSONObject(responseData);
						//MyLog.d(LOG_TAG, "ResponseData: " + jObject.toString(4));
					} catch (Exception e) {
						MyLog.e(LOG_TAG, "Exception in creating JSONObject, raw data :" + responseData);
					}
				}
			}

			// Create a new restreponse with the reponsedata and statuscode
			restResponse = new RestResponse(responseData, statusCode);

		} catch (SSLPeerUnverifiedException e) {
			// If we get an SLL exception this is probably caused by a wrong date setting on the phone
			throwable = e;
			message = mContext.getString(R.string.date_inaccurate);
		} catch (Throwable t) {
			throwable = t;
		} finally {
			if (throwable != null) {
				MyLog.e(LOG_TAG, throwable.toString());
				restResponse = new RestResponse();
			}
		}

		return restResponse;
	}

	@Override
	public void deliverResult(RestResponse data) {
		MyLog.i(LOG_TAG, "Deliver result");
		if (message != null) {
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		}
		super.deliverResult(data);
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	@Override
	protected void onStopLoading() {
		// This prevents the AsyncTask backing this
		// loader from completing if it is currently running.
		cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Stop the Loader if it is currently running.
		onStopLoading();
	}

	/**
	 * Creates a list of post parameters from the provided arguments
	 *
	 * @param pParams
	 *        The arguments provided to this loader
	 * @return A list of post parameters
	 */
	private static HttpEntity paramsToEntity(Bundle pParams) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

		for (String key : pParams.keySet()) {
			Object value = pParams.get(key);

			// We can only put Strings in a form entity, so we call the toString()
			// method to enforce. We also probably don't need to check for null here
			// but we do anyway because Bundle.get() can return null.
			// Also filter the URI we don't want to put that as a post parameter
			if (key != POST.URI && value != null) {
				if (key == POST.IMAGE) {
					multipartEntityBuilder.addBinaryBody(key, (byte[]) value, ContentType.create("image/jpeg"), "file");
				} else {
					multipartEntityBuilder.addTextBody(key, value.toString());
				}
			}
		}

		return multipartEntityBuilder.build();
	}
}
