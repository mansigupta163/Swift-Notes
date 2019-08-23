package com.moonpi.swiftnotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import static com.moonpi.swiftnotes.R.id.pdfName;

/**
 * Created by ishaandhamija on 26/09/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerHolder> {

    Context ctx;
    ArrayList<String> allPDFsArrayList;

    public RecyclerAdapter(Context context, ArrayList<String> list) {
        this.ctx = context;
        this.allPDFsArrayList = list;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = li.inflate(R.layout.pdf_sample, parent, false);
        return new RecyclerHolder(itemView, ctx);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        final String pdfNamee = allPDFsArrayList.get(position);
        holder.pdfName.setText(pdfNamee);
    }

    @Override
    public int getItemCount() {
        return allPDFsArrayList.size();
    }

}