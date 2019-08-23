package com.moonpi.swiftnotes;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moonpi.swiftnotes.ColorPicker.ColorPickerDialog;

import java.util.ArrayList;
import java.util.Locale;

import static com.moonpi.swiftnotes.ColorPicker.ColorPickerSwatch.OnColorSelectedListener;
import static com.moonpi.swiftnotes.ColorPicker.ColorPickerSwatch.OnTouchListener;
import static com.moonpi.swiftnotes.DataUtils.NEW_NOTE_REQUEST;
import static com.moonpi.swiftnotes.DataUtils.NOTE_BODY;
import static com.moonpi.swiftnotes.DataUtils.NOTE_COLOUR;
import static com.moonpi.swiftnotes.DataUtils.NOTE_FONT_SIZE;
import static com.moonpi.swiftnotes.DataUtils.NOTE_HIDE_BODY;
import static com.moonpi.swiftnotes.DataUtils.NOTE_REQUEST_CODE;
import static com.moonpi.swiftnotes.DataUtils.NOTE_TITLE;


public class EditActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    // Layout components
    private EditText titleEdit, bodyEdit;
    private RelativeLayout relativeLayoutEdit;
    private Toolbar toolbar;
    private MenuItem menuHideBody;
    private DatabaseReference mdatabase,myRef1,myRef2,mUserdatabase,mu;
    private FirebaseAuth mauth;
    private InputMethodManager imm;
    private Bundle bundle;

    private String[] colourArr; // Colours string array
    private int[] colourArrResId; // colourArr to resource int array
    private int[] fontSizeArr; // Font sizes int array
    private String[] fontSizeNameArr; // Font size names string array

    // Defaults
    private String colour = "#FFFFFF"; // white default
    private int fontSize = 18; // Medium default
    private Boolean hideBody = false;

    private AlertDialog fontDialog, saveChangesDialog;
    private ColorPickerDialog colorPickerDialog;
    private String id;
    private String key;
    private final int REQ_CODE_SPEECH_INPUT = 100;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mauth=FirebaseAuth.getInstance();
        mdatabase=FirebaseDatabase.getInstance().getReference().child("KEY");
        id=mauth.getCurrentUser().getUid();
        mUserdatabase=FirebaseDatabase.getInstance().getReference().child("User");

        myRef1=FirebaseDatabase.getInstance().getReferenceFromUrl("https://swiftnotes-master.firebaseio.com/Notes");
        mu=FirebaseDatabase.getInstance().getReference().child("User").child(id);
        myRef2=FirebaseDatabase.getInstance().getReferenceFromUrl("https://swiftnotes-master.firebaseio.com/Notes");
        mdatabase.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                key=dataSnapshot.child("decryptkey").getValue().toString(); //getting the key from database

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // Android version >= 18 -> set orientation fullUser
        if (Build.VERSION.SDK_INT >= 18)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);

        // Android version < 18 -> set orientation fullSensor
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        // Initialize colours and font sizes arrays
        colourArr = getResources().getStringArray(R.array.colours);

        colourArrResId = new int[colourArr.length];
        for (int i = 0; i < colourArr.length; i++)
            colourArrResId[i] = Color.parseColor(colourArr[i]);

        fontSizeArr = new int[] {14, 18, 22}; // 0 for small, 1 for medium, 2 for large
        fontSizeNameArr = getResources().getStringArray(R.array.fontSizeNames);

        setContentView(R.layout.activity_edit);

        // Init layout components
        toolbar = (Toolbar)findViewById(R.id.toolbarEdit);
        titleEdit = (EditText)findViewById(R.id.titleEdit);
        bodyEdit = (EditText)findViewById(R.id.bodyEdit);
        relativeLayoutEdit = (RelativeLayout)findViewById(R.id.relativeLayoutEdit);
        ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);

        imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);

        if (toolbar != null)
            initToolbar();

        // If scrollView touched and note body doesn't have focus -> request focus and go to body end
        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!bodyEdit.isFocused()) {
                    bodyEdit.requestFocus();
                    bodyEdit.setSelection(bodyEdit.getText().length());
                    // Force show keyboard
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                            InputMethodManager.HIDE_IMPLICIT_ONLY);

                    return true;
                }

                return false;
            }
        });

        // Get data bundle from MainActivity
        bundle = getIntent().getExtras();

        if (bundle != null) {
            // If current note is not new -> initialize colour, font, hideBody and EditTexts
            if (bundle.getInt(NOTE_REQUEST_CODE) != NEW_NOTE_REQUEST) {
                colour = bundle.getString(NOTE_COLOUR);
                fontSize = bundle.getInt(NOTE_FONT_SIZE);
                hideBody = bundle.getBoolean(NOTE_HIDE_BODY);

                titleEdit.setText(bundle.getString(NOTE_TITLE));
                bodyEdit.setText(bundle.getString(NOTE_BODY));
                bodyEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

                if (hideBody)
                    menuHideBody.setTitle(R.string.action_show_body);
            }

            // If current note is new -> request keyboard focus to note title and show keyboard
            else if (bundle.getInt(NOTE_REQUEST_CODE) == NEW_NOTE_REQUEST) {
                titleEdit.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }

            // Set background colour to note colour
            relativeLayoutEdit.setBackgroundColor(Color.parseColor(colour));
        }

        initDialogs(this);
    }


    /**
     * Initialize toolbar with required components such as
     * - title, navigation icon + listener, menu/OnMenuItemClickListener, menuHideBody -
     */
    protected void initToolbar() {
        toolbar.setTitle("");

        // Set a 'Back' navigation icon in the Toolbar and handle the click
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Inflate menu_edit to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_edit);

        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(this);

        Menu menu = toolbar.getMenu();

        if (menu != null)
            menuHideBody = menu.findItem(R.id.action_hide_show_body);
    }


    /**
     * Implementation of AlertDialogs such as
     * - colorPickerDialog, fontDialog and saveChangesDialog -
     * @param context The Activity context of the dialogs; in this case EditActivity context
     */
    protected void initDialogs(Context context) {
        // Colour picker dialog
        colorPickerDialog = ColorPickerDialog.newInstance(R.string.dialog_note_colour,
                colourArrResId, Color.parseColor(colour), 3,
                isTablet(this) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);

        // Colour picker listener in colour picker dialog
        colorPickerDialog.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                // Format selected colour to string
                String selectedColourAsString = String.format("#%06X", (0xFFFFFF & color));

                // Check which colour is it and equal to main colour
                for (String aColour : colourArr)
                    if (aColour.equals(selectedColourAsString))
                        colour = aColour;

                // Re-set background colour
                relativeLayoutEdit.setBackgroundColor(Color.parseColor(colour));
            }
        });


        // Font size picker dialog
        fontDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_font_size)
                .setItems(fontSizeNameArr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Font size updated with new pick
                        fontSize = fontSizeArr[which];
                        bodyEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                    }
                })
                .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();


        // 'Save changes?' dialog
        saveChangesDialog = new AlertDialog.Builder(context)
                .setMessage(R.string.dialog_save_changes)
                .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If 'Yes' clicked -> check if title is empty
                        // If title not empty -> save and go back; Otherwise toast
                        if (!isEmpty(titleEdit) || !isEmpty(bodyEdit))
                            saveChanges();

                        else
                            toastEditTextCannotBeEmpty();
                    }
                })
                .setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If 'No' clicked in new note -> put extra 'discard' to show toast
                        if (bundle != null && bundle.getInt(NOTE_REQUEST_CODE) ==
                                NEW_NOTE_REQUEST) {

                            Intent intent = new Intent();
                            intent.putExtra("request", "discard");

                            setResult(RESULT_CANCELED, intent);

                            imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);

                            dialog.dismiss();
                            finish();
                            overridePendingTransition(0, 0);
                        }
                    }
                })
                .create();
    }


    /**
     * Check if current device has tablet screen size or not
     * @param context current application context
     * @return true if device is tablet, false otherwise
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    /**
     * Item clicked in Toolbar menu callback method
     * @param item Item clicked
     * @return true if click detected and logic finished, false otherwise
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        // voice to text recognition
        if (id == R.id.gvoice)
        {
            promptSpeechInput();
        }
        // Note colour menu item clicked -> show colour picker dialog
        if (id == R.id.action_note_colour) {
            colorPickerDialog.show(getFragmentManager(), "colourPicker");
            return true;
        }

        // Font size menu item clicked -> show font picker dialog
        if (id == R.id.action_font_size) {
            fontDialog.show();
            return true;
        }

        // If 'Hide note body in list' or 'Show note body in list' clicked
        if (id == R.id.action_hide_show_body) {
            // If hideBody false -> set to true and change menu item text to 'Show note body in list'
            if (!hideBody) {
                hideBody = true;
                menuHideBody.setTitle(R.string.action_show_body);

                // Toast note body will be hidden
                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_note_body_hidden),
                        Toast.LENGTH_SHORT);
                toast.show();
            }

            // If hideBody true -> set to false and change menu item text to 'Hide note body in list'
            else {
                hideBody = false;
                menuHideBody.setTitle(R.string.action_hide_body);

                // Toast note body will be shown
                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_note_body_showing),
                        Toast.LENGTH_SHORT);
                toast.show();
            }

            return true;
        }

        return false;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    bodyEdit.setText(result.get(0));
                }
                break;
            }

        }
    }
    /**
     * Create an Intent with title, body, colour, font size and hideBody extras
     * Set RESULT_OK and go back to MainActivity
     */
    protected void saveChanges() {
        Intent intent = new Intent();


        int k=Integer.parseInt(key);
        final String titletext= titleEdit.getText().toString();
        final StringBuffer bodytext=Encrypt.encrypt(bodyEdit.getText().toString(),(k));  //encrypting the text with key
        // Package everything and send back to activity with OK
         //storing the notes in firebase
        final String noteid=myRef1.push().getKey();    //creating a key
        final String userid =mu.push().getKey();    //creating a key
        myRef2=FirebaseDatabase.getInstance().getReference().child("Notes");

//        mUserdatabase.child(id).child("Notes").child(titletext.toString()).setValue(noteid);
//
//                HashMap<String, String> hmap=new HashMap<String, String>();
//                hmap.put("title",titletext.toString());
//                hmap.put("body",bodytext.toString());
//
//                myRef1.child(noteid).setValue(hmap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                               @Override
//                                                               public void onSuccess(Void aVoid) {
//                                                                   Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_LONG).show();
//                                                               }
//                                                           });


        ArrayList<String> mylist = new ArrayList<String>();
        mylist.add(userid); //this adds an element to the list.
        Log.d("TAG", "myList" + mylist);
//        Intent intent1 = new Intent(this, TitlesActivity.class);
//        intent.putExtra("mylist", mylist);
//        startActivity(intent1);


        mu.child("Notes").child(userid).child("Title").setValue(titletext.toString());
        mu.child("Notes").child(userid).child("Body").setValue(bodytext.toString());









        intent.putExtra(NOTE_TITLE, titletext.toString().toString());
        intent.putExtra(NOTE_BODY, bodytext.toString()); //setting the encrypted text
        intent.putExtra(NOTE_COLOUR, colour);
        intent.putExtra(NOTE_FONT_SIZE, fontSize);
        intent.putExtra(NOTE_HIDE_BODY, hideBody);

        setResult(RESULT_OK, intent);

        imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);

        finish();
        overridePendingTransition(0, 0);
    }


    /**
     * Back or navigation '<-' pressed
     */
    @Override
    public void onBackPressed() {
        // New note -> show 'Save changes?' dialog
        if (bundle.getInt(NOTE_REQUEST_CODE) == NEW_NOTE_REQUEST) {
            saveChangesDialog.show();

        }

        // Existing note
        else {
            /*
             * If title is not empty -> Check if note changed
             *  If yes -> saveChanges
             *  If not -> hide keyboard if showing and finish
             */
            if (!isEmpty(titleEdit) || !isEmpty(bodyEdit)) {
                if (!(titleEdit.getText().toString().equals(bundle.getString(NOTE_TITLE))) ||
                    !(bodyEdit.getText().toString().equals(bundle.getString(NOTE_BODY))) ||
                    !(colour.equals(bundle.getString(NOTE_COLOUR))) ||
                    fontSize != bundle.getInt(NOTE_FONT_SIZE) ||
                    hideBody != bundle.getBoolean(NOTE_HIDE_BODY)) {

                    saveChanges();
                }

                else {
                    imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);

                    finish();
                    overridePendingTransition(0, 0);
                }
            }

            // If title empty -> Toast title cannot be empty
            else
                toastEditTextCannotBeEmpty();
        }
    }


    /**
     * Check if passed EditText text is empty or not
     * @param editText The EditText widget to check
     * @return true if empty, false otherwise
     */
    protected boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    /**
     * Show Toast for 'Title cannot be empty'
     */
    protected void toastEditTextCannotBeEmpty() {
        Toast toast = Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.toast_edittext_cannot_be_empty),
                Toast.LENGTH_LONG);
        toast.show();
    }


    /**
     * If current window loses focus -> hide keyboard
     * @param hasFocus parameter passed by system; true if focus changed, false otherwise
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus)
            if (imm != null && titleEdit != null)
                imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);
    }


    /**
     * Orientation changed callback method
     * If orientation changed -> If any AlertDialog is showing -> dismiss it to prevent WindowLeaks
     * @param newConfig Configuration passed by system
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (colorPickerDialog != null && colorPickerDialog.isDialogShowing())
            colorPickerDialog.dismiss();

        if (fontDialog != null && fontDialog.isShowing())
            fontDialog.dismiss();

        if (saveChangesDialog != null && saveChangesDialog.isShowing())
            saveChangesDialog.dismiss();

        super.onConfigurationChanged(newConfig);
    }
}
