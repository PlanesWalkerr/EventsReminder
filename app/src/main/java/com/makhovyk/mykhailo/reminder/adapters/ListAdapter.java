package com.makhovyk.mykhailo.reminder.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.EventHolder> {

    private List<Event> events;


    class EventHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name_view)
        TextView nameVIew;
        @BindView(R.id.date_view)
        TextView dateVIew;

        Event event;

        public EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindEvent(Event event) {
            nameVIew.setText(event.getPersonName());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            dateVIew.setText(sdf.format(event.getDate()));
            this.event = event;
        }
    }

    public ListAdapter(ArrayList<Event> events) {
        this.events = events;
        Log.v("Adapter", String.valueOf(events.size()));
    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_birthday, parent, false);
        return new EventHolder(v);
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {
        Event event = events.get(position);
        holder.bindEvent(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
