package nl.victronenergy.fragments;

import nl.victronenergy.models.UserResponse;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.UserUtils;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

/**
 * Fragment that has relogin build in, to prevent duplicate code
 * <p/>
 * User: M2Mobi<br/>
 * Date: 15-5-2014<br/>
 * Time: 13:48<br/>
 */
public abstract class VictronVRMFragment extends Fragment {

	/**
	 * Call the loader to try login
	 */
	protected void callLoginLoader() {
		String username = UserUtils.getUsername(getActivity());
		String password = UserUtils.getPassword(getActivity());

		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.WEBSERVICE_USER_LOGIN);
		params.putString(POST.EMAIL, username);
		params.putString(POST.PASSWORD, password);

		if (getActivity().getSupportLoaderManager().getLoader(LOADER_ID.LOGIN) == null) {
			getActivity().getSupportLoaderManager().initLoader(LOADER_ID.LOGIN, params, loginLoaderCallback);
		} else {
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID.LOGIN, params, loginLoaderCallback);
		}
	}

	/**
	 * Parse the login response, used when the session got timed out
	 *
	 * @param pRestResponse
	 *        The response returned by the login webservice
	 */
	private void parseLoginResponse(RestResponse pRestResponse) {
		UserResponse userResponse = JsonParserHelper.getInstance().parseJson(pRestResponse, UserResponse.class);
		if (userResponse != null) {
			if (userResponse.status.code == RESPONSE_CODE.RESPONSE_OK) {
				UserUtils.saveSessionID(getActivity(), userResponse.data.user.sessionId);
				onReloginSuccessful();
			}
		} else {
			onReloginFailed();
		}
	}

	/** Used to handle relogin callbacks */
	private LoaderCallbacks<RestResponse> loginLoaderCallback = new LoaderCallbacks<RestResponse>() {

		@Override
		public Loader<RestResponse> onCreateLoader(int id, Bundle params) {
			WebserviceAsync loader = WebserviceAsync.newInstance(getActivity());
			loader.setParams(params);
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<RestResponse> loader, RestResponse pRestResponse) {
			if (loader.getId() == LOADER_ID.LOGIN) {
				parseLoginResponse(pRestResponse);
			}
		}

		@Override
		public void onLoaderReset(Loader<RestResponse> loader) {
			// Do nothing
		}
	};

	/**
	 * Called when relogin is successful and the loaders of this class should be called again
	 */
	public abstract void onReloginSuccessful();

	/**
	 * Called when relogin failed and the user should be informed about a failed attempt
	 */
	public abstract void onReloginFailed();
}
