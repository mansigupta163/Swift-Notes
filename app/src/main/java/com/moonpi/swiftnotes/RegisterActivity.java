package com.moonpi.swiftnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Date;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Toolbar mtoolbar;
    TextInputLayout name,uname,password,phone;
    Button create;
    DatabaseReference db,forgotpassworddatabase;
    FirebaseAuth mauth;
    ProgressDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mtoolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Account");
        sharedPreferences=getSharedPreferences("MYREFERENCES",MODE_PRIVATE);
        mdialog=new ProgressDialog(this);

        create=(Button)findViewById(R.id.button2);

        db= FirebaseDatabase.getInstance().getReference().child("User");
        forgotpassworddatabase=FirebaseDatabase.getInstance().getReference().child("Phone");
        mauth=FirebaseAuth.getInstance();
        phone=(TextInputLayout)findViewById(R.id.phone);
        name=(TextInputLayout)findViewById(R.id.name_textInputLayout3);
        uname=(TextInputLayout)findViewById(R.id.textInputLayout4);
        password=(TextInputLayout)findViewById(R.id.textInputLayout5);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String client_name=name.getEditText().getText().toString();
                String email=uname.getEditText().getText().toString();
                String pass=password.getEditText().getText().toString();
                String phoneno=phone.getEditText().getText().toString();
                if(phoneno!=null && phoneno.length()==10) {

                    mdialog.setTitle("Creating Account");
                    mdialog.setMessage("Please wait while we are creating ur account");
                    mdialog.setCanceledOnTouchOutside(false);
                    mdialog.getProgress();
                    mdialog.show();


                    register(client_name, email, pass,phoneno);
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "Phone number should be of 10 digits", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void register(final String client_name, final String email, String pass, final String ph)
    {

try {

    mauth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {

            if (task.isSuccessful()) {

                String id = mauth.getCurrentUser().getUid();

                String token = FirebaseInstanceId.getInstance().getToken();
                String date = DateFormat.getDateTimeInstance().format(new Date()).toString();
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("userEmail",email);
                editor.commit();

                HashMap<String,String> mp=new HashMap<String, String>();
                mp.put("email",email);
                forgotpassworddatabase.child(ph).setValue(mp); //storing email id in the phone

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("phone",ph);
                map.put("email",email);
                map.put("name", client_name);
                map.put("device_token", token);
                map.put("date", date);
                map.put("online", "true");
                db.child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {


                            Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_LONG).show();
                            mdialog.dismiss();
                            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                            i.putExtra("message","null");
                            i.putExtra("classFrom","RegisterActivity");
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            mdialog.hide();
                            Toast.makeText(getApplicationContext(), "Error occured", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }

        }
    });
}catch (Exception e)
{
    Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
}
    }
}
