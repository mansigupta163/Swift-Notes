package com.moonpi.swiftnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    Toolbar mtoolbar;
    FirebaseAuth authentication;
    DatabaseReference db;
    Button login;

    ProgressDialog mdialog;
    TextInputLayout uname,pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mtoolbar = (Toolbar) findViewById(R.id.logintoolbar);
        mdialog=new ProgressDialog(LoginActivity.this);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");
        login=(Button)findViewById(R.id.login_button);
        uname=(TextInputLayout)findViewById(R.id.textInputLayout);
        pass=(TextInputLayout)findViewById(R.id.textInputLayout2);
        db= FirebaseDatabase.getInstance().getReference().child("User");
        authentication=FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String username=uname.getEditText().getText().toString();
                String password=pass.getEditText().getText().toString();


                if(!username.isEmpty() && !password.isEmpty())
                {
                    mdialog.setTitle("Signing in");
                    mdialog.setMessage("Please wait while we are redirecting");
                    mdialog.setCanceledOnTouchOutside(false);
                    mdialog.show();

                    loginuser(username,password);
                }
                else if(username.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Please enter email",Toast.LENGTH_LONG).show();
                }
                else if(password.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Please Enter password",Toast.LENGTH_LONG).show();
                }


            }
        });


    }

    private void loginuser(String username,String password)
    {


        try {

            authentication.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task)
                {

                    if (task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Login Successful",Toast.LENGTH_LONG).show();



                        final String current_userid = authentication.getCurrentUser().getUid();
                        final String device_token = FirebaseInstanceId.getInstance().getToken();//retrieving the token id of the device with which user has signed in
                        db.child(current_userid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String stored_device_token = dataSnapshot.child("device_token").getValue().toString();
                                String state = dataSnapshot.child("online").getValue().toString();
                                final String name=dataSnapshot.child("name").getValue().toString();

                                //if the stored token device is equal to the token value with which user logged in than only user in allowed to sign in

                                if (stored_device_token.equals(device_token) || (!stored_device_token.equals(device_token) && state.equals("false"))) {
                                    db.child(current_userid).child("online").setValue("true");
                                    db.child(current_userid).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mdialog.dismiss();



                                            Intent mainactivity = new Intent(LoginActivity.this, MainActivity.class);
                                            mainactivity.putExtra("message","null");
                                            mainactivity.putExtra("classFrom","LoginActivity");
                                            mainactivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainactivity);

                                        }
                                    });
                                }
                               /* else if(!stored_device_token.equals(device_token) && state.equals("true"))
                                {
                                    mdialog.dismiss();
                                    Toast.makeText(getApplicationContext(),"User is already logged in with some another device",Toast.LENGTH_LONG).show();
                                    db.child(current_userid).child("online").setValue("false");
                                    authentication.getInstance().signOut();
                                }*/

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                                //Toast.makeText(getApplicationContext(),databaseError.getMessage().toString(),Toast.LENGTH_LONG).show();
                            }
                        });

             /*  db.child(current_userid).child("online").setValue("true");
                db.child(current_userid).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent mainactivity = new Intent(LoginActivity.this, MainActivity.class);
                        mainactivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainactivity);

                    }
                });*/


                    }
                    else {
                        mdialog.hide();

                        Toast.makeText(getApplicationContext(), "Unable to sign in", Toast.LENGTH_LONG).show();
                    }


                }
            });
        }catch (Exception ee)
        {
            Toast.makeText(getApplicationContext(),ee.getMessage(),Toast.LENGTH_LONG).show();
        }

    }


    public void send_msg(View view)
    {
            Intent i=new Intent(LoginActivity.this,ForgotPassword.class);
            startActivity(i);

    }
}