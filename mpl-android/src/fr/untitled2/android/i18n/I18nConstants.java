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

    /** Log view **/
    public static final String logstart_bundle_name = "logstart";
    public static final I18nResourceKey logstart_name = new I18nResourceKey(logstart_bundle_name, "log.name", "Log Name");
    public static final I18nResourceKey logstart_time_zone = new I18nResourceKey(logstart_bundle_name, "log.timezone", "Time zone");
    public static final I18nResourceKey logstart_point_count = new I18nResourceKey(logstart_bundle_name, "log.pointcount", "Point count");
    public static final I18nResourceKey logstart_latitude = new I18nResourceKey(logstart_bundle_name, "log.latitude", "Latitude");
    public static final I18nResourceKey logstart_longitude = new I18nResourceKey(logstart_bundle_name, "log.longitude", "Longitude");
    public static final I18nResourceKey logstart_date = new I18nResourceKey(logstart_bundle_name, "log.date", "Date");
    public static final I18nResourceKey logstart_startnew = new I18nResourceKey(logstart_bundle_name, "log.startnew", "Start");
    public static final I18nResourceKey logstart_not_available = new I18nResourceKey(logstart_bundle_name, "log.notavailable", "Stop");
    public static final I18nResourceKey logstart_stoplog = new I18nResourceKey(logstart_bundle_name, "log.stoplog", "Stop");
    public static final I18nResourceKey log_started = new I18nResourceKey(logstart_bundle_name, "log.started", "Log started");
    public static final I18nResourceKey log_stopped = new I18nResourceKey(logstart_bundle_name, "log.stoped", "Log started");

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
    /** Connect View **/
    public static final String connect_bundle_name = "connect";

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

    /** Film Tool **/
    public static final String filmtool_bundle_name = "filmtool";
    public static final I18nResourceKey filmtool_enterid = new I18nResourceKey(filmtool_bundle_name, "enterfilmid", "Enter film id");


}
