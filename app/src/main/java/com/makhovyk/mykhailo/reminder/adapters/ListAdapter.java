package com.makhovyk.mykhailo.reminder.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.makhovyk.mykhailo.reminder.EditEventActivity;
import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.model.ListItem;
import com.makhovyk.mykhailo.reminder.model.Separator;
import com.makhovyk.mykhailo.reminder.utils.Constants;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> events;
    private final String TAG = "TAG";
    Context context;


    class BirthdayHolder extends EventViewHolder {

        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_date)
        TextView tvDate;


        BirthdayHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindEvent(Event event) {
            super.bindEvent(event);
            tvName.setText(event.getPersonName());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            tvDate.setText(sdf.format(event.getDate()));
        }

    }

    class OtherEventHolder extends EventViewHolder {

        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tv_event_name)
        TextView tvEventName;

        OtherEventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindEvent(Event event) {
            super.bindEvent(event);
            tvName.setText(event.getPersonName());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            tvDate.setText(sdf.format(event.getDate()));
            tvEventName.setText(event.getEventName());

        }
    }

    class SeparatorHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_month)
        TextView tvMonth;

        public SeparatorHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindEvent(Separator separator) {
            tvMonth.setText(separator.getMonth());

        }
    }

    public ListAdapter(ArrayList<ListItem> events, Context context) {
        this.events = events;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        switch (viewType) {
            case 1:
                v = inflater.inflate(R.layout.item_birthday, parent, false);
                return new BirthdayHolder(v);
            case 3:
                v = inflater.inflate(R.layout.item_other_event, parent, false);
                return new OtherEventHolder(v);
            case 5:
                v = inflater.inflate(R.layout.item_separator, parent, false);
                return new SeparatorHolder(v);
            default:
                v = inflater.inflate(R.layout.item_birthday, parent, false);
                return new BirthdayHolder(v);
        }




    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        switch (holder.getItemViewType()) {
            case 1:
                BirthdayHolder bh = (BirthdayHolder) holder;
                bh.bindEvent((Event) events.get(position));
                break;
            case 3:
                OtherEventHolder oh = (OtherEventHolder) holder;
                oh.bindEvent((Event) events.get(position));
                break;
            case 5:
                SeparatorHolder sh = (SeparatorHolder) holder;
                sh.bindEvent((Separator) events.get(position));
        }

        View itemView = holder.itemView;
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!events.get(position).isSeparator()) {
                    PopupMenu popup = new PopupMenu(view.getContext(), view, Gravity.RIGHT);
                    popup.inflate(R.menu.options_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.edit_menu_item:
                                    editEvent((Event) events.get(position));
                                    return true;
                                case R.id.delete_menu_item:
                                    deleteEvent((Event) events.get(position));
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popup.show();
                }
                return true;
            }
        });

    }

    @Override
    public int getItemViewType(int position) {

        if (events.get(position).isSeparator()) {
            return 5;
        }
        Event event = (Event) events.get(position);
        switch (event.getType()) {
            case Constants.TYPE_BIRTHDAY:
                return 1;
            case Constants.TYPE_ANNIVERSARY:
                return 1;
            case Constants.TYPE_OTHER_EVENT:
                return 3;
            default:
                return 10;

        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void deleteEvent(Event event) {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        dbHelper.deleteEvent(event.getTimestamp());
        Log.v(TAG, "Deleting item " + " : " + event.toString());
        events.remove(event);

        notifyDataSetChanged();
        Log.v(TAG, events.size() + "");
        Toast.makeText(context, "Deleted ", Toast.LENGTH_SHORT).show();
    }

    private void editEvent(Event event) {
        Intent intent = new Intent(context, EditEventActivity.class);
        intent.putExtra(EditEventActivity.KEY_EVENT, (Serializable) event);
        context.startActivity(intent);

    }
}
