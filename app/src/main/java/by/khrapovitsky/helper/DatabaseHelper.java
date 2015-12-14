package by.khrapovitsky.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import by.khrapovitsky.model.Note;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "notesManager";

    private static final String TABLE_NOTES = "notes";

    private static final String KEY_ID = "_id";
    private static final String KEY_NOTE_TEXT = "noteText";
    private static final String KEY_IMAGE_PATH = "imagePath";
    private static final String KEY_LAST_DATE_MODIFY = "lastDateModify";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NOTE_TEXT + " TEXT,"
                + KEY_IMAGE_PATH + " TEXT,"
                + KEY_LAST_DATE_MODIFY + " DATETIME" + ")";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public void createNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_TEXT, note.getNoteText());
        values.put(KEY_IMAGE_PATH, note.getImagePath());
        values.put(KEY_LAST_DATE_MODIFY, note.getLastDateModify());
        db.insert(TABLE_NOTES, null, values);
        db.close();
    }

    public Note readNote(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, new String[] { KEY_ID,
                        KEY_NOTE_TEXT,KEY_IMAGE_PATH, KEY_LAST_DATE_MODIFY }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Note note = null;
        if (cursor != null) {
            note = new Note(Integer.parseInt(cursor.getString(0)),cursor.getString(1),cursor.getString(2), cursor.getString(3));
            cursor.close();
        }
        db.close();
        return note;
    }

    public int updateNote(Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_TEXT, note.getNoteText());
        values.put(KEY_IMAGE_PATH,note.getImagePath());
        values.put(KEY_LAST_DATE_MODIFY, note.getLastDateModify());
        int count = db.update(TABLE_NOTES, values, KEY_ID + " = ?", new String[] { String.valueOf(note.getId()) });
        db.close();
        return count;
    }

    public void deleteNote(Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public List<Note> getAllNotes() {
        List<Note> noteList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(Integer.parseInt(cursor.getString(0)));
                note.setNoteText(cursor.getString(1));
                note.setImagePath(cursor.getString(2));
                note.setLastDateModify(cursor.getString(3));
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return noteList;
    }

    public Cursor getAllForAdapter(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT _id,noteText,imagePath,lastDateModify FROM " + TABLE_NOTES,null);
    }

}
