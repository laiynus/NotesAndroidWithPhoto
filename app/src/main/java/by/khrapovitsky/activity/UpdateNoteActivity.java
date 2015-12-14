package by.khrapovitsky.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import by.khrapovitsky.R;
import by.khrapovitsky.helper.DatabaseHelper;
import by.khrapovitsky.model.Note;

public class UpdateNoteActivity extends AppCompatActivity implements View.OnClickListener{

    EditText noteText = null;
    EditText dateModify = null;
    Button updateButton = null;
    Note note = null;
    Integer id = null;

    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_note);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Update note");
        }
        noteText = (EditText) findViewById(R.id.noteTextUpdate);
        dateModify = (EditText) findViewById(R.id.dateModifyUpdate);
        id = Integer.parseInt(getIntent().getStringExtra("id"));
        note = databaseHelper.readNote(id);
        noteText.setText(note.getNoteText());
        noteText.setSelection(noteText.getText().length());
        dateModify.setText(note.getLastDateModify());
        dateModify.setEnabled(false);
        dateModify.setFocusable(false);
        updateButton = (Button) findViewById(R.id.button_update);
        updateButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        View view = this.findViewById(android.R.id.content);
        view.setBackgroundColor(Color.parseColor(preferences.getString("backgroundColor", "White")));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_update:
                String noteTmp =  noteText.getText().toString();
                if(StringUtils.isBlank(noteTmp)){
                    Toast.makeText(getApplicationContext(), "Note can't be empty", Toast.LENGTH_LONG).show();
                }else{
                    note.setNoteText(noteTmp);
                    SimpleDateFormat date = new SimpleDateFormat ("dd.MM.yyyy hh:mm:ss");
                    note.setLastDateModify(date.format(new Date()));
                    databaseHelper.updateNote(note);
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
