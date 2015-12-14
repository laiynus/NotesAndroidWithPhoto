package by.khrapovitsky.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import by.khrapovitsky.R;
import by.khrapovitsky.adapter.NotesCursorAdapter;
import by.khrapovitsky.dialog.AboutDialog;
import by.khrapovitsky.helper.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int REQ_CODE_CHILD_UPDATE = 1;
    public final static int REQ_CODE_CHILD_SETTINGS = 2;
    public final static int REQ_CODE_CHILD_CREATE = 3;

    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    NotesCursorAdapter adapter = null;
    Button createButton = null;
    Button requestButton = null;
    ListView notesListView = null;
    private  final String URL = "http://192.168.43.102:8080/servertime/time/getservertime";
    private String date;
    Cursor cursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        cursor = databaseHelper.getAllForAdapter();
        notesListView = (ListView) findViewById(R.id.NotesListView);
        adapter = new NotesCursorAdapter(this,R.layout.item_note,cursor,0);
        notesListView.setAdapter(adapter);
        createButton = (Button) findViewById(R.id.action_create);
        createButton.setOnClickListener(this);
        requestButton = (Button) findViewById(R.id.getServerTimeButton);
        requestButton.setOnClickListener(this);
        registerForContextMenu(notesListView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        View view = this.findViewById(android.R.id.content);
        view.setBackgroundColor(Color.parseColor(preferences.getString("backgroundColor", "WHITE")));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select action");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_listview, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        adapter.getCursor().moveToPosition(info.position);
        Integer id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        switch (item.getItemId()) {
            case R.id.action_delete:
                databaseHelper.deleteNote(databaseHelper.readNote(id));
                cursor = databaseHelper.getAllForAdapter();
                adapter.changeCursor(cursor);
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Note has successfully deleted", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_update:
                Intent intent = new Intent(this, UpdateNoteActivity.class);
                intent.putExtra("id",id.toString());
                startActivityForResult(intent, REQ_CODE_CHILD_UPDATE);
                return true;
            default:
                super.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.action_create:
                Intent intent = new Intent(this, CreateNoteActivity.class);
                startActivityForResult(intent, REQ_CODE_CHILD_CREATE);
                break;
            case R.id.getServerTimeButton:
                new AsyncHttpTask().execute(this.URL);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if(requestCode == REQ_CODE_CHILD_UPDATE) {
            switch (resultCode) {
                case RESULT_OK:
                    cursor = databaseHelper.getAllForAdapter();
                    adapter.changeCursor(cursor);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Note has successfully updated", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
        if(requestCode == REQ_CODE_CHILD_CREATE) {
            switch (resultCode) {
                case RESULT_OK:
                    cursor = databaseHelper.getAllForAdapter();
                    adapter.changeCursor(cursor);
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
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
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQ_CODE_CHILD_SETTINGS);
                return true;
            case R.id.action_about:
                AboutDialog aboutDialog = new AboutDialog();
                aboutDialog.show(getFragmentManager(), "About");
                return true;
            default:
                return true;
        }
    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            date = null;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                int statusCode = urlConnection.getResponseCode();
                if (statusCode ==  200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    parseResult(response);
                    result = 1;
                }else{
                    result = 0;
                }
            } catch (Exception e) {
                result = 0;
            }finally {
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result == 1){
                if(StringUtils.isBlank(date)){
                    Toast.makeText(getApplicationContext(), "Failed to get server time!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), date, Toast.LENGTH_LONG).show();
                }
            }else{
                if(result == 0){
                    Toast.makeText(getApplicationContext(), "Connection failed!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
            result += line;
        }
        if(inputStream!=null){
            inputStream.close();
        }
        return result;
    }

    private void parseResult(String result) throws JSONException {
        try{
            JSONObject response = new JSONObject(result);
            if(!StringUtils.isBlank(response.getString("date"))){
                date = response.getString("date");
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

}
