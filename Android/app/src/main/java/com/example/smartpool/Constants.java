package com.example.smartpool;

public class Constants {
    // States
    public static final String STATE_IDLE = "IDL";
    public static final String STATE_DRAINING_MODE = "DM";
    public static final String STATE_DRAINING_DAY_MODE = "DDM";
    public static final String STATE_DRAINING_PROCESS_DAY = "DPD";
    public static final String STATE_DRAINING_NIGHT_MODE = "DNM";
    public static final String STATE_DRAINING_PROCESS_NIGHT = "DPN";
    public static final String STATE_FILTERING_MODE = "FM";
    public static final String STATE_FILTERING_DAY_MODE = "FDM";
    public static final String STATE_FILTERING_PROCESS_DAY = "FPD";
    public static final String STATE_FILTERING_NIGHT_MODE = "FNM";
    public static final String STATE_FILTERING_PROCESS_NIGHT = "FPN";

    // Events
    public static final String EVENT_SWITCH_DRAINING = "SWITCH_DRAINING";
    public static final String EVENT_SWITCH_FILTERING = "SWITCH_FILTERING";
    public static final String EVENT_HIGH_LIGHT = "HIGH_LIGHT";
    public static final String EVENT_MEDIUM_LIGHT = "MEDIUM_LIGHT";
    public static final String EVENT_LOW_LIGHT = "LOW_LIGHT";
    public static final String EVENT_TIMER_START_WATER_PUMP = "TIMER_START_WATER_PUMP";
    public static final String EVENT_TIMER_STOP_WATER_PUMP = "TIMER_STOP_WATER_PUMP";
    public static final String EVENT_MAX_TEMPERATURE = "MAX_TEMPERATURE";
    public static final String EVENT_LOW_WATER_LEVEL = "LOW_WATER_LEVEL";
    public static final String EVENT_BLUETOOTH_SIGNAL_READY = "BLUETOOTH_SIGNAL_READY";
    public static final String EVENT_BLUETOOTH_SIGNAL_LIGHT_COLOUR = "BLUETOOTH_SIGNAL_LIGHT_COLOUR";
    public static final String EVENT_BLUETOOTH_SIGNAL_LIGHT_MODE = "BLUETOOTH_SIGNAL_LIGHT_MODE";
    public static final String EVENT_BLUETOOTH_SIGNAL_SEND_INFORMATION = "BLUETOOTH_SIGNAL_SEND_INFORMATION";
    public static final String EVENT_CONTINUE = "EVENT_CONTINUE";

    // Pump modes
    public static final String PUMP_MODE_FILTER = "F";
    public static final String PUMP_MODE_DEWATER = "D";

    // Message codes to handle send and receive
    public static final String PUMP_MODE = "B";
    public static final String FINAL_STATE_CURRENT_EVENT_INFO = "E";
    public static final String STATS = "I";
    public static final String FILTER_SCHEDULE = "A";
    public static final String LIGHTS = "L";
    public static final String CHANGE_COLOUR = "C";
    public static final String SWITCH_LIGTHS_MODE = "W";
    public static final int MESSAGE_CODE = 0;
    public static final int CURRENT_PUMP_MODE = 1;
    public static final int FINAL_STATE = 1;
    public static final int CURRENT_STATE = 1;
    public static final int CURRENT_EVENT = 2;
    public static final int WATER_TEMPERATURE = 1;
    public static final int WATER_DISTANCE = 2;

    // Other values used
    public static final int WATER_LEVEL_TRESHOLD = 100;
    public static final int MIN_FILTER_HOURS = 1;
    public static final int MAX_FILTER_HOURS = 12;
    public static final int DEFAULT_FILTER_HOURS = 4;

    public static final String STATS_PREFS = "StatsPrefs";
    public static final String FILTER_TIME_KEY = "LastFilterTime";
    public static final String DEWATER_TIME_KEY = "LastDewaterTime";

    public static final String LIGHT_PREFS = "LightActivityPrefs";
    public static final String SWITCH_STATE_KEY = "switch_state";
    public static final String SELECTED_COLOR_KEY = "selected_color";
}
