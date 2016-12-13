package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * Contains the status of the reponse
 *
 * @author M2Mobi
 */
public class Status {
	@Expose
	public int code;
	@Expose
	public String message;
}
