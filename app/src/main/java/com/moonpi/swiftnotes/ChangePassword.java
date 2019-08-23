package com.moonpi.swiftnotes;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity
{
Toolbar mtoolbar;
    Button change;
    TextInputLayout tp;
    FirebaseAuth mauth;
    FirebaseUser user;
    ProgressDialog pd;
    String emailid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        pd=new ProgressDialog(ChangePassword.this);
        mtoolbar=(Toolbar)findViewById(R.id.password_toolbar);
        tp=(TextInputLayout)findViewById(R.id.textInputLayout3);
        change=(Button)findViewById(R.id.button5);
        mauth=FirebaseAuth.getInstance();
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        emailid=getIntent().getStringExtra("email_id");
        tp.getEditText().setText(emailid);
        Toast.makeText(this,emailid, Toast.LENGTH_SHORT).show();
        tp.getEditText().setEnabled(false);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                user=mauth.getCurrentUser();

                //String email=tp.getEditText().getText().toString();
                if(!emailid.isEmpty())
                {
                    pd.setTitle("Reset link");
                    pd.setMessage("Please Wait....");
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();
                    mauth.sendPasswordResetEmail(emailid).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                tp.getEditText().setText("");
                                Toast.makeText(getApplicationContext(), "Link has been sent to your Email", Toast.LENGTH_LONG).show();
                                 pd.dismiss();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Enter a valid email id",Toast.LENGTH_LONG).show();
                    pd.hide();
                }
            }
        });
    }
}
