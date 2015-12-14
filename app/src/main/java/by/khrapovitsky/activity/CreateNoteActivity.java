package by.khrapovitsky.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import by.khrapovitsky.R;
import by.khrapovitsky.helper.DatabaseHelper;
import by.khrapovitsky.model.Note;

public class CreateNoteActivity extends AppCompatActivity implements View.OnClickListener{

    private Button createButton = null;
    private Button selectPhoto = null;
    private EditText noteText = null;
    private ImageView imageView = null;
    private String pathImage = null;
    private DatabaseHelper databaseHelper = new DatabaseHelper(this);

    private final static int REQUEST_CAMERA = 0, SELECT_FILE = 1;

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
            if (requestCode == SELECT_FILE){
                imageView.setImageBitmap(onSelectFromGalleryResult(data));
            }
            else if (requestCode == REQUEST_CAMERA){
                imageView.setImageBitmap(onCaptureImageResult(data));
            }
        }
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
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Bitmap onSelectFromGalleryResult(Intent data) {
        String selectedImagePath = getURIofImage(data);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        pathImage = selectedImagePath;
        return BitmapFactory.decodeFile(selectedImagePath, options);
    }

    private Bitmap onCaptureImageResult(Intent data){
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (thumbnail != null) {
            thumbnail.compress(Bitmap.CompressFormat.PNG, 50, bytes);
        }
        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".png");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pathImage = getURIofImage(data);
        return thumbnail;
    }

    private String getURIofImage(Intent data){
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
        int column_index = 0;
        String selectedImagePath = null;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        }
        return selectedImagePath;
    }

    public static class SelectPhotoDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final CharSequence[] items = { "Take Photo", "Choose from Gallery"};

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals("Take Photo")) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        getActivity().startActivityForResult(intent, REQUEST_CAMERA);
                    } else if (items[item].equals("Choose from Gallery")) {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        getActivity().startActivityForResult(
                                Intent.createChooser(intent, "Select File"),
                                SELECT_FILE);
                    }
                }
            });

            builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setTitle(R.string.selectPhoto);
            Dialog dialog = builder.create();
            return dialog;
        }
    }

}
