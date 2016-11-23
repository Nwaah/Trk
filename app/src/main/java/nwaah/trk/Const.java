package nwaah.trk;

import android.content.Intent;

public abstract class Const {
    public final static String preferences_key_frequency = "Frequency";
    public final static String preferences_key_step = "Step";
    public final static String current_track_id = "CurrentTrackId";
    public final static String current_track_name = "CurrentTrackName";

    public final static String key_id = "id";
    public final static String key_longitude = "longitude";
    public final static String key_latitude = "latitude";
    public final static String key_altitude = "altitude";
    public final static String key_mode = "mode";

    public final static String intent_filter_action_ping_service = Intent.ACTION_ANSWER;
    public final static String intent_filter_action_order_button_change = Intent.ACTION_CALL_BUTTON;
    public final static String intent_filter_action_refresh_views = Intent.ACTION_SYNC;
    public final static String intent_filter_action_settings_updated = Intent.ACTION_SEARCH;
}
