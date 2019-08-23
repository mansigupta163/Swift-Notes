package com.moonpi.swiftnotes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;

import static com.moonpi.swiftnotes.DataUtils.BACKUP_FILE_NAME;
import static com.moonpi.swiftnotes.DataUtils.BACKUP_FOLDER_PATH;
import static com.moonpi.swiftnotes.DataUtils.NEW_NOTE_REQUEST;
import static com.moonpi.swiftnotes.DataUtils.NOTES_FILE_NAME;
import static com.moonpi.swiftnotes.DataUtils.NOTE_BODY;
import static com.moonpi.swiftnotes.DataUtils.NOTE_COLOUR;
import static com.moonpi.swiftnotes.DataUtils.NOTE_FAVOURED;
import static com.moonpi.swiftnotes.DataUtils.NOTE_FONT_SIZE;
import static com.moonpi.swiftnotes.DataUtils.NOTE_HIDE_BODY;
import static com.moonpi.swiftnotes.DataUtils.NOTE_REQUEST_CODE;
import static com.moonpi.swiftnotes.DataUtils.NOTE_TITLE;
import static com.moonpi.swiftnotes.DataUtils.deleteNotes;
import static com.moonpi.swiftnotes.DataUtils.isExternalStorageReadable;
import static com.moonpi.swiftnotes.DataUtils.isExternalStorageWritable;
import static com.moonpi.swiftnotes.DataUtils.retrieveData;
import static com.moonpi.swiftnotes.DataUtils.saveData;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
         AbsListView.MultiChoiceModeListener,
        SearchView.OnQueryTextListener
{

    FirebaseAuth mAuth;
    DatabaseReference mdatabase,mkeydatabase;
    FirebaseUser currentuser;

    String body;

    Intent shareintent;
    ProgressDialog pd;
    private static File localPath, backupPath;

    // Layout components
    private static ListView listView;
    private ImageButton newNote;
    private TextView noNotes;
     Toolbar mtoolbar;
    private MenuItem searchMenu;
    private String user_id;
    private static JSONArray notes; // Main notes array
    private static NoteAdapter adapter; // Custom ListView notes adapter
    String pdfKaNaam;

    // Array of selected positions for deletion
    public static ArrayList<Integer> checkedArray = new ArrayList<Integer>();
    public static boolean deleteActive = false; // True if delete mode is active, false otherwise

    // For disabling long clicks, favourite clicks and modifying the item click pattern
    public static boolean searchActive = false;
    private ArrayList<Integer> realIndexesOfSearchResults; // To keep track of real indexes in searched notes

    private int lastFirstVisibleItem = -1; // Last first item seen in list view scroll changed
    private float newNoteButtonBaseYCoordinate; // Base Y coordinate of newNote button

    private AlertDialog backupCheckDialog, backupOKDialog, restoreCheckDialog, restoreFailedDialog;

    private DrawerLayout mdrawerlayout;
    private ActionBarDrawerToggle mtoggle;    //for toggle button to open the naviagtion side drawer
    private FabSpeedDial fbs;

    ArrayList<String> base;
    ArrayList<Bitmap> photosList;
    String imageString;

    FileOutputStream fo;

    String userChoosenTask;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    String successmessage;

    int status=0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        base = new ArrayList<>();
        photosList = new ArrayList<>();

        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        mkeydatabase=FirebaseDatabase.getInstance().getReference().child("KEY");
        mdatabase= FirebaseDatabase.getInstance().getReference().child("User");
        if(currentuser==null) //if any user is not logged in tha it will take the user from main activity to start activity
        {
            status=0;
            sendtostart();

        }

        pd=new ProgressDialog(MainActivity.this);

       /* fbs=(FabSpeedDial)findViewById(R.id.fabdial);
        fbs.bringToFront();
        fbs.show();

        fbs.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {

                Toast.makeText(MainActivity.this,menuItem.getTitle().toString(),Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        }); */

        // Initialize local file path and backup file path

        Intent intent=getIntent();
        if(intent!=null)
        {
            String method=intent.getStringExtra("classFrom");
            if(method!=null) {
                if (method.equals("FingerprintHandler")) {
                    status = 1;
                } else if (method.equals("LoginActitivty")) {
                    status = 0;
                } else if (method.equals("RegisterActivity")) {
                    status = 0;
                }
            }
        }
        else
        {

        }

        if(status==0) {
            successmessage = "null";
        }
        else if(status==1) {
            successmessage = getIntent().getStringExtra("message"); //if fingerprint is successful
        }
        if(successmessage.equals("success"))
        {
           /* android.support.v7.app.AlertDialog.Builder builder= new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            View mview=getLayoutInflater().inflate(R.layout.decryptiondialog,null);
            final EditText k=(EditText)mview.findViewById(R.id.decrytext);
            Button submit=(Button)mview.findViewById(R.id.decrybutton);
            builder.setCancelable(true);
            builder.setView(mview);
            final android.support.v7.app.AlertDialog alertDialog=builder.create();
            alertDialog.show();*/
            final String id=currentuser.getUid();

          /*  submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {*/

                    pd.setTitle("Decryption");
                    pd.setMessage("Please wait while we are decrypting the text....... ");
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();
                   // final String key=k.getText().toString();  //key from edit text box(from user)
                    mkeydatabase.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.hasChild("decryptkey"))
                            {
                                String backendkey=dataSnapshot.child("decryptkey").getValue().toString();
                                //String arr[]=new String[100];
                                //int k=0;
                                //if(backendkey.equals(key))
                              //  {
                                    //alertDialog.dismiss();
                                    for(int i=0;i<notes.length();i++)
                                    {
                                        try {
                                            JSONObject jobject=notes.getJSONObject(i);
                                            //Toast.makeText(getApplicationContext(),"Title: "+jobject.getString("title").toString(),Toast.LENGTH_LONG).show();
                                            // Toast.makeText(getApplicationContext(),"Body: "+jobject.getString("body").toString(),Toast.LENGTH_LONG).show();
                                            int s=Integer.parseInt(backendkey);
                                            body=jobject.getString("body").toString();
                                            String title=jobject.getString("title").toString();
                                            StringBuffer result_body=Encrypt.encrypt(body,26-s);
                                            StringBuffer result_title=Encrypt.encrypt(title,26-s);
                                            //final int i1 = Integer.parseInt(encryptedtext) + Integer.parseInt(String.valueOf(result));
                                            Toast.makeText(getApplicationContext(),"Decrypted title: "+result_title,Toast.LENGTH_LONG).show();
                                            Toast.makeText(getApplicationContext(),"Decrypted body: "+result_body,Toast.LENGTH_LONG).show();
                                           // arr[k]=result_body.toString();
                                            //k++;
                                            pd.dismiss();


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    //Intent movetodecryptedlist=new Intent(MainActivity.this, DecryptedList.class);
                                    //movetodecryptedlist.putExtra("body",arr);
                                //startActivity(movetodecryptedlist);
                             //   }
                               /* else
                                {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(),"Please enter a valid key",Toast.LENGTH_LONG).show();
                                }*/
                            }
                            else
                            {
                                pd.hide();
                                Toast.makeText(getApplicationContext(),"First set decryption key",Toast.LENGTH_LONG).show();
                            }

                        }


                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
          //  });

       // }
        localPath = new File(getFilesDir() + "/" + NOTES_FILE_NAME);

        File backupFolder = new File(Environment.getExternalStorageDirectory() +
                BACKUP_FOLDER_PATH);

        if (isExternalStorageReadable() && isExternalStorageWritable() && !backupFolder.exists())
            backupFolder.mkdir();

        backupPath = new File(backupFolder, BACKUP_FILE_NAME);

        // Android version >= 18 -> set orientation userPortrait
        if (Build.VERSION.SDK_INT >= 18)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);

        // Android version < 18 -> set orientation sensorPortrait
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // Init notes array
        notes = new JSONArray();

        // Retrieve from local path
        JSONArray tempNotes = retrieveData(localPath);

        // If not null -> equal main notes to retrieved notes
        if (tempNotes != null)
            notes = tempNotes;

        setContentView(R.layout.activity_main);

        // Init layout components
        mtoolbar = (Toolbar)findViewById(R.id.toolbarMain);
        //setSupportActionBar(toolbar);
        listView = (ListView)findViewById(R.id.listView);
        newNote = (ImageButton)findViewById(R.id.newNote);
        noNotes = (TextView)findViewById(R.id.noNotes);

        if (mtoolbar != null)
            initToolbar();
        navigationsidebar();
        newNoteButtonBaseYCoordinate = newNote.getY();     //Problem here...............................

        // Initialize NoteAdapter with notes array
        adapter = new NoteAdapter(getApplicationContext(), notes);
        listView.setAdapter(adapter);

        // Set item click, multi choice and scroll listeners
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // If last first visible item not initialized -> set to current first
                if (lastFirstVisibleItem == -1)
                    lastFirstVisibleItem = view.getFirstVisiblePosition();

                // If scrolled up -> hide newNote button
                if (view.getFirstVisiblePosition() > lastFirstVisibleItem)
                    newNoteButtonVisibility(false);

                // If scrolled down and delete/search not active -> show newNote button
                else if (view.getFirstVisiblePosition() < lastFirstVisibleItem &&
                        !deleteActive && !searchActive) {

                    newNoteButtonVisibility(true);
                }

                // Set last first visible item to current
                lastFirstVisibleItem = view.getFirstVisiblePosition();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {}
        });


        // If newNote button clicked -> Start EditActivity intent with NEW_NOTE_REQUEST as request
        newNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra(NOTE_REQUEST_CODE, NEW_NOTE_REQUEST);

                startActivityForResult(intent, NEW_NOTE_REQUEST);
            }
        });

        // If no notes -> show 'Press + to add new note' text, invisible otherwise
        if (notes.length() == 0)
            noNotes.setVisibility(View.VISIBLE);

        else
            noNotes.setVisibility(View.INVISIBLE);

        initDialogs(this);
    }

    private void navigationsidebar()
    {

       mdrawerlayout=(DrawerLayout)findViewById(R.id.drawablelayout);
        mtoggle=new ActionBarDrawerToggle(this,mdrawerlayout,R.string.open,R.string.close);

        mdrawerlayout.addDrawerListener(mtoggle);
        mtoggle.syncState();   //displays three horizontal lines as a toggle button

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //to display the toggle button on action bar
    }


   public boolean onOptionsItemSelected(MenuItem item)
    {
        if(mtoggle.onOptionsItemSelected(item))
        {
            return true;
        }

        int id = item.getItemId();

        // 'Backup notes' pressed -> show backupCheckDialog
        if (id == R.id.action_backup) {
            backupCheckDialog.show();
            return true;
        }

        // 'Restore notes' pressed -> show restoreCheckDialog
        if (id == R.id.action_restore) {
            restoreCheckDialog.show();
            return true;
        }
        if(id==R.id.logout)
        {

            currentuser=mAuth.getCurrentUser();
            if(currentuser!=null)
            {
                mdatabase.child(currentuser.getUid()).child("online").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        mdatabase.child(currentuser.getUid()).child("online").setValue("false");
                        mAuth.getInstance().signOut();

                        sendtostart();
                    }
                });

            }

        }

        // 'Rate app' pressed -> create new dialog to ask the user if he wants to go to the PlayStore
        // If yes -> start PlayStore and go to app link < If Exception thrown, open in Browser >
        if (id == R.id.action_rate_app) {
            final String appPackageName = getPackageName();

            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_rate_title)
                    .setMessage(R.string.dialog_rate_message)
                    .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + appPackageName)));

                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id="
                                                + appPackageName)));
                            }
                        }
                    })
                    .setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            return true;
        }

           return false;

    }




    private void sendtostart() //taking to start activity
    {
     Intent start_activity=new Intent(this,StartActivity.class);
        start_activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(start_activity);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main,menu);

      return true;



    }

    /**
     * Initialize toolbar with required components such as
     * - title, menu/OnMenuItemClickListener and searchView -
     */
    protected void initToolbar()
    {
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().show();
        //toolbar.setTitle(R.string.app_name);


        // Inflate menu_main to be displayed in the toolbar
        /* mtoolbar.inflateMenu(R.menu.menu_main);

        // Set an OnMenuItemClickListener to handle menu item clicks
        mtoolbar.setOnMenuItemClickListener(this);

        Menu menu = mtoolbar.getMenu();

        if (menu != null) {
            // Get 'Search' menu item
            searchMenu = menu.findItem(R.id.action_search);

            if (searchMenu != null) {
                // If the item menu not null -> get it's support action view
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);

                if (searchView != null) {
                    // If searchView not null -> set query hint and open/query/close listeners
                    searchView.setQueryHint(getString(R.string.action_search));
                    searchView.setOnQueryTextListener(this);

                    MenuItemCompat.setOnActionExpandListener(searchMenu,
                            new MenuItemCompat.OnActionExpandListener() {

                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            searchActive = true;
                            newNoteButtonVisibility(false);
                            // Disable long-click on listView to prevent deletion
                            listView.setLongClickable(false);

                            // Init realIndexes array
                            realIndexesOfSearchResults = new ArrayList<Integer>();
                            for (int i = 0; i < notes.length(); i++)
                                realIndexesOfSearchResults.add(i);

                            adapter.notifyDataSetChanged();

                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            searchEnded();
                            return true;
                        }
                    });
                }
            }
        }*/
    }


    /**
     * Implementation of AlertDialogs such as
     * - backupCheckDialog, backupOKDialog, restoreCheckDialog, restoreFailedDialog -
     * @param context The Activity context of the dialogs; in this case MainActivity context
     */
    protected void initDialogs(Context context) {
        /*
         * Backup check dialog
         *  If not sure -> dismiss
         *  If yes -> check if notes length > 0
         *    If yes -> save current notes to backup file in backupPath
         */
        backupCheckDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.action_backup)
                .setMessage(R.string.dialog_check_backup_if_sure)
                .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If note array not empty -> continue
                        if (notes.length() > 0) {
                            boolean backupSuccessful = saveData(backupPath, notes);

                            if (backupSuccessful)
                                showBackupSuccessfulDialog();

                            else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.toast_backup_failed),
                                        Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                        // If notes array is empty -> toast backup no notes found
                        else {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.toast_backup_no_notes),
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                })
                .setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();


        // Dialog to display backup was successfully created in backupPath
        backupOKDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_backup_created_title)
                .setMessage(getString(R.string.dialog_backup_created) + " "
                        + backupPath.getAbsolutePath())
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();


        /*
         * Restore check dialog
         *  If not sure -> dismiss
         *  If yes -> check if backup notes exists
         *    If not -> display restore failed dialog
         *    If yes -> retrieve notes from backup file and store into local file
         */
        restoreCheckDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.action_restore)
                .setMessage(R.string.dialog_check_restore_if_sure)
                .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONArray tempNotes = retrieveData(backupPath);

                        // If backup file exists -> copy backup notes to local file
                        if (tempNotes != null) {
                            boolean restoreSuccessful = saveData(localPath, tempNotes);

                            if (restoreSuccessful) {
                                notes = tempNotes;

                                adapter = new NoteAdapter(getApplicationContext(), notes);
                                listView.setAdapter(adapter);

                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.toast_restore_successful),
                                        Toast.LENGTH_SHORT);
                                toast.show();

                                // If no notes -> show 'Press + to add new note' text, invisible otherwise
                                if (notes.length() == 0)
                                    noNotes.setVisibility(View.VISIBLE);

                                else
                                    noNotes.setVisibility(View.INVISIBLE);
                            }

                            // If restore unsuccessful -> toast restore unsuccessful
                            else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.toast_restore_unsuccessful),
                                        Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                        // If backup file doesn't exist -> show restore failed dialog
                        else
                            showRestoreFailedDialog();
                    }
                })
                .setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();


        // Dialog to display restore failed when no backup file found
        restoreFailedDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_restore_failed_title)
                .setMessage(R.string.dialog_restore_failed)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    // Method to dismiss backup check and show backup successful dialog
    protected void showBackupSuccessfulDialog() {
        backupCheckDialog.dismiss();
        backupOKDialog.show();
    }

    // Method to dismiss restore check and show restore failed dialog
    protected void showRestoreFailedDialog() {
        restoreCheckDialog.dismiss();
        restoreFailedDialog.show();
    }


    /**
     * If item clicked in list view -> Start EditActivity intent with position as requestCode
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        // If search is active -> use position from realIndexesOfSearchResults for EditActivity
        if (searchActive) {
            int newPosition = realIndexesOfSearchResults.get(position);

            try {
                // Package selected note content and send to EditActivity
                intent.putExtra(NOTE_TITLE, notes.getJSONObject(newPosition).getString(NOTE_TITLE));
                intent.putExtra(NOTE_BODY, notes.getJSONObject(newPosition).getString(NOTE_BODY));
                intent.putExtra(NOTE_COLOUR, notes.getJSONObject(newPosition).getString(NOTE_COLOUR));
                intent.putExtra(NOTE_FONT_SIZE, notes.getJSONObject(newPosition).getInt(NOTE_FONT_SIZE));

                if (notes.getJSONObject(newPosition).has(NOTE_HIDE_BODY)) {
                    intent.putExtra(NOTE_HIDE_BODY,
                            notes.getJSONObject(newPosition).getBoolean(NOTE_HIDE_BODY));
                }

                else
                    intent.putExtra(NOTE_HIDE_BODY, false);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            intent.putExtra(NOTE_REQUEST_CODE, newPosition);
            startActivityForResult(intent, newPosition);
        }

        // If search is not active -> use normal position for EditActivity
        else {
            try {
                // Package selected note content and send to EditActivity
                intent.putExtra(NOTE_TITLE, notes.getJSONObject(position).getString(NOTE_TITLE));
                intent.putExtra(NOTE_BODY, notes.getJSONObject(position).getString(NOTE_BODY));
                intent.putExtra(NOTE_COLOUR, notes.getJSONObject(position).getString(NOTE_COLOUR));
                intent.putExtra(NOTE_FONT_SIZE, notes.getJSONObject(position).getInt(NOTE_FONT_SIZE));

                if (notes.getJSONObject(position).has(NOTE_HIDE_BODY)) {
                    intent.putExtra(NOTE_HIDE_BODY,
                            notes.getJSONObject(position).getBoolean(NOTE_HIDE_BODY));
                }

                else
                    intent.putExtra(NOTE_HIDE_BODY, false);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            intent.putExtra(NOTE_REQUEST_CODE, position);
            startActivityForResult(intent, position);
        }
    }




   /* public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();

        // 'Backup notes' pressed -> show backupCheckDialog
        if (id == R.id.action_backup) {
            backupCheckDialog.show();
            return true;
        }

        // 'Restore notes' pressed -> show restoreCheckDialog
        if (id == R.id.action_restore) {
            restoreCheckDialog.show();
            return true;
        }
        if(id==R.id.logout)
        {

           currentuser=mAuth.getCurrentUser();
            if(currentuser!=null)
            {
                mdatabase.child(currentuser.getUid()).child("online").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        mdatabase.child(currentuser.getUid()).child("online").setValue("false");
                        mAuth.getInstance().signOut();

                        sendtostart();
                    }
                });

            }

        }

        // 'Rate app' pressed -> create new dialog to ask the user if he wants to go to the PlayStore
        // If yes -> start PlayStore and go to app link < If Exception thrown, open in Browser >
        if (id == R.id.action_rate_app) {
            final String appPackageName = getPackageName();

            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_rate_title)
                    .setMessage(R.string.dialog_rate_message)
                    .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + appPackageName)));

                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id="
                                                + appPackageName)));
                            }
                        }
                    })
                    .setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            return true;
        }

        return false;
    }  */


    /**
     * During multi-choice menu_delete selection mode, callback method if items checked changed
     * @param mode ActionMode of selection
     * @param position Position checked
     * @param id ID of item, if exists
     * @param checked true if checked, false otherwise
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // If item checked -> add to array
        if (checked)
            checkedArray.add(position);

        // If item unchecked
        else {
            int index = -1;

            // Loop through array and find index of item unchecked
            for (int i = 0; i < checkedArray.size(); i++) {
                if (position == checkedArray.get(i)) {
                    index = i;
                    break;
                }
            }

            // If index was found -> remove the item
            if (index != -1)
                checkedArray.remove(index);
        }

        // Set Toolbar title to 'x Selected'
        mode.setTitle(checkedArray.size() + " " + getString(R.string.action_delete_selected_number));
        adapter.notifyDataSetChanged();
    }

    /**
     * Callback method when 'Delete' icon pressed
     * @param mode ActionMode of selection
     * @param item MenuItem clicked, in our case just action_delete
     * @return true if clicked, false otherwise
     */
    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item)
    {
        if (item.getItemId() == R.id.action_delete)
        {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_delete)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Pass notes and checked items for deletion array to 'deleteNotes'
                           /* try {
                                JSONObject jo=notes.getJSONObject(Integer.parseInt(checkedArray.totoString()));
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }*/

                            notes = deleteNotes(notes, checkedArray);
                               Toast.makeText(getApplicationContext(),String.valueOf(checkedArray),Toast.LENGTH_LONG).show();

                            // Create and set new adapter with new notes array
                            adapter = new NoteAdapter(getApplicationContext(), notes);
                            listView.setAdapter(adapter);

                            // Attempt to save notes to local file
                            Boolean saveSuccessful = saveData(localPath, notes);

                            // If save successful -> toast successfully deleted
                            if (saveSuccessful) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.toast_deleted),
                                        Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            // Smooth scroll to top
                            listView.post(new Runnable() {
                                public void run() {
                                    listView.smoothScrollToPosition(0);
                                }
                            });

                            // If no notes -> show 'Press + to add new note' text, invisible otherwise
                            if (notes.length() == 0)
                                noNotes.setVisibility(View.VISIBLE);

                            else
                                noNotes.setVisibility(View.INVISIBLE);

                            mode.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            return true;
        }
        //Decryption part.....................................................

        if(item.getItemId()==R.id.decryptionkey)
        {
            //Toast.makeText(getApplicationContext(),"Hello",Toast.LENGTH_LONG).show();
            Intent authenticateyourself=new Intent(MainActivity.this,FingerprintAuthentication.class);
            startActivity(authenticateyourself);

            /*android.support.v7.app.AlertDialog.Builder builder= new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            View mview=getLayoutInflater().inflate(R.layout.decryptiondialog,null);
            final EditText k=(EditText)mview.findViewById(R.id.decrytext);
            Button submit=(Button)mview.findViewById(R.id.decrybutton);
            builder.setCancelable(true);
            builder.setView(mview);
            final android.support.v7.app.AlertDialog alertDialog=builder.create();
            alertDialog.show();
            final String id=currentuser.getUid();

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {

                    pd.setTitle("Decryption");
                    pd.setMessage("Please wait while we are decrypting the text....... ");
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();
                    final String key=k.getText().toString();  //key from edit text box(from user)
                    mkeydatabase.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.hasChild("decryptkey"))
                            {
                                String backendkey=dataSnapshot.child("decryptkey").getValue().toString();
                                if(backendkey.equals(key))
                                {
                                    alertDialog.dismiss();
                                    for(int i=0;i<notes.length();i++)
                                    {
                                        try {
                                            JSONObject jobject=notes.getJSONObject(i);
                                            //Toast.makeText(getApplicationContext(),"Title: "+jobject.getString("title").toString(),Toast.LENGTH_LONG).show();
                                           // Toast.makeText(getApplicationContext(),"Body: "+jobject.getString("body").toString(),Toast.LENGTH_LONG).show();
                                               int s=Integer.parseInt(key);
                                                body=jobject.getString("body").toString();
                                                String title=jobject.getString("title").toString();
                                                StringBuffer result_body=Encrypt.encrypt(body,26-s);
                                            StringBuffer result_title=Encrypt.encrypt(title,26-s);
                                            //final int i1 = Integer.parseInt(encryptedtext) + Integer.parseInt(String.valueOf(result));
                                            Toast.makeText(getApplicationContext(),"Decrypted title: "+result_title,Toast.LENGTH_LONG).show();
                                            Toast.makeText(getApplicationContext(),"Decrypted body: "+result_body,Toast.LENGTH_LONG).show();
                                            pd.dismiss();


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }

                                else
                                {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(),"Please enter a valid key",Toast.LENGTH_LONG).show();
                                }
                            }
                            else
                            {
                                pd.hide();
                                Toast.makeText(getApplicationContext(),"First set decryption key",Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }); */

        }

        if(item.getItemId()==R.id.share)  //note sharing
        {

                shareintent=new Intent(Intent.ACTION_SEND);
                shareintent.setType("text/plain");
            shareintent.putExtra(Intent.EXTRA_SUBJECT,"NOTES");
            shareintent.putExtra(Intent.EXTRA_TEXT,body);
            startActivity(Intent.createChooser(shareintent,"Share via"));
        }

        return false;
    }

    // Long click detected on ListView item -> start selection ActionMode (delete mode)
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_delete, menu); // Inflate 'menu_delete' menu
        deleteActive = true; // Set deleteActive to true as we entered delete mode
        newNoteButtonVisibility(false); // Hide newNote button
        newNote.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged(); // Notify adapter to hide favourite buttons

        return true;
    }

    // Selection ActionMode finished (delete mode ended)
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        checkedArray = new ArrayList<Integer>(); // Reset checkedArray
        deleteActive = false; // Set deleteActive to false as we finished delete mode
        newNoteButtonVisibility(true); // Show newNote button
        adapter.notifyDataSetChanged(); // Notify adapter to show favourite buttons
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }


    /**
     * Method to show and hide the newNote button
     * @param isVisible true to show button, false to hide
     */
    protected void newNoteButtonVisibility(boolean isVisible) {
        if (isVisible) {
            newNote.animate().cancel();
            newNote.animate().translationY(newNoteButtonBaseYCoordinate);
        } else {
            newNote.animate().cancel();
            newNote.animate().translationY(newNoteButtonBaseYCoordinate + 500);
        }
    }


    /**
     * Callback method for 'searchView' menu item widget text change
     * @param s String which changed
     * @return true if text changed and logic finished, false otherwise
     */
    @Override
    public boolean onQueryTextChange(String s) {
        s = s.toLowerCase(); // Turn string into lowercase

        // If query text length longer than 0
        if (s.length() > 0) {
            // Create new JSONArray and reset realIndexes array
            JSONArray notesFound = new JSONArray();
            realIndexesOfSearchResults = new ArrayList<Integer>();

            // Loop through main notes list
            for (int i = 0; i < notes.length(); i++) {
                JSONObject note = null;

                // Get note at position i
                try {
                    note = notes.getJSONObject(i);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // If note not null and title/body contain query text
                // -> Put in new notes array and add i to realIndexes array
                if (note != null) {
                    try {
                        if (note.getString(NOTE_TITLE).toLowerCase().contains(s) ||
                            note.getString(NOTE_BODY).toLowerCase().contains(s)) {

                            notesFound.put(note);
                            realIndexesOfSearchResults.add(i);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Create and set adapter with notesFound to refresh ListView
            NoteAdapter searchAdapter = new NoteAdapter(getApplicationContext(), notesFound);
            listView.setAdapter(searchAdapter);
        }

        // If query text length is 0 -> re-init realIndexes array (0 to length) and reset adapter
        else {
            realIndexesOfSearchResults = new ArrayList<Integer>();
            for (int i = 0; i < notes.length(); i++)
                realIndexesOfSearchResults.add(i);

            adapter = new NoteAdapter(getApplicationContext(), notes);
            listView.setAdapter(adapter);
        }

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }


    /**
     * When search mode is finished
     * Collapse searchView widget, searchActive to false, reset adapter, enable listView long clicks
     * and show newNote button
     */
    protected void searchEnded() {
        searchActive = false;
        adapter = new NoteAdapter(getApplicationContext(), notes);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        newNoteButtonVisibility(true);
    }


    /**
     * Callback method when EditActivity finished adding new note or editing existing note
     * @param requestCode requestCode for intent sent, in our case either NEW_NOTE_REQUEST or position
     * @param resultCode resultCode from activity, either RESULT_OK or RESULT_CANCELED
     * @param data Data bundle passed back from EditActivity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_FILE)
                try {
                    onSelectFromGalleryResult(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);

            // If search was active -> call 'searchEnded' method
            if (searchActive && searchMenu != null)
                searchMenu.collapseActionView();

            // Get extras
            Bundle mBundle = null;
            if (data != null)
                mBundle = data.getExtras();

            if (mBundle != null) {
                // If new note was saved
                if (requestCode == NEW_NOTE_REQUEST) {
                    JSONObject newNoteObject = null;

                    try {
                        // Add new note to array
                        newNoteObject = new JSONObject();
                        newNoteObject.put(NOTE_TITLE, mBundle.getString(NOTE_TITLE));
                        newNoteObject.put(NOTE_BODY, mBundle.getString(NOTE_BODY));
                        newNoteObject.put(NOTE_COLOUR, mBundle.getString(NOTE_COLOUR));
                        newNoteObject.put(NOTE_FAVOURED, false);
                        newNoteObject.put(NOTE_FONT_SIZE, mBundle.getInt(NOTE_FONT_SIZE));
                        newNoteObject.put(NOTE_HIDE_BODY, mBundle.getBoolean(NOTE_HIDE_BODY));

                        notes.put(newNoteObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // If newNoteObject not null -> save notes array to local file and notify adapter
                    if (newNoteObject != null) {
                        adapter.notifyDataSetChanged();

                        Boolean saveSuccessful = saveData(localPath, notes);

                        if (saveSuccessful) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.toast_new_note),
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        // If no notes -> show 'Press + to add new note' text, invisible otherwise
                        if (notes.length() == 0)
                            noNotes.setVisibility(View.VISIBLE);

                        else
                            noNotes.setVisibility(View.INVISIBLE);
                    }
                }

                // If existing note was updated (saved)
                else {
                    JSONObject newNoteObject = null;

                    try {
                        // Update array item with new note data
                        newNoteObject = notes.getJSONObject(requestCode);
                        newNoteObject.put(NOTE_TITLE, mBundle.getString(NOTE_TITLE));
                        newNoteObject.put(NOTE_BODY, mBundle.getString(NOTE_BODY));
                        newNoteObject.put(NOTE_COLOUR, mBundle.getString(NOTE_COLOUR));
                        newNoteObject.put(NOTE_FONT_SIZE, mBundle.getInt(NOTE_FONT_SIZE));
                        newNoteObject.put(NOTE_HIDE_BODY, mBundle.getBoolean(NOTE_HIDE_BODY));

                        // Update note at position 'requestCode'
                        notes.put(requestCode, newNoteObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // If newNoteObject not null -> save notes array to local file and notify adapter
                    if (newNoteObject != null) {
                        adapter.notifyDataSetChanged();

                        Boolean saveSuccessful = saveData(localPath, notes);

                        if (saveSuccessful) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.toast_note_saved),
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
            }
        }


        else if (resultCode == RESULT_CANCELED) {
            Bundle mBundle = null;

            // If data is not null, has "request" extra and is new note -> get extras to bundle
            if (data != null && data.hasExtra("request") && requestCode == NEW_NOTE_REQUEST) {
                mBundle = data.getExtras();

                // If new note discarded -> toast empty note discarded
                if (mBundle != null && mBundle.getString("request").equals("discard")) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_empty_note_discarded),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Favourite or un-favourite the note at position
     * @param context application context
     * @param favourite true to favourite, false to un-favourite
     * @param position position of note
     */
    public static void setFavourite(Context context, boolean favourite, int position) {
        JSONObject newFavourite = null;

        // Get note at position and store in newFavourite
        try {
            newFavourite = notes.getJSONObject(position);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (newFavourite != null) {
            if (favourite) {
                // Set favoured to true
                try {
                    newFavourite.put(NOTE_FAVOURED, true);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // If favoured note is not at position 0
                // Sort notes array so favoured note is first
                if (position > 0) {
                    JSONArray newArray = new JSONArray();

                    try {
                        newArray.put(0, newFavourite);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Copy contents to new sorted array without favoured element
                    for (int i = 0; i < notes.length(); i++) {
                        if (i != position) {
                            try {
                                newArray.put(notes.get(i));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Equal main notes array with new sorted array and reset adapter
                    notes = newArray;
                    adapter = new NoteAdapter(context, notes);
                    listView.setAdapter(adapter);

                    // Smooth scroll to top
                    listView.post(new Runnable() {
                        public void run() {
                            listView.smoothScrollToPosition(0);
                        }
                    });
                }

                // If favoured note was first -> just update object in notes array and notify adapter
                else {
                    try {
                        notes.put(position, newFavourite);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            // If note not favourite -> set favoured to false and notify adapter
            else {
                try {
                    newFavourite.put(NOTE_FAVOURED, false);
                    notes.put(position, newFavourite);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapter.notifyDataSetChanged();
            }

            // Save notes to local file
            saveData(localPath, notes);
        }
    }


    /**
     * If back button pressed while search is active -> collapse view and end search mode
     */
    @Override
    public void onBackPressed() {
        if (searchActive && searchMenu != null) {
            searchMenu.collapseActionView();
            return;
        }

        super.onBackPressed();
    }


    /**
     * Orientation changed callback method
     * If orientation changed -> If any AlertDialog is showing, dismiss it to prevent WindowLeaks
     * @param newConfig New Configuration passed by system
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (backupCheckDialog != null && backupCheckDialog.isShowing())
            backupCheckDialog.dismiss();

        if (backupOKDialog != null && backupOKDialog.isShowing())
            backupOKDialog.dismiss();

        if (restoreCheckDialog != null && restoreCheckDialog.isShowing())
            restoreCheckDialog.dismiss();

        if (restoreFailedDialog != null && restoreFailedDialog.isShowing())
            restoreFailedDialog.dismiss();

        super.onConfigurationChanged(newConfig);
    }


    // Static method to return File at localPath
    public static File getLocalPath() {
        return localPath;
    }

    // Static method to return File at backupPath
    public static File getBackupPath() {
        return backupPath;
    }


    public void exit(MenuItem item)
    {
        item.setCheckable(true);
        finish();
        System.exit(1);

    }



    public void settings(MenuItem item)
    {

     Intent intent=new Intent(MainActivity.this,Settings.class);
        startActivity(intent);
    }


    public void details(MenuItem item)
    {
         Intent detail=new Intent(MainActivity.this,PersonalDetails.class);
        startActivity(detail);
    }


    public void users(MenuItem item)
    {
        Intent user=new Intent(MainActivity.this,UsersActivity.class);
        startActivity(user);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    public void selectImage(MenuItem menuItem) {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(MainActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void downloadpdf()
    {
        Log.d("downloadPDF", "downloadpdf: ");
        final DownloadTask downloadTask = new DownloadTask(MainActivity.this, pdfKaNaam);
        downloadTask.execute("http://192.168.43.248:5211/downloadPDF");
        Toast.makeText(MainActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();

    }

    private void hitapi(String img)
    {
        Log.d("hitAPI", "onClick: " + img);
        JSONObject json = new JSONObject();
        try {
            json.put("jsonObject", img);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.43.248:5211/makePDF",
                json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            /// 4th fxn to be called to be here...
                            Toast.makeText(MainActivity.this, "PDF Recieved", Toast.LENGTH_SHORT).show();
                            downloadpdf();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        downloadpdf();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    private void convertintobase64()
    {
        if(photosList==null)
        {
            Toast.makeText(MainActivity.this, "no image selected", Toast.LENGTH_SHORT).show();
            return;
        }
        for(int i=0;i<photosList.size();i++)
        {
            String currentimage=encodeImage(photosList.get(i));
            base.add(currentimage);
            Log.d("base64", "onClick: " + currentimage);
        }

        /// {khkhkhk,jjgjgjg,jgjgjgjg}

        imageString = "{";
        for(int i=0;i<photosList.size()-1;i++)
        {
            imageString+=base.get(i);
            imageString+=',';

        }
        imageString+=base.get(photosList.size()-1);
        imageString+='}';

        hitapi(imageString);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    /*
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("checkkk", "onCaptureImageResult: " + thumbnail.toString());

        photosList.add(thumbnail);
        convertintobase64();
    }*/
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        Log.d("checkkk", "onCaptureImageResult: " + thumbnail.toString());

        //ivImage.setImageBitmap(thumbnail);
        photosList.clear();
        photosList.add(thumbnail);
        alertedittext();
//        convertintobase64();

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

            }
        }


        photosList.add(bm);
        Log.d("checkkk", "onSelectFromGalleryResult: " + bm.toString());
//        ivImage.setImageBitmap(bm);
        convertintobase64();
    }*/
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) throws IOException {
        if(data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            int currentItem = 0;
            photosList.clear();
            while(currentItem < count) {
                Uri imageUri = data.getClipData().getItemAt(currentItem).getUri();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                photosList.add(bitmap);
                Log.d("checkkkk", "onSelectFromGalleryResult: " + imageUri);
                //do something with the image (save it to some directory or whatever you need to do with it here)
                currentItem = currentItem + 1;
            }
            alertedittext();
//            convertintobase64();
        } else if(data.getData() != null) {
            String imagePath = data.getData().getPath();
            //do something with the image (save it to some directory or whatever you need to do with it here)
        }
    }

    public void alertedittext()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Alert Dialog With EditText"); //Set Alert dialog title here
        alert.setMessage("Enter Your Name Here"); //Message here

        // Set an EditText view to get user input
        final EditText input = new EditText(MainActivity.this);
//        input.setTextColor(Color.parseColor("#000"));
//        input.setTextColor();
        int blackk = Color.parseColor("#000000");
        input.setTextColor(blackk);
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //You will get as string input data in this variable.
                // here we convert the input to a string and show in a toast.
                String srt = input.getEditableText().toString();
                pdfKaNaam=srt;
                convertintobase64();
                Toast.makeText(MainActivity.this,srt,Toast.LENGTH_LONG).show();
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                pdfKaNaam="downloadedpdf";
                convertintobase64();
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    public void openConvertedPDFs(MenuItem menuItem){
        startActivity(new Intent(MainActivity.this, ConvertedPDFActivity.class));
    }
}
