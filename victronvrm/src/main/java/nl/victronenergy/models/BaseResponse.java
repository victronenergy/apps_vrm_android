/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * Base Response, every response is formatted this way with a custom data object
 *
 * @author Victron Energy
 */
public class BaseResponse {
	@Expose
	public Status status;
}
