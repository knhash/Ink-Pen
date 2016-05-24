package knhash.mr.sqwamize;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int EDIT_ID = Menu.FIRST + 1;
    private TheAdapter mDbHelper;
    private SharedPreferences firstRuncheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if app is opened for the first time
        /*firstRuncheck = getSharedPreferences("firstRun", 0);
        boolean firstTime = firstRuncheck.getBoolean("firstTime", true);
        if (firstTime) {

            Intent intent = new Intent(MainActivity.this, Intro.class);
            startActivity(intent);
            finish();
        }*/

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNote();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        mDbHelper = new TheAdapter(this);
        mDbHelper.open();
        final ListView listview = (ListView) findViewById(R.id.list);
        fillData(listview);
        registerForContextMenu(listview);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                i.putExtra(TheAdapter.KEY_ROWID, id);
                startActivityForResult(i, ACTIVITY_EDIT);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.INSERT_QUICK) {
            //createNote();
            new MaterialDialog.Builder(this)
                    .title("Quick note")
                    .titleGravity(GravityEnum.CENTER)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("", "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            mDbHelper.open();
                            String title = input.toString();
                            if (title.equals("")) {
                                Toast.makeText(MainActivity.this, "Note Discarded", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Date date = new Date();
                            String Date= DateFormat.getDateInstance().format(date);
                            mDbHelper.createNote(title, "", Date, "");
                            ListView listview = (ListView) findViewById(R.id.list);
                            fillData(listview);
                        }
                    })
                    .positiveText("Create")
                    .negativeText("Cancel")
                    .show();
            return true;
        }

        if (id == R.id.ABOUT) {
            new MaterialDialog.Builder(this)
                    .title(R.string.about_title)
                    .content(R.string.about_content)
                    .titleGravity(GravityEnum.CENTER)
                    .contentGravity(GravityEnum.CENTER)
                    .icon(getResources().getDrawable(R.mipmap.ic_launcher))
                    .show();
            return true;
        }

        if (id == R.id.BACKUP) {
            exportDB();
            return true;
        }

        if (id == R.id.RESTORE) {
            importDB();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillData(ListView listview) {
        // Get all of the rows from the database and create the item list
        Cursor mNotesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{TheAdapter.KEY_TITLE,TheAdapter.KEY_DATE};

        // and an array of the fields we want to bind those fields to)
        int[] to = new int[]{R.id.text1,R.id.text3};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.note_row, mNotesCursor, from, to);
        listview.setAdapter(notes);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(2, DELETE_ID, 2, R.string.menu_delete);
        menu.add(1, EDIT_ID, 1, R.string.menu_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case DELETE_ID:
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                mNotifyMgr.cancel((int) info.id);
                mDbHelper.deleteNote(info.id);

                ListView listview = (ListView) findViewById(R.id.list);
                fillData(listview);
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                i.putExtra(TheAdapter.KEY_ROWID, info.id);
                startActivityForResult(i, ACTIVITY_EDIT);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, EditActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ListView listview = (ListView) findViewById(R.id.list);
        fillData(listview);
    }

    private void exportDB(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source;//=null;
        FileChannel destination;//=null;
        String currentDBPath = "/data/"+ "knhash.mr.sqwamize" +"/databases/"+"data";
        String backupDBPath = "/data/"+ "knhash.mr.sqwamize" +"/databases/"+"back-up";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        if (!backupDB.exists()) {
            if (!backupDB.getParentFile().mkdirs()) {
                Log.e("File Create Error", "Problem creating Backup file");
            }
        }
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "Notes backed-up", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void importDB(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source;//=null;
        FileChannel destination;//=null;
        String currentDBPath = "/data/"+ "knhash.mr.sqwamize" +"/databases/"+"data";
        String backupDBPath = "/data/"+ "knhash.mr.sqwamize" +"/databases/"+"back-up";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        if (!backupDB.exists()) {
            Toast.makeText(this, "Nothing to restore", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            source = new FileInputStream(backupDB).getChannel();
            destination = new FileOutputStream(currentDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            ListView listview = (ListView) findViewById(R.id.list);
            fillData(listview);
            Toast.makeText(this, "Notes restored", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
