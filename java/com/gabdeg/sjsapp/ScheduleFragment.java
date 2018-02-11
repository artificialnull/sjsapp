package com.gabdeg.sjsapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ishan on 8/21/17.
 */

public class ScheduleFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppCompatActivity mContext;

    public ArrayList<Browser.ScheduledClass> scheduledClasses;
    Calendar calendar = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        ((MainActivity) getActivity()).setNavigationChecked(R.id.drawer_schedule);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.schedule_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetScheduleTask().execute();
                    }
                }
        );
        mRecyclerView = (RecyclerView) view.findViewById(R.id.schedule_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        mContext = (AppCompatActivity) getActivity();
        new GetScheduleTask().execute();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        new GetScheduleTask().execute();
    }

    public void travelBackInTime() {
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        new GetScheduleTask().execute();

    }
    public void travelForwardInTime() {
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        new GetScheduleTask().execute();
    }

    private class GetScheduleTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                    new SimpleDateFormat("E - MMM d, yyyy").format(calendar.getTime())
            );
        }
        protected Void doInBackground(Void... voids) {
            Browser browser = new Browser();
            browser.validateLogIn(
                    PreferenceManager.getDefaultSharedPreferences(mContext)
                            .getString("username", "username"),
                    PreferenceManager.getDefaultSharedPreferences(mContext)
                            .getString("password", "password")

            );
            scheduledClasses = browser.getScheduleList(calendar.getTime());

            if (scheduledClasses.size() == 0) {
                scheduledClasses.add(
                        new Browser.ScheduledClass()
                                .setClassName("Nothing scheduled")
                                .setClassStart("8:30 AM")
                                .setClassEnd("3:35 PM")
                                .setClassTeacher("SJS")
                                .setClassRoom("")
                                .setClassBlock("")
                );
            }
            return null;
        }
        protected void onPostExecute(Void underscore) {
            mRecyclerView.setAdapter(new ScheduleAdapter());
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mClassName;
            public TextView mClassStart;
            public TextView mClassEnd;
            public TextView mClassBlock;
            public TextView mClassRoom;
            public TextView mClassTeacher;
            public View mClassHighlight;

            public ViewHolder(View v) {
                super(v);
                mClassName = (TextView) v.findViewById(R.id.schedule_item_name);
                mClassStart = (TextView) v.findViewById(R.id.schedule_item_start);
                mClassEnd = (TextView) v.findViewById(R.id.schedule_item_end);
                mClassBlock = (TextView) v.findViewById(R.id.schedule_item_block);
                mClassRoom = (TextView) v.findViewById(R.id.schedule_item_room);
                mClassTeacher = (TextView) v.findViewById(R.id.schedule_item_teacher);
                mClassHighlight = (View) v.findViewById(R.id.schedule_item_highlight);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Browser.ScheduledClass scheduledClass = scheduledClasses.get(position);
            holder.mClassName.setText(scheduledClass.getClassName());

            SimpleDateFormat toFormat = new SimpleDateFormat("HH:mm");
            holder.mClassStart.setText(
                    toFormat.format(scheduledClass.getClassStart())
            );
            holder.mClassEnd.setText(
                    toFormat.format(scheduledClass.getClassEnd())
            );

            Calendar today = Calendar.getInstance();
            Calendar calStart = Calendar.getInstance();
            calStart.setTime(scheduledClass.getClassStart());
            calStart.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(scheduledClass.getClassEnd());
            calEnd.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));


            if (today.getTime().after(calEnd.getTime())) {
                holder.mClassHighlight.setBackgroundResource(R.color.completedColor);
            } else if (today.getTime().before(calStart.getTime())) {
                holder.mClassHighlight.setBackgroundResource(R.color.toDoColor);
            } else {
                holder.mClassHighlight.setBackgroundResource(R.color.inProgressColor);
            }


            holder.mClassBlock.setText(scheduledClass.getClassBlock());
            holder.mClassRoom.setText(scheduledClass.getClassRoom());
            holder.mClassTeacher.setText(scheduledClass.getClassTeacher());
        }

        @Override
        public int getItemCount() {
            return scheduledClasses.size();
        }

    }

}
