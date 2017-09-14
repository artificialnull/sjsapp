package com.gabdeg.sjsapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ishan on 8/21/17.
 */

public class AssignmentFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public ArrayList<Browser.Assignment> assignments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignments, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.assignment_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetAssignmentsTask().execute();
                    }
                }
        );
        mRecyclerView = (RecyclerView) view.findViewById(R.id.assignment_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        new GetAssignmentsTask().execute();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Assignments");

        return view;

    }

    private class GetAssignmentsTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        protected Void doInBackground(Void... voids) {
            Browser browser = new Browser();
            browser.validateLogIn(
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .getString("username", "username"),
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .getString("password", "password")

            );

            assignments = browser.getAssignmentList();

            return null;
        }
        protected void onPostExecute(Void underscore) {
            mSwipeRefreshLayout.setRefreshing(false);
            mRecyclerView.setAdapter(new AssignmentAdapter());
        }
    }

    private class UpdateAssignmentStatusTask extends AsyncTask<Browser.Assignment, Void, Void> {
        protected Void doInBackground(Browser.Assignment... assignments) {
            Browser browser = new Browser();
            browser.validateLogIn(
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .getString("username", "username"),
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .getString("password", "password")

            );

            browser.updateAssignmentStatus(assignments[0]);

            return null;
        }
    }

    private class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mAssignmentClass;
            public TextView mAssignmentAssigned;
            public TextView mAssignmentDue;
            public TextView mAssignmentType;
            public HtmlTextView mAssignmentShort;
            public TextView mAssignmentStatus;
            public HtmlTextView mAssignmentLong;
            public ConstraintLayout mAssignmentLayout;


            public ViewHolder(View v) {
                super(v);
                mAssignmentClass = (TextView) v.findViewById(R.id.assignment_item_class);
                mAssignmentAssigned = (TextView) v.findViewById(R.id.assignment_item_assign);
                mAssignmentDue = (TextView) v.findViewById(R.id.assignment_item_due);
                mAssignmentStatus = (TextView) v.findViewById(R.id.assignment_item_status);
                mAssignmentType = (TextView) v.findViewById(R.id.assignment_item_type);
                mAssignmentShort = (HtmlTextView) v.findViewById(R.id.assignment_item_short);
                mAssignmentLong = (HtmlTextView) v.findViewById(R.id.assignment_item_long);
                mAssignmentLayout = (ConstraintLayout) v.findViewById(R.id.assignment_item_layout);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.assignment_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final String[] choices = {"To Do", "In Progress", "Completed"};
            final int[] colorChoices = {
                    R.color.toDoColor,
                    R.color.inProgressColor,
                    R.color.completedColor
            };
            final Browser.Assignment assignment = assignments.get(position);
            holder.mAssignmentClass.setText(assignment.getAssignmentClass());
            holder.mAssignmentAssigned.setText(assignment.getAssignmentAssigned().split(" ")[0]);
            holder.mAssignmentDue.setText(assignment.getAssignmentDue().split(" ")[0]);

            holder.mAssignmentStatus.setText(assignment.getAssignmentStatus());
            holder.mAssignmentStatus.setTextColor(
                    getResources().getColor(colorChoices[
                            Arrays.asList(choices)
                                    .indexOf(holder.mAssignmentStatus.getText())
                            ])
            );
            holder.mAssignmentStatus.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.mAssignmentStatus.setText(
                                    choices[
                                        (
                                                Arrays.asList(choices).indexOf(
                                                        holder.mAssignmentStatus.getText()
                                                ) + 1
                                        ) % 3
                                    ]
                            );
                            holder.mAssignmentStatus.setTextColor(
                                    getResources().getColor(colorChoices[
                                            Arrays.asList(choices)
                                                    .indexOf(holder.mAssignmentStatus.getText())
                                    ])
                            );
                            assignment.setAssignmentStatus(
                                    holder.mAssignmentStatus.getText().toString()
                            );
                            new UpdateAssignmentStatusTask().execute(assignment);
                        }
                    }
            );


            holder.mAssignmentLong.setHtml(assignment.getAssignmentLong());
            holder.mAssignmentLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (holder.mAssignmentLong.getVisibility()) {
                                case View.VISIBLE:
                                    holder.mAssignmentLong.setVisibility(View.GONE);
                                    break;
                                case View.GONE:
                                    if (!holder.mAssignmentLong.getText().toString().isEmpty()) {
                                        holder.mAssignmentLong.setVisibility(View.VISIBLE);
                                    }
                                    break;
                            }
                        }
                    }
            );

            holder.mAssignmentType.setText(assignment.getAssignmentType());
            holder.mAssignmentShort.setHtml(assignment.getAssignmentShort());
        }

        @Override
        public int getItemCount() {
            return assignments.size();
        }

    }

}
