package com.gabdeg.sjsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ishan on 2/14/18.
 */

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final Calendar sampleCalendar = Calendar.getInstance();
        sampleCalendar.set(2001, Calendar.FEBRUARY, 2, 13, 37, 0);

        MainActivity mainActivity = (MainActivity) getActivity();

        mainActivity.setMenuButtonVisibility(R.id.action_left, false);
        mainActivity.setMenuButtonVisibility(R.id.action_right, false);
        mainActivity.setMenuButtonVisibility(R.id.action_sort, false);

        mainActivity.setNavigationChecked(R.id.drawer_settings);

        mainActivity.getSupportActionBar().setTitle("Settings");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        final SharedPreferences.Editor editor = settings.edit();

        ((EditText) view.findViewById(R.id.settings_time_box)).setText(
                settings.getString("time_format", "HH:mm")
        );
        ((TextView) view.findViewById(R.id.settings_time_preview)).setText(
                new SimpleDateFormat(settings.getString("time_format", "HH:mm"))
                        .format(sampleCalendar.getTime())
        );
        ((EditText) view.findViewById(R.id.settings_date_box)).setText(
                settings.getString("date_format", "yyyy-MM-dd")
        );
        ((TextView) view.findViewById(R.id.settings_date_preview)).setText(
                new SimpleDateFormat(settings.getString("date_format", "yyyy-MM-dd"))
                        .format(sampleCalendar.getTime())
        );


        view.findViewById(R.id.settings_time_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeFormatString = ((EditText) view.findViewById(R.id.settings_time_box))
                        .getText().toString();
                editor.putString("time_format", timeFormatString);
                editor.apply();

                ((TextView) view.findViewById(R.id.settings_time_preview)).setText(
                        new SimpleDateFormat(timeFormatString).format(sampleCalendar.getTime())
                );

                Toast.makeText(getContext(), "Time format saved", Toast.LENGTH_SHORT).show();

            }
        });

        view.findViewById(R.id.settings_date_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dateFormatString = ((EditText) view.findViewById(R.id.settings_date_box))
                        .getText().toString();
                editor.putString("date_format", dateFormatString);
                editor.apply();

                ((TextView) view.findViewById(R.id.settings_date_preview)).setText(
                        new SimpleDateFormat(dateFormatString).format(sampleCalendar.getTime())
                );

                Toast.makeText(getContext(), "Date format saved", Toast.LENGTH_SHORT).show();

            }
        });



        return view;
    }
}
