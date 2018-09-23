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
import com.makhovyk.mykhailo.reminder.EventDetailsActivity;
import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.model.ListItem;
import com.makhovyk.mykhailo.reminder.model.Separator;
import com.makhovyk.mykhailo.reminder.notifications.AlarmHelper;
import com.makhovyk.mykhailo.reminder.utils.Constants;
import com.makhovyk.mykhailo.reminder.utils.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> events;
    private final String TAG = "zaebalo";
    private Context context;


    class BirthdayHolder extends EventViewHolder {

        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tv_age)
        TextView tvAge;
        @BindView(R.id.tv_type)
        TextView tvType;


        BirthdayHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindEvent(Event event) {
            super.bindEvent(event);
            tvName.setText(event.getPersonName());
            tvDate.setText(Utils.getformattedDate(event.getDate(), event.isYearUnknown()));
            tvType.setText(event.getType());
            int age = Utils.getAge(event.getYear());
            if (!event.isYearUnknown() && age >= 0) {
                tvAge.setText(String.valueOf(age));
            } else {
                tvAge.setText("--");
            }
            tvAge.setBackgroundResource(Utils.getAgeCircleDrawable(age));
        }

    }

    class OtherEventHolder extends EventViewHolder {

        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tv_event_name)
        TextView tvEventName;
        @BindView(R.id.tv_age)
        TextView tvAge;

        OtherEventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindEvent(Event event) {
            super.bindEvent(event);
            tvName.setText(event.getPersonName());
            tvDate.setText(Utils.getformattedDate(event.getDate(), event.isYearUnknown()));
            tvEventName.setText(event.getEventName());
            int age = Utils.getAge(event.getYear());
            if (!event.isYearUnknown() && age >= 0) {
                tvAge.setText(String.valueOf(age));
            } else {
                tvAge.setText("--");
            }
            tvAge.setBackgroundResource(Utils.getAgeCircleDrawable(age));

        }
    }

    class SeparatorHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_month)
        TextView tvMonth;

        SeparatorHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindEvent(Separator separator) {
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
                v = inflater.inflate(R.layout.item_birthday_anniversary, parent, false);
                return new BirthdayHolder(v);
            case 3:
                v = inflater.inflate(R.layout.item_other_event, parent, false);
                return new OtherEventHolder(v);
            case 5:
                v = inflater.inflate(R.layout.item_separator, parent, false);
                return new SeparatorHolder(v);
            default:
                v = inflater.inflate(R.layout.item_birthday_anniversary, parent, false);
                return new BirthdayHolder(v);
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        Event e;
        switch (holder.getItemViewType()) {
            case 1:
                e = (Event) events.get(position);
                BirthdayHolder bh = (BirthdayHolder) holder;
                bh.bindEvent((Event) events.get(position));
                break;
            case 3:
                e = (Event) events.get(position);
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
                    PopupMenu popup = new PopupMenu(view.getContext(), view, Gravity.END);
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

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!events.get(position).isSeparator()) {
                    Intent intent = new Intent(context, EventDetailsActivity.class);
                    intent.putExtra(Constants.EVENT, (Event) events.get(position));
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position) {

        if (events.get(position).isSeparator()) {
            return 5;
        }
        Event event = (Event) events.get(position);
        final String typeBirthday = context.getResources().getString(R.string.type_birthday);
        final String typeAnniversary = context.getResources().getString(R.string.type_anniversary);
        final String typeOtherEvent = context.getResources().getString(R.string.type_other_event);

        if (event.getType().equals(typeBirthday) || event.getType().equals(typeAnniversary)) {
            return 1;
        } else {
            return 3;
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
        new AlarmHelper(context).deleteAlarm(event.getTimestamp());
        removeRedundantSeparators();
        notifyDataSetChanged();
        Toast.makeText(context, context.getString(R.string.msg_deleted), Toast.LENGTH_SHORT).show();
    }

    private void editEvent(Event event) {
        Intent intent = new Intent(context, EditEventActivity.class);
        intent.putExtra(EditEventActivity.KEY_EVENT, (Serializable) event);
        context.startActivity(intent);

    }

    private void removeRedundantSeparators() {
        for (int i = 0; i < events.size() - 1; i++) {
            if (events.get(i).isSeparator() && events.get(i + 1).isSeparator()) {
                events.remove(i);
            }
        }
        if (events.get(events.size() - 1).isSeparator()) {
            events.remove(events.size() - 1);
        }
    }
}
