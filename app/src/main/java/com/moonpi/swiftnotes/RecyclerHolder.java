package com.moonpi.swiftnotes;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class RecyclerHolder extends RecyclerView.ViewHolder {

    TextView pdfName;
    View view;

    public RecyclerHolder(View itemView, final Context ctx) {
        super(itemView);

        this.pdfName = (TextView) itemView.findViewById(R.id.pdfName);

        this.view = itemView;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filename = Environment.getExternalStorageDirectory() + "/Swiftnotes/" + pdfName.getText().toString();
                File file = new File(filename);
                Uri internal = Uri.fromFile(file);
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(internal, "application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Intent intent = Intent.createChooser(target, "Open File");
                try {
                    ctx.startActivity(intent);
                }
                catch (ActivityNotFoundException e) {

                }
            }
        });

    }

}