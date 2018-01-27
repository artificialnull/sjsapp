package com.gabdeg.sjsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

/**
 * Created by ishan on 1/27/18.
 */

public class AssignmentActivity extends AppCompatActivity {

    public static String ASSIGNMENT_ID = "ASSIGNMENT_ID";
    int assignmentID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        assignmentID = intent.getIntExtra(ASSIGNMENT_ID, 0);

        Toast.makeText(this, String.valueOf(assignmentID), Toast.LENGTH_SHORT).show();



    }
}
