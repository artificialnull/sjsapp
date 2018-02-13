package com.gabdeg.sjsapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ishan on 2/13/18.
 */

public class ProfileFragment extends Fragment {

    TextView profileFullName;
    TextView profileEmail;
    TextView profileStudentID;
    TextView profileLockerNumber;
    TextView profileLockerCombo;
    ImageView profilePhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ((MainActivity) getActivity()).setNavigationChecked(R.id.drawer_profile);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");

        profileFullName = (TextView) view.findViewById(R.id.profile_photo_title);
        profileEmail = (TextView) view.findViewById(R.id.profile_photo_caption);
        profileStudentID = (TextView) view.findViewById(R.id.profile_personal_id_info);
        profileLockerNumber = (TextView) view.findViewById(R.id.profile_personal_locker_info);
        profileLockerCombo = (TextView) view.findViewById(R.id.profile_personal_combo_info);
        profilePhoto = (ImageView) view.findViewById(R.id.profile_photo_photo);

        new GetUserMetadataTask().execute();

        return view;

    }

    private class GetUserMetadataTask extends AsyncTask<Void, Void, Browser.User> {
        protected Browser.User doInBackground(Void... voids) {
            Browser browser = new Browser();
            browser.validateLogIn(
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .getString("username", "username"),
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .getString("password", "password")

            );
            return browser.getUserData();
        }
        protected void onPostExecute(Browser.User user) {
            String fullName = user.getUserFirstName();
            if (!user.getUserMiddleName().isEmpty()) {
                fullName += " " + user.getUserMiddleName();
            }
            if (!user.getUserLastName().isEmpty()) {
                fullName += " " + user.getUserLastName();
            }
            profileFullName.setText(fullName);
            profileEmail.setText(user.getUserEmail());
            profileStudentID.setText(user.getUserStudentID());
            profileLockerNumber.setText(user.getUserLockerNumber());
            profileLockerCombo.setText(user.getUserLockerCombo());
        }
    }

}
