package Helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Pierre Rivierre on 8/23/2016.
 */
public class Contract {
    public Contract() {}

    public static abstract class EntrySettings {
        public static final String TABLE_NAME = "entry_settings";
        public static final String COLUMN_NAME_ENTRY_ID = "entry_id"; // primary key
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LOCATION_ID = "location_id"; // location p. key
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_GEO_ID = "geofence_id";
        public static final String COLUMN_NAME_LATITUDE = "latitute";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_RADIUS = "radius";


        public static final String[] BASE_COLUMNS = new String[] {
                COLUMN_NAME_TITLE,
                COLUMN_NAME_LOCATION_ID,
                COLUMN_NAME_START_TIME,
                COLUMN_NAME_END_TIME};

        public static String  createTable() {
            return "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_ENTRY_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME_TITLE + " TEXT," + COLUMN_NAME_LOCATION_ID + " TEXT,"
                    + COLUMN_NAME_START_TIME + " TEXT," + COLUMN_NAME_GEO_ID + " TEXT,"
                    + COLUMN_NAME_LATITUDE + " TEXT," + COLUMN_NAME_LONGITUDE + " TEXT,"
                    + COLUMN_NAME_RADIUS + " TEXT," + COLUMN_NAME_END_TIME + " TEXT)";
        }
    }

    public class DBHelper extends SQLiteOpenHelper {

        public static final int DB_VERSION = 1;
        public static final String DB_NAME = "silencr.db";

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(EntrySettings.createTable());
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE " + EntrySettings.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
