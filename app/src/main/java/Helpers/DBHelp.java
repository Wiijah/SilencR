package Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Pierre Rivierre on 8/24/2016.
 */
public class DBHelp {
    Context context;
    Contract.DBHelper dbh;
    public DBHelp(Context context) {
        this.context = context;
        dbh = new Contract().new DBHelper(context);
    }

    public void close() {
        dbh.close();
    }

    public long insert(String TABLE_NAME, String[] COLUMNS, String[] VALUES) {
        SQLiteDatabase db = dbh.getWritableDatabase();

        if(COLUMNS.length != VALUES.length) {
            return -1;
        }

        ContentValues values = new ContentValues();
        for (int i = 0; i < COLUMNS.length; i++) {
            if (VALUES[i] == null) {
                values.putNull(COLUMNS[i]);
            } else {
                values.put(COLUMNS[i], VALUES[i]);
            }
        }

        long id = db.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public Cursor read(String TABLE_NAME, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return c;
    }

    public int update(String TABLE_NAME, String[] COLUMNS, String[] VALUES, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbh.getReadableDatabase();

        ContentValues values = new ContentValues();
        if (COLUMNS.length != VALUES.length) {
            return -1;
        }

        for (int i = 0; i < COLUMNS.length; i++) {
            if (VALUES[i] == null) {
                values.putNull(COLUMNS[i]);
            } else {
                values.put(COLUMNS[i], VALUES[i]);
            }
        }

        int count = db.update(TABLE_NAME, values, selection, selectionArgs);
        close();
        return count;
    }

    public void delete(String TABLE_NAME, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        db.delete(TABLE_NAME, selection, selectionArgs);
        close();
    }
}
