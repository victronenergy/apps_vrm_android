package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * Base Response, every response is formatted this way with a custom data object
 *
 * @author M2Mobi
 */
public class BaseResponse {
    @Expose
	public Status status;
}
