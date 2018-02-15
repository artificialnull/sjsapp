package com.gabdeg.sjsapp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ishan on 8/21/17.
 */

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;

    private MenuItem scheduleLeftArrow;
    private MenuItem scheduleRightArrow;
    private MenuItem assignmentSorting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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

        mNavigationView = (NavigationView) findViewById(R.id.drawer_navigation);

        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        FragmentTransaction fragmentTransaction =
                                getSupportFragmentManager().beginTransaction();

                        switch (item.getTitle().toString()) {
                            case "Schedule":
                                Log.v("SCHEDULE", "Opening...");

                                fragmentTransaction.replace(R.id.content_frame,
                                        new ScheduleFragment());
                                fragmentTransaction.addToBackStack(null).commit();

                                scheduleLeftArrow.setVisible(true);
                                scheduleRightArrow.setVisible(true);
                                assignmentSorting.setVisible(false);

                                break;
                            case "Assignments":
                                Log.v("ASSIGNMENTS", "Opening...");

                                fragmentTransaction.replace(R.id.content_frame,
                                        new AssignmentFragment());
                                fragmentTransaction.addToBackStack(null).commit();

                                scheduleLeftArrow.setVisible(false);
                                scheduleRightArrow.setVisible(false);
                                assignmentSorting.setVisible(true);

                                break;

                            case "Profile":
                                Log.v("PROFILE", "Opening...");

                                fragmentTransaction.replace(R.id.content_frame,
                                        new ProfileFragment());
                                fragmentTransaction.addToBackStack(null).commit();

                                scheduleLeftArrow.setVisible(false);
                                scheduleRightArrow.setVisible(false);
                                assignmentSorting.setVisible(false);

                                break;

                            case "Settings":
                                Log.v("SETTINGS", "Opening...");

                                fragmentTransaction
                                        .replace(R.id.content_frame, new SettingsFragment())
                                        .addToBackStack(null)
                                        .commit();

                                scheduleLeftArrow.setVisible(false);
                                scheduleRightArrow.setVisible(false);
                                assignmentSorting.setVisible(false);

                                break;

                            default:
                                break;
                        }

                        mDrawerLayout.closeDrawer(mNavigationView);
                        return true;
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

        new GetUserMetadataTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        scheduleLeftArrow = menu.findItem(R.id.action_left);
        scheduleRightArrow = menu.findItem(R.id.action_right);
        assignmentSorting = menu.findItem(R.id.action_sort);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        ScheduleFragment sf;
        AssignmentFragment af;
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
            case R.id.action_sort:
                af = (AssignmentFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.content_frame);
                af.showSortByMenu(findViewById(R.id.action_sort));
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
        mDrawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    public void setNavigationChecked(int resourceID) {
        Log.v("SET_CHECKED", String.valueOf(resourceID));
        mNavigationView.setCheckedItem(resourceID);
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

    private class GetUserMetadataTask extends AsyncTask<Void, Void, Browser.User> {
        protected Browser.User doInBackground(Void... voids) {
            Browser browser = new Browser();
            browser.validateLogIn(
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                            .getString("username", "username"),
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                            .getString("password", "password")

            );
            return browser.getUserData();
        }
        protected void onPostExecute(Browser.User user) {
            ((TextView) findViewById(R.id.drawer_header_name)).setText(user.getUserFirstName());
            ((TextView) findViewById(R.id.drawer_header_email)).setText(user.getUserEmail());
        }
    }

}