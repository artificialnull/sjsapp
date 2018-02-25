package com.gabdeg.sjsapp;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.text.SimpleDateFormat;

/**
 * Created by ishan on 1/27/18.
 */

public class AssignmentActivity extends AppCompatActivity {

    public static String ASSIGNMENT_ID = "ASSIGNMENT_ID";
    Browser.Assignment assignment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Details");

        Intent intent = getIntent();
        assignment = (Browser.Assignment) intent.getSerializableExtra(ASSIGNMENT_ID);

        final Button statusButton = (Button) findViewById(R.id.assignment_status);

        statusButton.setText(assignment.getAssignmentStatus().getStatusDescription());
        statusButton.getBackground().setColorFilter(
                getResources().getColor(assignment.getAssignmentStatus().getStatusColor()),
                PorterDuff.Mode.MULTIPLY
        );
        statusButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        assignment.setAssignmentStatus(assignment.getNextAssignmentStatus());
                        statusButton.setText(
                                assignment.getAssignmentStatus().getStatusDescription()
                        );
                        statusButton.getBackground().setColorFilter(
                                getResources().getColor(assignment.getAssignmentStatus().getStatusColor()),
                                PorterDuff.Mode.MULTIPLY
                        );
                        new UpdateAssignmentStatusTask().execute();
                    }
                }
        );


        new GetAssignmentTask().execute();

    }

    private class UpdateAssignmentStatusTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            Browser browser = new Browser();
            browser.validateLogIn(
                    PreferenceManager.getDefaultSharedPreferences(AssignmentActivity.this)
                            .getString("username", "username"),
                    PreferenceManager.getDefaultSharedPreferences(AssignmentActivity.this)
                            .getString("password", "password")

            );

            browser.updateAssignmentStatus(assignment);

            return null;
        }
    }

    private class GetAssignmentTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            Browser browser = new Browser();
            browser.validateLogIn(
                    PreferenceManager.getDefaultSharedPreferences(AssignmentActivity.this)
                            .getString("username", "username"),
                    PreferenceManager.getDefaultSharedPreferences(AssignmentActivity.this)
                            .getString("password", "password")
            );

            assignment = browser.getFullAssignment(assignment);

            return null;
        }

        protected void onPostExecute(Void underscore) {
            ((HtmlTextView) findViewById(R.id.assignment_short))
                    .setHtml(assignment.getAssignmentShort());

            SimpleDateFormat toFormat = new SimpleDateFormat(
                    PreferenceManager.getDefaultSharedPreferences(AssignmentActivity.this)
                            .getString("date_format", "yyyy-MM-dd")
                    + " " +
                    PreferenceManager.getDefaultSharedPreferences(AssignmentActivity.this)
                            .getString("time_format", "HH:mm")
            );

            ((TextView) findViewById(R.id.assignment_class_info))
                    .setText(assignment.getAssignmentClass());
            ((TextView) findViewById(R.id.assignment_assigned_info))
                    .setText(toFormat.format(
                            assignment.getAssignmentAssigned()));
            ((TextView) findViewById(R.id.assignment_due_info))
                    .setText(toFormat.format(
                            assignment.getAssignmentDue()));

            if (assignment.getAssignmentLong().isEmpty()) {
                findViewById(R.id.assignment_long).setVisibility(View.GONE);
            } else {
                ((HtmlTextView) findViewById(R.id.assignment_long))
                        .setHtml(assignment.getAssignmentLong());
            }

            if (assignment.getDownloads().isEmpty()) {
                findViewById(R.id.assignment_download).setVisibility(View.GONE);
            } else {
                findViewById(R.id.assignment_download).setVisibility(View.VISIBLE);
                ((RecyclerView) findViewById(R.id.assignment_download_list))
                        .setLayoutManager(new LinearLayoutManager(AssignmentActivity.this));
                ((RecyclerView) findViewById(R.id.assignment_download_list))
                        .addItemDecoration(new DividerItemDecoration(
                                findViewById(R.id.assignment_download_list).getContext(),
                        DividerItemDecoration.VERTICAL));

                ((RecyclerView) findViewById(R.id.assignment_download_list)).setAdapter(new DownloadAdapter());
            }

            if (assignment.getLinks().isEmpty()) {
                findViewById(R.id.assignment_link).setVisibility(View.GONE);
            } else {
                findViewById(R.id.assignment_link).setVisibility(View.VISIBLE);
                ((RecyclerView) findViewById(R.id.assignment_link_list))
                        .setLayoutManager(new LinearLayoutManager(AssignmentActivity.this));
                ((RecyclerView) findViewById(R.id.assignment_link_list))
                        .addItemDecoration(new DividerItemDecoration(
                                findViewById(R.id.assignment_link_list).getContext(),
                                DividerItemDecoration.VERTICAL));

                ((RecyclerView) findViewById(R.id.assignment_link_list)).setAdapter(new LinkAdapter());

            }
        }
    }

    private class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mDownloadTitle;
            public Button mDownloadButton;

            public ViewHolder(View v) {
                super(v);
                mDownloadTitle = (TextView) v.findViewById(R.id.extra_title);
                mDownloadButton = (Button) v.findViewById(R.id.extra_button);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.extra_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Browser.Assignment.Download download = assignment.getDownloads().get(position);

            holder.mDownloadTitle.setText(download.getName());
            holder.mDownloadButton.setText("download");
            holder.mDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Browser browser = new Browser();
                    browser.downloadFile(AssignmentActivity.this, download);
                }
            });
        }

        @Override
        public int getItemCount() {
            return assignment.getDownloads().size();
        }
    }

    private class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mLinkTitle;
            public Button mLinkButton;

            public ViewHolder(View v) {
                super(v);
                mLinkTitle = (TextView) v.findViewById(R.id.extra_title);
                mLinkButton = (Button) v.findViewById(R.id.extra_button);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.extra_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mLinkTitle.setText(assignment.getLinks().get(position).getName());
            holder.mLinkButton.setText("open");
            holder.mLinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            assignment.getLinks().get(position).getUrl()
                    )));
                }
            });
        }

        @Override
        public int getItemCount() {
            return assignment.getLinks().size();
        }
    }
}
