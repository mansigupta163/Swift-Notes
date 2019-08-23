package com.moonpi.swiftnotes.ColorPicker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.moonpi.swiftnotes.R;

public class DecryptedList extends AppCompatActivity {

    TextView tv;
    int i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypted_list);
        tv=(TextView)findViewById(R.id.textView14);
        String arr[]=getIntent().getStringArrayExtra("body");
        for(i=0;i<arr.length;i++) {


            tv.setText(arr[i].toString() + "\n");
        }

    }
}
