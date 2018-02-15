package com.gabdeg.sjsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ishan on 8/22/17.
 */

public class LoginDialogFragment extends DialogFragment {

    boolean invalid = false;

    public void showInvalidBanner() {
        invalid = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_login_dialog, null);
        if (invalid) {
            ((TextView) view.findViewById(R.id.header_text)).setText("Try Again");
        }
        builder.setView(view)
                .setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).onCredentialsGotten(
                                ((TextView) getDialog().findViewById(R.id.username)).getText().toString(),
                                ((TextView) getDialog().findViewById(R.id.password)).getText().toString()
                        );

                    }
                });


        return builder.create();
    }



}

