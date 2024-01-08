package com.example.ledcontrollmkii.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.ledcontrollmkii.ScheduleEntry;
import com.example.ledcontrollmkii.dbhelper.*;//ScheduleContract;

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
                    ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID + " TEXT," +
                    ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE + " TEXT," +
                    ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ScheduleContract.ScheduleEntry.TABLE_NAME;

    public int delete(String rowId) {

        int deletedRows;
        SQLiteDatabase db = _scheduleDbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID + " = ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = {rowId};
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

    public int updateOrInsert(String rowId, String mode, String eventTime) {
        SQLiteDatabase db = _scheduleDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID, rowId);
        values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE, mode);
        values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME, eventTime);

        String selection = ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID + " = ?";
        String[] selectionArgs = {rowId};

        int count = 0;
        try {
            Cursor c = db.query(
                    ScheduleContract.ScheduleEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null, null,  null
            );
            if (c.moveToFirst()) {//if the row exist then return the id

                count = db.update(
                        ScheduleContract.ScheduleEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );  // number 1 is the _id here, update to variable for your code
                Log.i(TAG, "dbUpdated: " + count + " rows");
            } else {

                long newRowId =  db.insert(
                        ScheduleContract.ScheduleEntry.TABLE_NAME,
                        null,
                        values
                );

                Log.i(TAG, "dbInserted: 1 row" + "Id: " + newRowId);
            }

//            count = db.update(
//                    ScheduleContract.ScheduleEntry.TABLE_NAME,
//                    values,
//                    selection,
//                    selectionArgs);


        } catch (Exception e) {
            Log.e(TAG, "dbUpdate: failed" + e.getMessage().toString());

            //throw new RuntimeException(e);
        }

        return count;
    }

    public void insert(String rowId, String mode, String eventTime) {
        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        SQLiteDatabase db = _scheduleDbHelper.getWritableDatabase();

        try {

            ContentValues values = new ContentValues();
            values.put(ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID, rowId);
            values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE, mode);
            values.put(ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME, eventTime);
            newRowId = db.insert(ScheduleContract.ScheduleEntry.TABLE_NAME, null, values);
            Log.i(TAG, "dbInserted: " + newRowId);
            Log.i(TAG, "data: " + rowId + " " + mode + " " + eventTime);

        } catch (Exception e) {

            Log.e(TAG, "dbInsert: failed" + e.getMessage().toString());
        } finally {
            // The database will be automatically closed when the activity is destroyed
            if (db != null && db.isOpen()) {
                db.close();
            }

        }

    }

    public ScheduleEntry getEntryByRowId(String rowId) {
        SQLiteDatabase db = _scheduleDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID,
                ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE,
                ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME
        };

// Filter results WHERE "SCHEDULE_ROW_ID" = rowId
        String selection = ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID + " = ?";
        String[] selectionArgs = {rowId};

// How you want the results sorted in the resulting Cursor
//        String sortOrder =
//                ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID + " DESC";

        Cursor cursor = null;
        ScheduleEntry entry = null;
        String row_id;
        String mode;
        String eventTime;
        try {
            cursor = db.query(
                    ScheduleContract.ScheduleEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause (null for all rows)
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null               // The sort order
            );


            // Get values from columns
            cursor.moveToFirst();
            row_id = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID));
            mode = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE));
            eventTime = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME));

            cursor.close();

            entry = new ScheduleEntry.Builder()
                    .row_id(row_id)
                    .mode(mode)
                    .eventTime(eventTime)
                    .build();
            Log.i(TAG, "dbGet: " + entry.toString());

        } catch (Exception e) {
            Log.e(TAG, "dbGet: failed" + e.getMessage().toString());

            //throw new RuntimeException(e);
        } finally {
            // The database will be automatically closed when the activity is destroyed
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return entry;
    }

    public ArrayList<ScheduleEntry> getSixEntries() {

        ArrayList<ScheduleEntry> entries = new ArrayList<>();
        SQLiteDatabase db = _scheduleDbHelper.getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID,
                ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE,
                ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME
        };

        Cursor cursor = null;
        String row_id, mode, eventTime;
        ScheduleEntry entry = null;
        String selection = ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID + " BETWEEN ? AND ?";
        String[] selectionArgs = {String.valueOf(1), String.valueOf(6)};

        try {
            cursor = db.query(
                    ScheduleContract.ScheduleEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause (null for all rows)
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null               // The sort order
            );

            while (cursor.moveToNext()) {

                row_id = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.SCHEDULE_ROW_ID));
                mode = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_NAME_MODE));
                eventTime = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_NAME_TIME));
                entry = new ScheduleEntry.Builder()
                        .row_id(row_id)
                        .mode(mode)
                        .eventTime(eventTime)
                        .build();

                entries.add(entry);
                Log.i(TAG, "row data ROW:" + entry.getScheduleRow() + " MODE:" + entry.getMode() + " EVENT TIME:" + entry.getEventTime());
            }

            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "dbGet: failed" + e.getMessage().toString());
        } finally {
            // The database will be automatically closed
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return entries;
    }
}

