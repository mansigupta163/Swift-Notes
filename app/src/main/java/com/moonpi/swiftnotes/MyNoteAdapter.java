package com.moonpi.swiftnotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ayushi on 11/29/2017.
 */

public class MyNoteAdapter extends RecyclerView.Adapter<MyNoteAdapter.ListItemHolder> {

    Context context;
    ArrayList<Note> visitors;

    public MyNoteAdapter(Context context, ArrayList<Note> visitors) {
        this.context = context;
        this.visitors = visitors;
    }

    @Override
    public MyNoteAdapter.ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.sample_note,parent,false);
        return new ListItemHolder(view);
    }

    @Override
    public void onBindViewHolder(MyNoteAdapter.ListItemHolder holder, int position) {
        final Note item = visitors.get(position);
        holder.note_body.setText(item.getBody());
        holder.note_title.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return visitors.size();
    }

    public class ListItemHolder extends RecyclerView.ViewHolder{
        View mainView;
        TextView note_title, note_body;
        public ListItemHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            note_title = (TextView) itemView.findViewById(R.id.note_title);
            note_body = (TextView) itemView.findViewById(R.id.note_body);
        }
    }
}