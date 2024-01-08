package com.example.ledcontrollmkii.dbhelper;
import android.provider.BaseColumns;
public final class ScheduleContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ScheduleContract() {}

    /* Inner class that defines the table contents */
    public static class ScheduleEntry implements BaseColumns {
        public static final String TABLE_NAME = "schedule";
        public static final String SCHEDULE_ROW_ID = "schedule_row_id";
        public static final String COLUMN_NAME_MODE = "mode";
        public static final String COLUMN_NAME_TIME= "time";
    }
}
