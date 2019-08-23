package com.moonpi.swiftnotes;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity
{
    ListView lv;
    Toolbar mtoolbar;
    String items[]={"Erase data","Decryption key","About Us","Deactivate Account"};
    DatabaseReference muserdatabase;
    FirebaseAuth mauth;
    ProgressDialog pd;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        pd=new ProgressDialog(this);
        mauth=FirebaseAuth.getInstance();
        muserdatabase=FirebaseDatabase.getInstance().getReference();
        lv=(ListView)findViewById(R.id.lstv);
        mtoolbar=(Toolbar)findViewById(R.id.settingstoolbar);
        ListAdapter listAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item,items);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().show();

        lv.setAdapter(listAdapter);

      lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int position, final long l)
          {
              String value=adapterView.getItemAtPosition(position).toString();

              if(value.equals("About Us"))
              {
                  Intent about=new Intent(Settings.this,About.class);
                  startActivity(about);
              }
              if(value.equals("Decryption key"))
              {
                  AlertDialog.Builder builder=new AlertDialog.Builder(Settings.this);
                  View mview=getLayoutInflater().inflate(R.layout.decryptiondialog,null);
                  final EditText key=(EditText)mview.findViewById(R.id.decrytext);
                  Button click=(Button)mview.findViewById(R.id.decrybutton);

                  click.setOnClickListener(new View.OnClickListener()
                  {
                      @Override
                      public void onClick(View view)
                      {
                          pd.setTitle("Saving");
                          pd.setMessage("Please wait....");
                          pd.setCanceledOnTouchOutside(false);
                          pd.show();
                          String decrkey=key.getText().toString();
                          String id=mauth.getCurrentUser().getUid();
                          muserdatabase.child("KEY").child(id).child("decryptkey").setValue(decrkey).addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task)
                              {
                                  if(task.isSuccessful())
                                  {
                                      pd.dismiss();
                                      Toast.makeText(getApplicationContext(),"Saved successfully",Toast.LENGTH_LONG).show();
                                  }
                                  else
                                  {
                                      Toast.makeText(getApplicationContext(),"Error in saving key",Toast.LENGTH_LONG).show();
                                  }

                              }
                          });

                      }
                  });
                  builder.setCancelable(true);
                  builder.setView(mview);
                  AlertDialog alertDialog=builder.create();
                  alertDialog.show();
              }

              if(value.equals("Deactivate Account"))
              {
                    id=mauth.getCurrentUser().getUid();

                  final AlertDialog.Builder builder=new AlertDialog.Builder(Settings.this);

                  builder.setTitle("Account Deactivation").setMessage(R.string.message).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i)
                      {

                          mauth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task)
                              {
                                  muserdatabase.child("User").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                      @Override
                                      public void onDataChange(DataSnapshot dataSnapshot) {
                                          dataSnapshot.getRef().removeValue();
                                          Toast.makeText(getApplicationContext(),"Account deleted successfully",Toast.LENGTH_LONG).show();
                                          Intent loginpage=new Intent(Settings.this,LoginActivity.class);
                                          loginpage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                          startActivity(loginpage);
                                      }

                                      @Override
                                      public void onCancelled(DatabaseError databaseError) {

                                      }
                                  });
                              }
                          });
                      }
                  }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i)
                      {
                        Toast.makeText(getApplicationContext(),"Canceled",Toast.LENGTH_LONG).show();
                      }
                  });
                 builder.show();



              }
          }
      });

    }
}
