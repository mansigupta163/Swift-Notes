package com.moonpi.swiftnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PersonalDetails extends AppCompatActivity
{
    Toolbar mtoolbar;
    ImageView userimage;
    ImageButton save;
    Button changeimage;
    TextView name;
    EditText email,phone;
    DatabaseReference muserdatabase,db;
    FirebaseAuth mauth;
String userid;
    StorageReference pp;
    ProgressDialog pd;
    private static final int pick=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        mtoolbar=(Toolbar)findViewById(R.id.personaldetailstoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Personal Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        pp= FirebaseStorage.getInstance().getReference();
        name=(TextView)findViewById(R.id.textView6);
        userimage=(ImageView)findViewById(R.id.imageView3);
        save=(ImageButton)findViewById(R.id.imageButton);
        changeimage=(Button)findViewById(R.id.button4);
        email=(EditText)findViewById(R.id.editText3);
        phone=(EditText)findViewById(R.id.editText5);
        db=FirebaseDatabase.getInstance().getReference().child("Details");
        mauth=FirebaseAuth.getInstance();
        muserdatabase= FirebaseDatabase.getInstance().getReference().child("User");
        userid=mauth.getCurrentUser().getUid();
        db.keepSynced(true);

        muserdatabase.child(userid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uname=dataSnapshot.getValue().toString();
                name.setText(uname);
                name.setEnabled(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db.child(userid).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChildren())
                {
                    if(dataSnapshot.child("email").getValue()!=null && dataSnapshot.child("phoneno").getValue()!=null && dataSnapshot.child("photo").getValue()!=null) {
                        email.setText(dataSnapshot.child("email").getValue().toString());
                        phone.setText(dataSnapshot.child("phoneno").getValue().toString());

                        String path = dataSnapshot.child("photo").getValue().toString();
                        Picasso.with(PersonalDetails.this).load(path).placeholder(R.drawable.ic_person_black_24dp).into(userimage);
                    }
                    else
                    {
                        email.setText("");
                        phone.setText("");

                    }
                }else
                {
                  email.setText("");
                    phone.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                pd=new ProgressDialog(PersonalDetails.this);
                pd.setTitle("Saving");
                pd.setMessage("Please wait....");
                pd.setCanceledOnTouchOutside(false);

                pd.show();
                final String mail=email.getText().toString();
                final String ph=phone.getText().toString();


                final HashMap<String,String> map=new HashMap<String, String>();
                map.put("email",mail);
                map.put("phoneno",ph);
                map.put("photo","default");

                db.child(userid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            pd.dismiss();

                            Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        changeimage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent galleryintent=new Intent();
                galleryintent.setType("image/+");
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryintent,"Select Photo"),pick);//selecting photo from gallery
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode==pick && resultCode==RESULT_OK)
        {
            Uri imageuri=data.getData();//getting path of selected photo
            CropImage.activity(imageuri).setAspectRatio(1,1).start(this);//cropping image

        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);//getting cropped image
            if (resultCode == RESULT_OK)
            {
                final String id = mauth.getCurrentUser().getUid();

                pd = new ProgressDialog(PersonalDetails.this);
                pd.setTitle("Cropping");
                pd.setMessage("Please wait....");
                pd.setCanceledOnTouchOutside(false);
                pd.show();


                final Uri resulturi = result.getUri();
                pp.child("Profile_pictures").child(id+".jpg").putFile(resulturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {

                        if(task.isSuccessful())
                        {
                            db.child(id).child("photo").setValue(resulturi.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    Picasso.with(PersonalDetails.this).load(resulturi).into(userimage);
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(), "Successfully uploaded image", Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                    }
                });


            }
        }
        else if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
        {

            Toast.makeText(getApplicationContext(),"Error occured while uploading",Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
