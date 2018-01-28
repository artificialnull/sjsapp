package com.gabdeg.sjsapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.sufficientlysecure.htmltextview.HtmlTextView;

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

        new GetAssignmentTask().execute();

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
            ((HtmlTextView) findViewById(R.id.assignment_long))
                    .setHtml(assignment.getAssignmentLong());
        }
    }
}
