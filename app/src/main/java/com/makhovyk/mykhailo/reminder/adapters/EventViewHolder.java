package com.makhovyk.mykhailo.reminder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.makhovyk.mykhailo.reminder.model.Event;


public class EventViewHolder extends RecyclerView.ViewHolder {

    Context context;
    Event event;


    public EventViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
    }

    public void bindEvent(Event event) {
        this.event = event;
    }


}
