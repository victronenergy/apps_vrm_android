/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.util;

/**
 * Constants used for analytics
 *
 * @author Victron Energy
 */
public class AnalyticsConstants {
	// Special views that use "subpages" are defined here
	public static final String VIEW_WEBSITE_SETTINGS = "SettingsWebsite";
	public static final String VIEW_WEBSITE_SITE = "SiteWebsite";

	// Categories
	public static final String CAT_UI_ACTION = "ui_action";
	public static final String CAT_GESTURE_ACTION = "gesture_action";
	public static final String CAT_ROTATE_ACTION = "rotate_action";
	public static final String CAT_INPUT_ACTION = "input_action";
	public static final String CAT_TIMING_WEBSERVICE = "webservice";

	// Event actions
	public static final String BUTTON_PRESS = "button_press";
	public static final String PULL = "pull";
	public static final String LIST_PRESS = "list_press";
	public static final String ROTATE_LANDSCAPE = "rotate_landscape";
	public static final String ROTATE_PORTRAIT = "rotate_portrait";
	public static final String SWIPE = "swipe";
	public static final String EDIT_DATA = "edit_data";

	// Event labels login
	public static final String LOGIN_BUTTON = "login_button";
	public static final String DEMO_BUTTON = "demo_button";

	// Event labels sitelist
	public static final String REFRESH_SITELIST = "refresh_sitelist_button";
	public static final String SETTINGS_BUTTON = "settings_button";
	public static final String SEARCH_BUTTON = "search_button";
	public static final String SITELIST_LIST = "sitelist_list";
	public static final String SITELIST_BACK = "sitelist_back_button";

	// Event labels settings
	public static final String SETTINGS_WEBSITE = "settings_website_button";
	public static final String SETTINGS_LOGOUT = "logout_button";
	public static final String SETTINGS_BACK = "settings_back_button";

	// Event historic data
	public static final String HISTORIC_BACK = "historic_back_button";

	// Event labels site summary
	public static final String WEBSITE_BUTTON = "website_button";
	public static final String REFRESH_SITESUM = "refresh_site_summary";
	public static final String GRAPH_BUTTON = "graph_button";
	public static final String GENERATOR_BUTTON = "generator_button";
	public static final String IO_EXTENDER_LIST = "io_extender_list";
	public static final String SITE_SWIPE = "site_swipe";
	public static final String SITE_BACK_BUTTON = "site_back_button";

	// Event labels IO
	public static final String IO_EDIT_BUTTON = "edit_io_button";
	public static final String IO_SAVE_BUTTON = "save_io_button";
	public static final String IO_NAME_EDIT = "io_name_edit";
	public static final String IO_PICTURE_EDIT = "io_picture_edit";
	public static final String IO_TOGGLE = "io_on_off_toggle";
	public static final String IO_BACK_BUTTON = "io_back_button";

	// Event labels graph
	public static final String LEGEND_BUTTON = "graph_legend_button";
	public static final String GRAPH_REFRESH_BUTTON = "graph_refresh_button";
	public static final String TODAY_BUTTON = "graph_today_button";
	public static final String WEEK_BUTTON = "graph_week_button";
	public static final String MONTH_BUTTON = "graph_month_button";
	public static final String YEAR_BUTTON = "graph_year_button";
	public static final String GRAPH_SWIPE = "graph_swipe";
	public static final String GRAPH_BACK_BUTTON = "graph_back_button";

	// Timing name
	public static final String TIMING_LOGIN = "login";
	public static final String TIMING_GRAPH = "graph";

	// Timing label
	public static final String TIMING_LABEL_SPLASH = "splash";
	public static final String TIMING_LABEL_LOGIN = "login";
}
