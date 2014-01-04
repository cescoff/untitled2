package fr.untitled2.android.i18n;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/11/13
 * Time: 6:19 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class I18nConstants {
    /** Main View **/
    public static final String main_bundle_name = "main";
    public static final I18nResourceKey main_start_log = new I18nResourceKey(main_bundle_name, "start.log", "Start log");
    public static final I18nResourceKey main_view_log = new I18nResourceKey(main_bundle_name, "view.log", "View Log");
    public static final I18nResourceKey main_settings = new I18nResourceKey(main_bundle_name, "settings", "Settings");
    public static final I18nResourceKey main_welcome = new I18nResourceKey(main_bundle_name, "welcome", "Welcome to MyPictureLog");
    public static final I18nResourceKey main_home = new I18nResourceKey(main_bundle_name, "home", "Home");
    public static final I18nResourceKey main_list_logs = new I18nResourceKey(main_bundle_name, "list.log", "List logs");
    public static final I18nResourceKey not_connected_alert_title = new I18nResourceKey(main_bundle_name, "notconnected.alert", "You are not connected please first connect using settings view");
    public static final I18nResourceKey knownlocationlist_menusettings_label = new I18nResourceKey(main_bundle_name, "menusetting", "Settings");
    public static final I18nResourceKey knownlocationlist_menuglobalsettings_label = new I18nResourceKey(main_bundle_name, "menuglobalsettings", "General");
    public static final I18nResourceKey knownlocationlist_menulocationsettings_label = new I18nResourceKey(main_bundle_name, "menuknownlocationsettings", "Locations");
    public static final I18nResourceKey knownlocationlist_menuknownsportsettings_label = new I18nResourceKey(main_bundle_name, "menuknownsportsettings", "Sport");
    public static final I18nResourceKey knownlocationlist_menuhistory_label = new I18nResourceKey(main_bundle_name, "menuhistory", "History");
    public static final I18nResourceKey knownlocationlist_menuhistorylogs_label = new I18nResourceKey(main_bundle_name, "menuhistorylogs", "Logs");
    public static final I18nResourceKey knownlocationlist_menuhistoryjourneys_label = new I18nResourceKey(main_bundle_name, "menuhistoryjourneys", "Journeys");
    public static final I18nResourceKey knownlocationlist_add_knownlocation = new I18nResourceKey(main_bundle_name, "addknownlocation", "Add location");
    public static final I18nResourceKey main_title = new I18nResourceKey(main_bundle_name, "title", "Home");

    /** Log view **/
    public static final String logstart_bundle_name = "logstart";
    public static final I18nResourceKey logstart_name = new I18nResourceKey(logstart_bundle_name, "log.name", "Log Name");
    public static final I18nResourceKey logstart_time_zone = new I18nResourceKey(logstart_bundle_name, "log.timezone", "Time zone");
    public static final I18nResourceKey logstart_point_count = new I18nResourceKey(logstart_bundle_name, "log.pointcount", "Point count");
    public static final I18nResourceKey logstart_latitude = new I18nResourceKey(logstart_bundle_name, "log.latitude", "Latitude");
    public static final I18nResourceKey logstart_longitude = new I18nResourceKey(logstart_bundle_name, "log.longitude", "Longitude");
    public static final I18nResourceKey logstart_altitude = new I18nResourceKey(logstart_bundle_name, "log.altitude", "Altitude");
    public static final I18nResourceKey logstart_date = new I18nResourceKey(logstart_bundle_name, "log.date", "Date");
    public static final I18nResourceKey logstart_distance = new I18nResourceKey(logstart_bundle_name, "log.distance", "Distance");
    public static final I18nResourceKey logstart_startnew = new I18nResourceKey(logstart_bundle_name, "log.startnew", "Start");
    public static final I18nResourceKey logstart_not_available = new I18nResourceKey(logstart_bundle_name, "log.notavailable", "Stop");
    public static final I18nResourceKey logstart_stoplog = new I18nResourceKey(logstart_bundle_name, "log.stoplog", "Stop");
    public static final I18nResourceKey log_started = new I18nResourceKey(logstart_bundle_name, "log.started", "Log started");
    public static final I18nResourceKey log_stopped = new I18nResourceKey(logstart_bundle_name, "log.stoped", "Log started");

    /** Journey List View **/
    public static final String journeylist_bundle_name = "journeylist";
    public static final I18nResourceKey journeylist_start_label = new I18nResourceKey(journeylist_bundle_name, "startlabel", "From");
    public static final I18nResourceKey journeylist_end_label = new I18nResourceKey(journeylist_bundle_name, "endlabel", "to");
    public static final I18nResourceKey journeylist_here_label = new I18nResourceKey(journeylist_bundle_name, "herelabel", "Current position");
    public static final I18nResourceKey journeylist_distance_label = new I18nResourceKey(journeylist_bundle_name, "distancelabel", "Distance :");
    public static final I18nResourceKey journeylist_duration_label = new I18nResourceKey(journeylist_bundle_name, "durationlabel", "Duration :");
    public static final I18nResourceKey journeylist_avgspeed_label = new I18nResourceKey(journeylist_bundle_name, "avgspeedlabel", "Avg speed :");
    public static final I18nResourceKey journeylist_maxspeed_label = new I18nResourceKey(journeylist_bundle_name, "maxseeplabel", "Max speed :");
    public static final I18nResourceKey journeylist_title = new I18nResourceKey(journeylist_bundle_name, "title", "Journeys");

    /** Knownlocation list view **/
    public static final String knownlocationlist_bundle_name = "knownlocationlist";
    public static final I18nResourceKey knownlocationlist_add_ssid_alert_label = new I18nResourceKey(knownlocationlist_bundle_name, "addknownlocationalerttitle", "Add current wifi to known locations ?");
    public static final I18nResourceKey knownlocationlist_add_wifi_to_knownlocation_label = new I18nResourceKey(knownlocationlist_bundle_name, "confirmaddknownlocationtitle", "Add wifi to known locations");
    public static final I18nResourceKey knownlocationlist_title = new I18nResourceKey(knownlocationlist_bundle_name, "title", "Locations");

    /** Log List View **/
    public static final String loglist_bundle_name = "loglist";
    public static final I18nResourceKey loglist_delete_question = new I18nResourceKey(loglist_bundle_name, "deletequestion", "Are you sure you want to delete this log ?");
    public static final I18nResourceKey loglist_delete_button_yes = new I18nResourceKey(loglist_bundle_name, "deleteyes", "Yes");
    public static final I18nResourceKey loglist_delete_button_no = new I18nResourceKey(loglist_bundle_name, "deleteno", "No");
    public static final I18nResourceKey loglist_notconnected = new I18nResourceKey(loglist_bundle_name, "notconnected", "You are not connected unable to upload log");
    public static final I18nResourceKey loglist_uploadyes = new I18nResourceKey(loglist_bundle_name, "uploadyes", "Yes");
    public static final I18nResourceKey loglist_uploadno = new I18nResourceKey(loglist_bundle_name, "uploadno", "No");
    public static final I18nResourceKey loglist_uploadalerttitle = new I18nResourceKey(loglist_bundle_name, "uploadalerttitle", "Are you sure you want to upload this log ?");
    public static final I18nResourceKey loglist_erroralerttitle = new I18nResourceKey(loglist_bundle_name, "errordialogtitle", "An error has occured");
    public static final I18nResourceKey loglist_uploadoktitle = new I18nResourceKey(loglist_bundle_name, "uploadoktitle", "Your log has been successfully uploaded");
    public static final I18nResourceKey loglist_title = new I18nResourceKey(loglist_bundle_name, "title", "History");
    /** Connect View **/
    public static final String connect_bundle_name = "connect";
    public static final I18nResourceKey connect_title = new I18nResourceKey(connect_bundle_name, "title", "Connect");

    /** Settings View **/
    public static final String settings_bundle_name = "settings";
    public static final I18nResourceKey settings_connected_account_label = new I18nResourceKey(settings_bundle_name, "connectedaccount", "Connected account");
    public static final I18nResourceKey settings_not_available = new I18nResourceKey(settings_bundle_name, "notavailable", "Not available");
    public static final I18nResourceKey settings_log_precision = new I18nResourceKey(settings_bundle_name, "logprecision", "Precision");
    public static final I18nResourceKey settings_frequency = new I18nResourceKey(settings_bundle_name, "frequency", "Frequency");
    public static final I18nResourceKey settings_autolog = new I18nResourceKey(settings_bundle_name, "autolog", "Auto mode");
    public static final I18nResourceKey settings_camera_time_zone = new I18nResourceKey(settings_bundle_name, "cameratimezone", "Camera time zone");
    public static final I18nResourceKey settings_date_format = new I18nResourceKey(settings_bundle_name, "prefereddateformat", "Date format");
    public static final I18nResourceKey settings_language = new I18nResourceKey(settings_bundle_name, "preferedlanguage", "Language");
    public static final I18nResourceKey settings_disconnect_account = new I18nResourceKey(settings_bundle_name, "disconnectaccount", "Disconnect account");
    public static final I18nResourceKey settings_autosynchourofday = new I18nResourceKey(settings_bundle_name, "autosynchour", "Auto sync hour of day");
    public static final I18nResourceKey settings_accountconnection = new I18nResourceKey(settings_bundle_name, "accountconnection", "Cloud account");
    public static final I18nResourceKey settings_connectaccount = new I18nResourceKey(settings_bundle_name, "connectaccount", "Connect");
    public static final I18nResourceKey settings_filmmode = new I18nResourceKey(settings_bundle_name, "filmmode", "Use film counter");
    public static final I18nResourceKey settings_knwonlocations = new I18nResourceKey(settings_bundle_name, "knwonlocations", "Known locations");
    public static final I18nResourceKey settings_knwonlocationsna = new I18nResourceKey(settings_bundle_name, "naknownlocations", "N/A");
    public static final I18nResourceKey settings_title = new I18nResourceKey(settings_bundle_name, "title", "Settings");

    /** Film Tool **/
    public static final String filmtool_bundle_name = "filmtool";
    public static final I18nResourceKey filmtool_enterid = new I18nResourceKey(filmtool_bundle_name, "enterfilmid", "Enter film id");

    /** SMS **/
    /*
    locationunknown=Unknown location
lastknownlocationwithjourneystarted=Known location at {0} was latitude {1}, longitude {2}, altitude {3}. Journey started from {4} for {5}. Average speed {6}. http://maps.google.com/maps?q=loc:{7},{8}
lastknownlocationwithknownlocation=Known location at {0} was latitude {1}, longitude {2}, altitude {3}. Current location is {4}. http://maps.google.com/maps?q=loc:{5},{6}
lastknownlocation=Known location at {0} was latitude {1}, longitude {2}, altitude {3}. http://maps.google.com/maps?q=loc:{4},{5}

     */
    public static final String sms_bundle_name = "sms";
    public static final I18nResourceKey sms_locationunknown = new I18nResourceKey(sms_bundle_name, "locationunknown", "Unknown location");
    public static final I18nResourceKey sms_lastknownlocationwithjourneystarted = new I18nResourceKey(sms_bundle_name, "lastknownlocationwithjourneystarted", "Error");
    public static final I18nResourceKey sms_lastknownlocationwithknownlocation = new I18nResourceKey(sms_bundle_name, "lastknownlocationwithknownlocation", "Error");
    public static final I18nResourceKey sms_lastknownlocation = new I18nResourceKey(sms_bundle_name, "lastknownlocation", "Error");

    public static final String knownlocationadd_bundle_name = "knownlocationadd";
    public static final I18nResourceKey knownlocationadd_title = new I18nResourceKey(knownlocationadd_bundle_name, "title", "Home");
    public static final I18nResourceKey knownlocationadd_labelname = new I18nResourceKey(knownlocationadd_bundle_name, "labelname", "Name : ");
    public static final I18nResourceKey knownlocationadd_labelusercurrentlocation = new I18nResourceKey(knownlocationadd_bundle_name, "labelusecurrentlocation", "Use current location : ");
    public static final I18nResourceKey knownlocationadd_labeladdress = new I18nResourceKey(knownlocationadd_bundle_name, "labeladdress", "Address : ");
    public static final I18nResourceKey knownlocationadd_validate = new I18nResourceKey(knownlocationadd_bundle_name, "validatebutton", "Ok");
}
