package nwaah.trk;

import android.content.Intent;

public abstract class Const {
    public final static String preferences_key_setting_frequency = "Frequency";
    public final static String preferences_key_setting_min_step = "Min Step";
    public final static String preferences_key_setting_max_step = "Max Step";
    public final static String preferences_key_setting_draw_circles = "draw_circles";
    public final static String preferences_key_setting_navigation_complete_proximity = "navigation_complete_proximity";
    public final static String preferences_key_setting_provider = "provider";


    public final static String current_track_id = "CurrentTrackId";
    public final static String current_track_name = "CurrentTrackName";

    public final static String key_id = "id";
    public final static String key_longitude = "longitude";
    public final static String key_latitude = "latitude";
    public final static String key_altitude = "altitude";
    public final static String key_mode = "mode";
    public final static String key_goal_lat = "goal_lat";
    public final static String key_goal_lng = "goal_lng";

    public final static float key_goal_none = 999;



    public final static String intent_filter_action_ping_service = Intent.ACTION_ANSWER;
    public final static String intent_filter_action_order_button_change = Intent.ACTION_CALL_BUTTON;
    public final static String intent_filter_action_refresh_views = Intent.ACTION_SYNC;
    public final static String intent_filter_action_settings_updated = Intent.ACTION_SEARCH;
}
