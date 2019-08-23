package com.moonpi.swiftnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UsersActivity extends AppCompatActivity
{
    private RecyclerView r;
    private DatabaseReference db;
    Toolbar mtoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        r = (RecyclerView) findViewById(R.id.rv);
        db = FirebaseDatabase.getInstance().getReference().child("User");
        r.setHasFixedSize(true);
        r.setLayoutManager(new LinearLayoutManager(this));
        mtoolbar=(Toolbar)findViewById(R.id.user_toolbarMain);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Other Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
    }

    @Override
    protected void onStart()
    {

        super.onStart();
        FirebaseRecyclerAdapter<Users,Userview> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, Userview>(Users.class,R.layout.users_layout,Userview.class,db) {
            @Override
            protected void populateViewHolder(final Userview viewHolder, final Users model, int position)
            {
              viewHolder.setUsername(model.getName());
                final String u_id=getRef(position).getKey();
                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        //Toast.makeText(getApplicationContext(),"Retrieved id:"+u_id.toString(),Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), TitlesActivity.class);
                        intent.putExtra("userEmail", model.getName());
                        Log.d("yahadekh", "onClick: " + model.getName());;
                        startActivity(intent);
                    }
                });


            }
        };
       r.setAdapter(firebaseRecyclerAdapter);
    }

    public static class Userview extends RecyclerView.ViewHolder
    {
        View mview;
        public Userview(View itemView)
        {
            super(itemView);
            mview=itemView;
        }

        public void setUsername(String name)
        {
            TextView uname=(TextView)mview.findViewById(R.id.txt);
            uname.setText(name);

        }
    }
}
