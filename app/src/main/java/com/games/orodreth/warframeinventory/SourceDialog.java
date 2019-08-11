package com.games.orodreth.warframeinventory;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_SOURCE;

public class SourceDialog extends AppCompatDialogFragment {

    private RadioButton nexusButton;
    private RadioButton marketButton;
    private SourceListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        int sourcselected = bundle.getInt(EXTRA_SOURCE);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.source_menu,null);

        builder.setView(view)
                .setTitle("Choose the Source")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int source = 0;
                        if(nexusButton.isChecked()) source = 0;
                        else if(marketButton.isChecked()) source = 1;
                        listener.selectSource(source);
                    }
                });

        nexusButton = view.findViewById(R.id.nav_nexus);
        marketButton = view.findViewById(R.id.nav_market);
        if (sourcselected==0)nexusButton.setChecked(true);
        else marketButton.setChecked(true);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (SourceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+ "must implement SourceListener");
        }
    }

    public interface SourceListener{
        void selectSource(int source);
    }
}
