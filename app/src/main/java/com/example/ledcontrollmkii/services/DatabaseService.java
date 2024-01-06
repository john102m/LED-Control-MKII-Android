package com.example.ledcontrollmkii.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.ledcontrollmkii.dbhelper.ScheduleContract;

import java.util.ArrayList;
import java.util.List;

import com.example.ledcontrollmkii.dbhelper.*;

public class DatabaseService {

    private static final String TAG = "DatabaseService";
    private ScheduleDbHelper _scheduleDbHelper;// = new ScheduleDbHelper(getApplicationContext());

    public DatabaseService(com.example.ledcontrollmkii.dbhelper.ScheduleDbHelper scheduleDbHelper) {
        _scheduleDbHelper = scheduleDbHelper;
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ScheduleContract.ScheduleEntry.TABLE_NAME + " (" +
                    ScheduleContract.ScheduleEntry._ID + " INTEGER PRIMARY KEY," +
                    ScheduleContract.ScheduleEntry.SCHEDULE_ITEM_ID + " TEXT," +
                    ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE + " TEXT," +
                    ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ScheduleContract.ScheduleEntry.TABLE_NAME;

    public int delete(String ItemId) {

        int deletedRows;
        SQLiteDatabase db = _scheduleDbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = ScheduleContract.ScheduleEntry.SCHEDULE_ITEM_ID + " = ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = {ItemId};
// Issue SQL statement.
        try {
            deletedRows = db.delete(ScheduleContract.ScheduleEntry.TABLE_NAME, selection, selectionArgs);
            Log.i(TAG, "dbDeleted: " + deletedRows + " rows");
        } catch (Exception e) {
            Log.e(TAG, "dbDelete: failed" + e.getMessage().toString());
            deletedRows = 0;
            //throw new RuntimeException(e);
        }


        return deletedRows;
    }

    public int update(String itemId, String mode, String eventTime) {
        SQLiteDatabase db = _scheduleDbHelper.getWritableDatabase();

// New value for one column
        //String mode = "MODE 5";
        ContentValues values = new ContentValues();
        values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE, mode);
        values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME, eventTime);

// Which row to update, based on the title
        String selection = ScheduleContract.ScheduleEntry.SCHEDULE_ITEM_ID + " = ?";
        String[] selectionArgs = {itemId};

        int count = 0;
        try {
            count = db.update(
                    ScheduleContract.ScheduleEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            Log.i(TAG, "dbUpdated: " + count + " rows");
        } catch (Exception e) {
            Log.e(TAG, "dbUpdate: failed" + e.getMessage().toString());

            //throw new RuntimeException(e);
        }

        return count;
    }

    public void insert(String itemId, String mode, String eventTime) {
        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        SQLiteDatabase db = _scheduleDbHelper.getWritableDatabase();

        try {

            ContentValues values = new ContentValues();
            values.put(ScheduleContract.ScheduleEntry.SCHEDULE_ITEM_ID, itemId);
            values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE, mode);
            values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME, eventTime);
            newRowId = db.insert(ScheduleContract.ScheduleEntry.TABLE_NAME, null, values);
            Log.i(TAG, "dbInserted: " + newRowId);
            Log.i(TAG, "data: " + itemId + " "  + mode + " " + eventTime);

        } catch (Exception e) {

            Log.e(TAG, "dbInsert: failed" + e.getMessage().toString());
        } finally {
            // The database will be automatically closed when the activity is destroyed
            if (db != null && db.isOpen()) {
                db.close();
            }

        }

    }

    public void getDataById(String Id) {
        SQLiteDatabase db = _scheduleDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                ScheduleContract.ScheduleEntry.SCHEDULE_ITEM_ID,
                ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE,
                ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME
        };

// Filter results WHERE "title" = 'My Title'
        String selection = ScheduleContract.ScheduleEntry.SCHEDULE_ITEM_ID + " = ?";
        String[] selectionArgs = {"MODE 1"};

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME + " DESC";

        Cursor cursor = null;
        try {
            cursor = db.query(
                    ScheduleContract.ScheduleEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            List itemIds = new ArrayList<>();
            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(ScheduleContract.ScheduleEntry._ID));
                itemIds.add(itemId);
                Log.i(TAG, "dbGet: " + itemId);
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "dbGet: failed" + e.getMessage().toString());

            //throw new RuntimeException(e);
        } finally {
            // The database will be automatically closed when the activity is destroyed
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

    }
}
