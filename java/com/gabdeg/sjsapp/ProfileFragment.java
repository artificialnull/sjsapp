package com.gabdeg.sjsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ishan on 2/13/18.
 */

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ((MainActivity) getActivity()).setNavigationChecked(R.id.drawer_profile);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");

        return view;

    }

}
