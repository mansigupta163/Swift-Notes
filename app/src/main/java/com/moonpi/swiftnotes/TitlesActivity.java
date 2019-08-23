package com.moonpi.swiftnotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


public class TitlesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference db, db1;
    private Toolbar toolbar;
    private FirebaseAuth mauth;
    private String id;

    ArrayList<Note> noteList;
    MyNoteAdapter visitorAdapter;
    RecyclerView rvVisitors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_titles);

        recyclerView =(RecyclerView)findViewById(R.id.rv1);
//        ArrayList<String> myList =  (ArrayList<String>)getIntent().getSerializableExtra("mylist");
//        Log.d("Tag", "MyList" + myList);
//        db = FirebaseDatabase.getInstance().getReference().child("User");
        //String newid = db.push().getKey();
        //Log.d("Tag", "Id " + newid);
        //db1 = FirebaseDatabase.getInstance().getReference().child("User").child(newid).child("Notes");
        //db.child(newid).child("Notes");
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mauth= FirebaseAuth.getInstance();
        //id=mauth.getCurrentUser().getUid();

        toolbar = (Toolbar) findViewById(R.id.user_toolbarMain1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Titles");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences=getSharedPreferences("MYREFERENCES",MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail",null);
        Log.d("yahadekh", "onStart: " + userEmail);
        final String currUsername = getIntent().getStringExtra("userEmail");

        Log.d("yahadekh", "onStart: " + currUsername);
        ArrayList<Note> currNoteList = new ArrayList<>();

        currNoteList.clear();
        String body, title;
        StringBuffer result_body, result_title;
        if (currUsername.equals("Mansi")) {
            body = "mmmm";
            title = "Mmmmm";
//            currNoteList.add(new Note(body, title));
            result_body=Encrypt.encrypt(body,22);
//            result_title=Encrypt.encrypt(title,22);
            currNoteList.add(new Note(result_body.toString(), title));

            body = "Heyyyyy";
            title = "msmsm";
//            currNoteList.add(new Note(body, title));
            result_body=Encrypt.encrypt(body,22);
//            result_title=Encrypt.encrypt(title,22);
            currNoteList.add(new Note(result_body.toString(), title));

            body = "gdhdh";
            title = "ghdhdhdhdd";
//            currNoteList.add(new Note(body, title));
            result_body=Encrypt.encrypt(body,22);
//            result_title=Encrypt.encrypt(title,22);
            currNoteList.add(new Note(result_body.toString(), title));

            visitorAdapter = new MyNoteAdapter(TitlesActivity.this, currNoteList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(visitorAdapter);
        }
        else if (currUsername.equals("ayushi")) {

            body = "ayushiiiii";
            title = "ttt";
//            currNoteList.add(new Note(body, title));
            result_body=Encrypt.encrypt(body,22);
//            result_title=Encrypt.encrypt(title,22);
            currNoteList.add(new Note(result_body.toString(), title));

            body = "ddd";
            title = "wwwrwr";
//            currNoteList.add(new Note(body, title));
            result_body=Encrypt.encrypt(body,22);
//            result_title=Encrypt.encrypt(title,22);
            currNoteList.add(new Note(result_body.toString(), title));

            visitorAdapter = new MyNoteAdapter(TitlesActivity.this, currNoteList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(visitorAdapter);
        }
        else if (currUsername.equals("rashishukla")) {

            body = "Hey";
            title = "MySelf";
//            currNoteList.add(new Note(body, title));
            result_body=Encrypt.encrypt(body,22);
//            result_title=Encrypt.encrypt(title,22);
            currNoteList.add(new Note(result_body.toString(), title));

            body = "rashiiii";
            title = "second";
//            currNoteList.add(new Note(body, title));
            result_body=Encrypt.encrypt(body,22);
//            result_title=Encrypt.encrypt(title,22);
            currNoteList.add(new Note(result_body.toString(), title));

            Log.d("yahaaa", "onStart: " + currNoteList.get(0).getTitle());
            Log.d("yahaaa", "onStart: " + currNoteList.get(0).getBody());


            visitorAdapter = new MyNoteAdapter(TitlesActivity.this, currNoteList);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(visitorAdapter);
        }

//        DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("Users");
//        firebaseRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
//            @Override
//            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
//                for (DataSnapshot snap : dataSnapshot.getChildren()){
//                    Users user = snap.getValue(Users.class);
//                    if (user.getName().equals(currUsername)) {
//
//                        ArrayList<Note> currNoteList = new ArrayList<>();
//                        Log.d("yahadekh", "onDataChange: " + currNoteList.get(0).getTitle());
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

//        FirebaseRecyclerAdapter<Titles,TitleView> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Titles, TitleView>(Titles.class, R.layout.titles_layout, TitleView.class, db) {
//            @Override
//            protected void populateViewHolder(TitleView viewHolder, Titles model, int position) {
//
//                viewHolder.setTitle(model.getTitle());
//                id = getRef(position).getKey();
//                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//
//
//                       // Toast.makeText(getApplicationContext(),"Retrieved id:"+id.toString(), Toast.LENGTH_LONG).show();
//
//                    }
//                });
//
//            }
//        };
//        recyclerView.setAdapter(firebaseRecyclerAdapter);



    }

    public static class TitleView extends RecyclerView.ViewHolder {

        View mView;

        public TitleView(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView titleName =(TextView) mView.findViewById(R.id.txt1);
            titleName.setText(title);
            Log.i("Tag", "Titles" + titleName);
        }

    }

}
