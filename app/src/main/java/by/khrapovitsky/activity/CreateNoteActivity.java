package by.khrapovitsky.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

public class CreateNoteActivity extends AppCompatActivity implements View.OnClickListener{

    private RetainedFragment dataFragment;

    private Button createButton = null;
    private Button selectPhoto = null;
    private EditText noteText = null;
    private ImageView imageView = null;
    private String pathImage = null;
    private Bitmap bitmap = null;
    private DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Create note");
        }
        createButton = (Button) findViewById(R.id.noteButtonCreate);
        selectPhoto = (Button) findViewById(R.id.selectPhotoButton);
        createButton.setOnClickListener(this);
        selectPhoto.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageViewNote);
        noteText = (EditText) findViewById(R.id.noteTextCreate);
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
        view.setBackgroundColor(Color.parseColor(preferences.getString("backgroundColor", "WHITE")));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.noteButtonCreate:
                String note =  noteText.getText().toString();
                if(StringUtils.isBlank(note)){
                    Toast.makeText(getApplicationContext(), "Note can't be empty", Toast.LENGTH_LONG).show();
                }else{
                    databaseHelper.createNote(new Note(note, pathImage,new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").format(new Date())));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    noteText.setText("");
                    Toast.makeText(getApplicationContext(), "Note has successfully created", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.selectPhotoButton:
                new SelectPhotoDialog().show(getFragmentManager(), "Please select a photo");
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            BitmapUtil bitmapUtil = new BitmapUtil(CreateNoteActivity.this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataFragment.setData(new BitmapAndPath(bitmap,pathImage));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




}
