package com.moonpi.swiftnotes;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

//also include Notes in manifest file
//This class is for enabling offline capabilities of firebase
/**
 * Created by aman on 18/9/17.
 */

public class Notes extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        //For enabling the offline capabilities for storing image with picasso
//also include dependency for okhttp
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
