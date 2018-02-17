package com.gabdeg.sjsapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by ishan on 8/21/17.
 */

public class AssignmentFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    float minDateWidth;

    public ArrayList<Browser.Assignment> assignments;
    public static Comparator<Browser.Assignment> chosenComparator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignments, container, false);

        ((MainActivity) getActivity()).setNavigationChecked(R.id.drawer_assignments);
        ((MainActivity) getActivity()).setMenuButtonVisibility(R.id.action_left, false);
        ((MainActivity) getActivity()).setMenuButtonVisibility(R.id.action_right, false);
        ((MainActivity) getActivity()).setMenuButtonVisibility(R.id.action_sort, true);

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

        if (chosenComparator == null) {
            chosenComparator = new DueComparator();
        }


        Calendar sampleCalendar = Calendar.getInstance();
        sampleCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        sampleCalendar.set(Calendar.DAY_OF_MONTH, 31);
        // set a calendar to 12-31 so that we can find the min width of our date section
        // so that we can guarantee proper formatting no matter what the user chooses as their
        // date format

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView dateWidthTextView = new TextView(getContext());
        dateWidthTextView.setText(
                new SimpleDateFormat(
                        PreferenceManager.getDefaultSharedPreferences(getContext())
                                .getString("date_format", "yyyy-MM-dd")
                ).format(sampleCalendar.getTime())
        );

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        dateWidthTextView.setPadding(
                (int) (displayMetrics.density * 16 + 0.5f), 0, 0, 0);
        linearLayout.addView(dateWidthTextView);

        dateWidthTextView.measure(0, 0);
        minDateWidth = dateWidthTextView.getMeasuredWidth() / ((float) displayMetrics.widthPixels);

        Log.v("MIN_DAte_WIDTH", String.valueOf(minDateWidth));

        new GetAssignmentsTask().execute();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Assignments");

        return view;

    }

    public class DueComparator implements Comparator<Browser.Assignment> {
        public int compare(Browser.Assignment o1, Browser.Assignment o2) {
            Date o1Date = o1.getAssignmentDue();
            Date o2Date = o2.getAssignmentDue();
            return o1Date.compareTo(o2Date);
        }
    }

    public class AssignedComparator implements Comparator<Browser.Assignment> {
        public int compare(Browser.Assignment o1, Browser.Assignment o2) {
            Date o1Date = o1.getAssignmentAssigned();
            Date o2Date = o2.getAssignmentAssigned();
            return o1Date.compareTo(o2Date);
        }
    }

    public class ClassComparator implements Comparator<Browser.Assignment> {
        public int compare(Browser.Assignment o1, Browser.Assignment o2) {
            return o1.getAssignmentClass().compareTo(o2.getAssignmentClass());
        }
    }

    public void showSortByMenu(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sorting, popup.getMenu());
        popup.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.due_sort:
                                chosenComparator = new DueComparator();
                                break;
                            case R.id.assigned_sort:
                                chosenComparator = new AssignedComparator();
                                break;
                            case R.id.class_sort:
                                chosenComparator = new ClassComparator();
                                break;
                            default:
                                return false;

                        }
                        new GetAssignmentsTask().execute();
                        return true;
                    }
                }
        );
        popup.show();
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
            Collections.sort(assignments, chosenComparator);
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
            public Guideline mAssignmentGuideline;

            public ViewHolder(View v) {
                super(v);
                mAssignmentClass = (TextView) v.findViewById(R.id.assignment_item_class);
                mAssignmentAssigned = (TextView) v.findViewById(R.id.assignment_item_assign);
                mAssignmentDue = (TextView) v.findViewById(R.id.assignment_item_due);
                mAssignmentStatus = (TextView) v.findViewById(R.id.assignment_item_status);
                mAssignmentType = (TextView) v.findViewById(R.id.assignment_item_type);
                mAssignmentShort = (HtmlTextView) v.findViewById(R.id.assignment_item_short);
                mAssignmentGuideline = (Guideline) v.findViewById(R.id.name_time_sep);
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
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                    holder.mAssignmentGuideline.getLayoutParams();
            params.guidePercent = minDateWidth;
            holder.mAssignmentGuideline.setLayoutParams(params);

            final String[] choices = {"To Do", "In Progress", "Completed", "Overdue",
                    "Unknown", "Graded"};
            final int[] colorChoices = {
                    R.color.toDoColor,
                    R.color.inProgressColor,
                    R.color.completedColor,
                    R.color.overdueColor,
                    R.color.unknownColor,
                    R.color.gradedColor
            };
            final Browser.Assignment assignment = assignments.get(position);
            holder.mAssignmentClass.setText(assignment.getAssignmentClass());

            SimpleDateFormat toFormat = new SimpleDateFormat(
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString("date_format", "yyyy-MM-dd")
            );

            holder.mAssignmentAssigned.setText(toFormat.format(assignment.getAssignmentAssigned()));
            holder.mAssignmentDue.setText(toFormat.format((assignment.getAssignmentDue())));

            holder.mAssignmentStatus.setText(assignment.getAssignmentStatus());
            holder.mAssignmentStatus.setTextColor(
                    getResources().getColor(colorChoices[
                            Arrays.asList(choices)
                                    .indexOf(holder.mAssignmentStatus.getText())
                            ])
            );
            if (!assignment.getAssignmentStatus().equals("Graded")) {
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
            }

            holder.mAssignmentType.setText(assignment.getAssignmentType());
            holder.mAssignmentShort.setHtml(assignment.getAssignmentShort());
            holder.mAssignmentShort.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), AssignmentActivity.class);
                            intent.putExtra(AssignmentActivity.ASSIGNMENT_ID, assignment);
                            startActivity(intent);
                        }
                    }
            );

        }

        @Override
        public int getItemCount() {
            return assignments.size();
        }

    }

}
