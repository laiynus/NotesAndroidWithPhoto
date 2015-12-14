package by.khrapovitsky.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;

import by.khrapovitsky.helper.DatabaseHelper;

public class NotesContentProvider extends ContentProvider {

    static final String AUTHORITIES = "by.khrapovitsky.provider";
    static final String URL = "content://" + AUTHORITIES + "/notes";
    static final Uri CONTENT_URI = Uri.parse(URL);

    private static HashMap<String, String> NOTES_MAP;

    private static final String TABLE_NOTES = "notes";

    private static final String KEY_ID = "_id";
    private static final String KEY_NOTE_TEXT= "noteText";
    private static final String KEY_LAST_DATE_MODIFY = "lastDateModify";

    static final int ALL_NOTES = 1;
    static final int ONE_NOTE = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITIES, "notes", ALL_NOTES);
        uriMatcher.addURI(AUTHORITIES, "notes/#", ONE_NOTE);
    }

    private SQLiteDatabase db;
    private DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case ALL_NOTES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = KEY_ID + " ASC";
                }
                break;
            case ONE_NOTE:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = KEY_ID + " = " + id;
                } else {
                    selection = selection + " AND " + KEY_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = databaseHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, projection, selection,selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),CONTENT_URI);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case ALL_NOTES:
                return "vnd.android.cursor.dir/vnd.by.khrapovitsky.provider/notes";
            case ONE_NOTE:
                return "vnd.android.cursor.item/vnd.by.khrapovitsky.provider/notes";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(TABLE_NOTES, "", values);
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case ALL_NOTES:
                count = db.delete(TABLE_NOTES, selection, selectionArgs);
                break;
            case ONE_NOTE:
                String id = uri.getPathSegments().get(1);
                count = db.delete( TABLE_NOTES, KEY_ID +  " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case ALL_NOTES:
                count = db.update(TABLE_NOTES, values, selection, selectionArgs);
                break;
            case ONE_NOTE:
                count = db.update(TABLE_NOTES, values, KEY_ID + " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
