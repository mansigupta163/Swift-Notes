package com.moonpi.swiftnotes;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ConvertedPDFActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> pdfList;
    RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converted_pdf);

        Log.d("getHere", "onCreate: ");

        pdfList = new ArrayList<>();
        pdfList.clear();

        String path = Environment.getExternalStorageDirectory().toString() + "/Swiftnotes";
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files == null){
            Toast.makeText(this, "No Files Added", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < files.length; i++) {
            pdfList.add(files[i].getName());
        }

        recyclerView = (RecyclerView) findViewById(R.id.rvList);
        recyclerAdapter = new RecyclerAdapter(this, pdfList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

    }
}