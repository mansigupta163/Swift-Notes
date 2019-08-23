package com.moonpi.swiftnotes;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPassword extends AppCompatActivity
{
    DatabaseReference db;
Toolbar mtoolbar;
Button generate;
    EditText phoneno;
    Otpgenerator otpgenerator;
    ProgressDialog mprogress;
    String SENT="SMS_SENT";
    String DELIEVERED="SMS_DELEIVERED";
    PendingIntent sentPI,delieveredPI;
    String otp,retrievedphoneno;
    BroadcastReceiver smsSentReceiver,smsDelieveredReceiver;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        db= FirebaseDatabase.getInstance().getReference();
        mtoolbar=(Toolbar)findViewById(R.id.forgotpassword_toolbar);
        generate=(Button)findViewById(R.id.phone_button3);
        phoneno=(EditText)findViewById(R.id.phone_editText3);
        otpgenerator=new Otpgenerator();
        mprogress=new ProgressDialog(this);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Forgot Password");
        getSupportActionBar().show();


        //given below pending intents are fired only once when they are called from sms.sendTextMessage()
        sentPI=PendingIntent.getBroadcast(ForgotPassword.this,0,new Intent(SENT),0);
        delieveredPI=PendingIntent.getBroadcast(ForgotPassword.this,0,new Intent(DELIEVERED),0);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {


                char[] msg=otpgenerator.sendotp(6);
                otp=msg.toString();
               // Toast.makeText(getApplicationContext(),otp,Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),otp,Toast.LENGTH_LONG).show();
                final String cellno=phoneno.getText().toString();
                db.child("Phone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        //Toast.makeText(ForgotPassword.this,dataSnapshot.child(cellno).child("email").getValue().toString(), Toast.LENGTH_SHORT).show();
                        if(dataSnapshot.hasChild(cellno))
                        {
                          retrievedphoneno=cellno;
                            email=dataSnapshot.child(cellno).child("email").getValue().toString();
                          //  Toast.makeText(ForgotPassword.this,"Email in forgot activity= "+email, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ForgotPassword.this,"Phone number is not valid", Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(ForgotPassword.this,LoginActivity.class);
                            startActivity(i);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//Toast.makeText(getApplicationContext(),cellno,Toast.LENGTH_LONG).show();


               // Intent intent=new Intent(getApplicationContext(),ForgotPassword.class);
                //PendingIntent pi=PendingIntent.getActivity(getApplicationContext(),0,intent,0);
try
{
//if is checking whether the permision is granted or not

    if(ContextCompat.checkSelfPermission(ForgotPassword.this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED)
  {
      ActivityCompat.requestPermissions(ForgotPassword.this,new String[]{Manifest.permission.SEND_SMS},1);
  }
else //when the permission is granted
    {
if(!cellno.isEmpty() || cellno.length()==10) {
    SmsManager sms = SmsManager.getDefault();

    sms.sendTextMessage(cellno,null, otp, sentPI,delieveredPI);//here null is to be replaced with the address

    //Toast.makeText(getApplicationContext(), "OTP sent to your mobile", Toast.LENGTH_LONG).show();
}
else
{
    Toast.makeText(getApplicationContext(),"Enter a valid 10 digit no",Toast.LENGTH_LONG).show();
}
  }
}catch (Exception e)
{
    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
}
            }
        });
    }

    protected  void onResume()
    {
        super.onResume();

        smsSentReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:  //this means that message sent out succesfully
                        Toast.makeText(getApplicationContext(),"OTP Sent successully",Toast.LENGTH_LONG).show();
                        final AlertDialog.Builder dialog=new AlertDialog.Builder(ForgotPassword.this);
                        View mview=getLayoutInflater().inflate(R.layout.dialog_otp,null);  //dialog_otp is the custom layout file
                        final EditText otp_box=(EditText)mview.findViewById(R.id.editText);  //here mview is used along with findViewById because the edittext and button both r in in layout file and not in the main page
                        Button submit=(Button)mview.findViewById(R.id.button3);

                        dialog.setCancelable(true);
                        dialog.setView(mview);
                        AlertDialog alertDialog=dialog.create();
                        alertDialog.show();

                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String text=otp_box.getText().toString();
                                otp_box.setText("");
                                Toast.makeText(getApplicationContext(),"OTP: "+otp,Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(),"Text: "+text,Toast.LENGTH_LONG).show();
                                if(text.equals(otp))
                                {
                                    Toast.makeText(getApplicationContext(),"OTP verified",Toast.LENGTH_LONG).show();
                                    Intent change=new Intent(ForgotPassword.this,ChangePassword.class);
                                    change.putExtra("email_id",email);
                                    startActivity(change);

                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),"OTP not verified",Toast.LENGTH_LONG).show();
                                    dialog.setCancelable(true);
                                }
                            }
                        });


                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getApplicationContext(),"Generic Failure",Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getApplicationContext(),"No Service", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getApplicationContext(),"PDU NULL!",Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getApplicationContext(),"Radio OFF",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        smsDelieveredReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

        switch (getResultCode())
        {
            case Activity.RESULT_OK:
                Toast.makeText(getApplicationContext(),"SMS Delievered",Toast.LENGTH_LONG).show();
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(getApplicationContext(),"SMS Not Delievered",Toast.LENGTH_LONG).show();
                break;
        }
            }
        };

        registerReceiver(smsSentReceiver,new IntentFilter(SENT));
        registerReceiver(smsDelieveredReceiver,new IntentFilter(DELIEVERED));
    }

    protected  void  onPause()   //when we r not using the broadcast activity than we should unregister the receiver
    {
        super.onPause();
        unregisterReceiver(smsSentReceiver);
        unregisterReceiver(smsDelieveredReceiver);
    }
}


/*Steps for above methods used
1) sms.sendTextMessage(cellno, null, otp, sentPI,delieveredPI);
when sentPI is called than sentPI=PendingIntent.getBroadcast(ForgotPassword.this,0,new Intent(SENT),0); is fired
which calls the regsterReceiver(smsSentReceiver) in onResume .This registerReceiver than fires the smsSentReceiver
 */