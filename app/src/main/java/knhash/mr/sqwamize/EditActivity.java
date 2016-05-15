package knhash.mr.sqwamize;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

public class EditActivity extends AppCompatActivity {

    private TheAdapter mDbHelper;
    private EditText mTitleText;
    private EditText mBodyText;
    private TextView mCountText;
    private Long mRowId;
    public String notstat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDbHelper = new TheAdapter(this);
        mDbHelper.open();

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mCountText = (TextView) findViewById(R.id.counter);
        notstat = "unset";

        final FloatingActionButton fab_edit = (FloatingActionButton) findViewById(R.id.fab_edit);


        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                if(!mBodyText.isFocusableInTouchMode()){
                    mBodyText.setFocusableInTouchMode(true);
                    mTitleText.setFocusableInTouchMode(true);
                    fab_edit.hide();
                    Selection.setSelection(mBodyText.getText(),mBodyText.length());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mBodyText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(TheAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(TheAdapter.KEY_ROWID)
                    : null;
        }

        populateFields();

        if(mTitleText.getText().toString().equals("") || mBodyText.getText().toString().equals("")) {
            fab_edit.hide();
            mBodyText.setFocusableInTouchMode(true);
            mTitleText.setFocusableInTouchMode(true);
        }
        else
            fab_edit.show();

        //Prevent Keyboard on start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(TheAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(TheAdapter.KEY_BODY)));
            mCountText.setText(note.getString(
                    note.getColumnIndexOrThrow(TheAdapter.KEY_DATE)));
            notstat = note.getString(
                    note.getColumnIndexOrThrow(TheAdapter.KEY_UPDATE));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(TheAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        if(notstat.equals("set")) {
            notiminder();
        }

        Date date = new Date();
        String Date= DateFormat.getDateInstance().format(date);
        mCountText.setText(Date);

        String count = mCountText.getText().toString();

        if (mRowId == null && !title.equals("")) {
            long id = mDbHelper.createNote(title, body, count, notstat);
            if (id > 0) {
                mRowId = id;
            }

        }

        else if (title.equals("") && !body.equals("")){
            String temptitle = truncate(body, 30);
            long id = mDbHelper.createNote(temptitle, body, count, notstat);
            if (id > 0) {
                mRowId = id;
            }

        }

        else if (title.equals("") && body.equals("")){
            Toast.makeText(this, "Empty note discarded", Toast.LENGTH_SHORT).show();
        }

        else {
            mDbHelper.updateNote(mRowId, title, body, count, notstat);
        }
    }

    public String truncate(final String content, final int lastIndex) {
        String result = content.substring(0, lastIndex);
        if (content.charAt(lastIndex) != ' ') {
            result = result.substring(0, result.lastIndexOf(" "));
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public  boolean onPrepareOptionsMenu(Menu menu){
        if (mTitleText.getText().toString().equals("")) {
            menu.findItem(R.id.DENOTIFY).setVisible(false);
            menu.findItem(R.id.NOTIFY).setVisible(false);
        }

        else if(notstat.equals("set")) {
            menu.findItem(R.id.NOTIFY).setVisible(false);
            menu.findItem(R.id.DENOTIFY).setVisible(true);
        }
        else if(notstat.equals("unset")) {
            menu.findItem(R.id.DENOTIFY).setVisible(false);
            menu.findItem(R.id.NOTIFY).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.SHARE) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "  " + mTitleText.getText().toString() + "\n\n");
            sendIntent.putExtra(Intent.EXTRA_TEXT, mBodyText.getText().toString());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }

        if (id == R.id.DELETE) {
            mDbHelper.deleteNote(mRowId);
            finish();
            notstat = "unset";
            notiminder();
            Toast.makeText(this, "Note Discarded", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.NOTIFY) {
            saveState();
            notstat = "set";
            notiminder();
        }

        if (id == R.id.DENOTIFY) {
            saveState();
            notstat = "unset";
            notiminder();
        }

        return super.onOptionsItemSelected(item);
    }

    public void notiminder(){

        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(TheAdapter.KEY_ROWID, mRowId);

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(mTitleText.getText().toString())
                .setContentText(mBodyText.getText().toString())
                .setSmallIcon(R.drawable.ic_done_white_36dp)
                .setOngoing(true)
                .setAutoCancel(false);

        PendingIntent pIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pIntent);

        int nNotificationId = mRowId.intValue();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(nNotificationId, mBuilder.build());

        if(notstat.equals("unset")) {
            mNotifyMgr.cancel(mRowId.intValue());
        }

        /*// prepare intent which is triggered if the notification is selected

        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(TheAdapter.KEY_ROWID, mRowId);

        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // build notification
        Notification n  = new Notification.Builder(this)
                .setContentTitle(mTitleText.getText().toString())
                .setContentText(mBodyText.getText().toString())
                .setSmallIcon(R.drawable.ic_done_white_36dp)
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .build();

        n.flags |= Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(mRowId.intValue(), n);

        if(notstat.equals("unset")) {
            notificationManager.cancel(mRowId.intValue());
        }*/
    }

}
