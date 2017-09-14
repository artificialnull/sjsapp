package com.gabdeg.sjsapp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by ishan on 8/21/17.
 */

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    private MenuItem scheduleLeftArrow;
    private MenuItem scheduleRightArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(R.string.app_name);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle(R.string.drawer_name);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(
                new ArrayAdapter<String>(this, R.layout.drawer_list_item,
                        getResources().getStringArray(R.array.drawer_items))
        );


        mDrawerList.addHeaderView(getLayoutInflater()
                .inflate(R.layout.drawer_header, mDrawerList, false), null, false);

        mDrawerList.setItemChecked(0, true);

        mDrawerList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mDrawerList.setItemChecked(position, true);
                        mDrawerLayout.closeDrawer(mDrawerList);
                        FragmentTransaction fragmentTransaction =
                                getSupportFragmentManager().beginTransaction();

                        switch (getResources().getStringArray(R.array.drawer_items)[position - 1]) {
                            case "Schedule":
                                Log.v("SCHEDULE", "Opening...");

                                fragmentTransaction.replace(R.id.content_frame,
                                        new ScheduleFragment());
                                fragmentTransaction.commit();

                                scheduleLeftArrow.setVisible(true);
                                scheduleRightArrow.setVisible(true);

                                break;
                            case "Assignments":
                                Log.v("ASSIGNMENTS", "Opening...");

                                fragmentTransaction.replace(R.id.content_frame,
                                        new AssignmentFragment());
                                fragmentTransaction.commit();

                                scheduleLeftArrow.setVisible(false);
                                scheduleRightArrow.setVisible(false);

                                break;
                            default:
                                break;
                        }

                    }

                }
        );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("password", "").isEmpty()) {
            showLoginDialog(false);
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, new ScheduleFragment());
            ft.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.schedule, menu);
        scheduleLeftArrow = menu.findItem(R.id.action_left);
        scheduleRightArrow = menu.findItem(R.id.action_right);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        ScheduleFragment sf;
        switch (item.getItemId()) {
            case R.id.action_left:
                sf = (ScheduleFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.content_frame);
                sf.travelBackInTime();
                return true;
            case R.id.action_right:
                sf = (ScheduleFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.content_frame);
                sf.travelForwardInTime();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void showLoginDialog(boolean showAsInvalid) {
        LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        if (showAsInvalid) {
            loginDialogFragment.showInvalidBanner();
        }
        loginDialogFragment.show(getFragmentManager(), "login");
    }

    public void onCredentialsConfirmed(String username, String password) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new ScheduleFragment());
        ft.commit();
    }

    public void onCredentialsGotten(String username, String password) {
        new ValidateCredentialsTask().execute(username, password);
    }

    private class ValidateCredentialsTask extends AsyncTask<String, Void, Boolean> {
        private String username;
        private String password;
        protected Boolean doInBackground(String... credentials) {
            Browser browser = new Browser();
            username = credentials[0];
            password = credentials[1];
            return browser.checkCredentialLegitimacy(username, password);
        }
        protected void onPostExecute(Boolean credentialsValid) {
            if (!credentialsValid) {
                showLoginDialog(true);
            } else {
                onCredentialsConfirmed(username, password);
            }
        }

    }

}