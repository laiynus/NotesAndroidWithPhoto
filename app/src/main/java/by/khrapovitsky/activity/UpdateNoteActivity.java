package by.khrapovitsky.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import by.khrapovitsky.R;
import by.khrapovitsky.fragment.RetainedFragment;
import by.khrapovitsky.fragment.SelectPhotoDialog;
import by.khrapovitsky.helper.DatabaseHelper;
import by.khrapovitsky.model.BitmapAndPath;
import by.khrapovitsky.model.Note;
import by.khrapovitsky.util.BitmapUtil;

public class UpdateNoteActivity extends AppCompatActivity implements View.OnClickListener{

    private RetainedFragment dataFragment;

    private EditText noteText = null;
    private EditText dateModify = null;
    private Button updateButton = null;
    private ImageView imageView = null;
    private Button selectPhoto = null;
    private String pathImage = null;
    private Bitmap bitmap = null;
    private Note note = null;
    private Integer id = null;

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
        imageView = (ImageView) findViewById(R.id.imageViewUpdateNote);
        selectPhoto = (Button) findViewById(R.id.selectPhotoUpdateButton);
        dateModify = (EditText) findViewById(R.id.dateModifyUpdate);
        id = Integer.parseInt(getIntent().getStringExtra("id"));
        note = databaseHelper.readNote(id);
        if(note.getImagePath()!=null){
            BitmapUtil bitmapUtil = new BitmapUtil(UpdateNoteActivity.this);
            imageView.setImageBitmap(bitmapUtil.getBitmapByPath(note.getImagePath()));
        }
        noteText.setText(note.getNoteText());
        noteText.setSelection(noteText.getText().length());
        dateModify.setText(note.getLastDateModify());
        dateModify.setEnabled(false);
        dateModify.setFocusable(false);
        updateButton = (Button) findViewById(R.id.button_update);
        updateButton.setOnClickListener(this);
        selectPhoto.setOnClickListener(this);
        FragmentManager fm = getSupportFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("data");
        if (savedInstanceState == null) {
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();
        }else{
            bitmap = dataFragment.getData().getBitmap();
            pathImage = dataFragment.getData().getPath();
            if(bitmap!=null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        View view = this.findViewById(android.R.id.content);
        view.setBackgroundColor(Color.parseColor(preferences.getString("backgroundColor", "White")));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataFragment.setData(new BitmapAndPath(bitmap, pathImage));
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            BitmapUtil bitmapUtil = new BitmapUtil(UpdateNoteActivity.this);
            if (requestCode == SelectPhotoDialog.SELECT_FILE){
                BitmapAndPath tmp = bitmapUtil.onSelectFromGalleryResult(data);
                bitmap = tmp.getBitmap();
                pathImage = tmp.getPath();
                imageView.setImageBitmap(bitmap);
            }
            else if (requestCode == SelectPhotoDialog.REQUEST_CAMERA){
                BitmapAndPath tmp = bitmapUtil.onCaptureImageResult(data);
                bitmap = tmp.getBitmap();
                pathImage = tmp.getPath();
                imageView.setImageBitmap(bitmap);
            }
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
                    if(pathImage != null){
                        note.setImagePath(pathImage);
                    }
                    databaseHelper.updateNote(note);
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
                break;
            case R.id.selectPhotoUpdateButton:
                new SelectPhotoDialog().show(getFragmentManager(), "Please select a photo");
                break;
            default:
                break;
        }
    }
}
