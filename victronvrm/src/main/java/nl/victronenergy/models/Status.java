/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * Contains the status of the reponse
 *
 * @author Victron Energy
 */
public class Status {
	@Expose
	public int code;
	@Expose
	public String message;
}
