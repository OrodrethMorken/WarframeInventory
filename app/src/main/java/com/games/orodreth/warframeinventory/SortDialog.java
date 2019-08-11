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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_INVERSE;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_NO_ZERO;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_SORT;
import static com.games.orodreth.warframeinventory.MainActivity.SORT_AZ;
import static com.games.orodreth.warframeinventory.MainActivity.SORT_DUC;
import static com.games.orodreth.warframeinventory.MainActivity.SORT_DUCPLAT;
import static com.games.orodreth.warframeinventory.MainActivity.SORT_PLAT;

public class SortDialog extends AppCompatDialogFragment {

    private RadioButton sort_az;
    private RadioButton sort_duc;
    private RadioButton sort_plat;
    private RadioButton sort_duc_plat;
    private CheckBox no_Zero;
    private CheckBox inverse;
    private SortListener listener;




    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Bundle bundle = getArguments();
        int sortselected = bundle.getInt(EXTRA_SORT);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sort_menu,null);


        builder.setView(view)
                .setTitle("Choose the Sorting method")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int sort = 0;
                        if(sort_az.isChecked()){
                            sort = SORT_AZ;
                        }else if (sort_duc.isChecked()){
                            sort = SORT_DUC;
                        }else if (sort_plat.isChecked()){
                            sort = SORT_PLAT;
                        }else if (sort_duc_plat.isChecked()){
                            sort = SORT_DUCPLAT;
                        }
                        bundle.clear();
                        bundle.putInt(EXTRA_SORT, sort);
                        if(no_Zero.isEnabled()){
                            bundle.putBoolean(EXTRA_NO_ZERO,no_Zero.isChecked());
                        }else {
                            bundle.putBoolean(EXTRA_NO_ZERO, no_Zero.isEnabled());
                        }
                        bundle.putBoolean(EXTRA_INVERSE,inverse.isChecked());
                        listener.sortSelected(bundle);
                    }
                });

        sort_az = view.findViewById(R.id.sort_az);
        sort_duc = view.findViewById(R.id.sort_duc);
        sort_plat = view.findViewById(R.id.sort_plat);
        sort_duc_plat = view.findViewById(R.id.sort_duc_plat);
        no_Zero = view.findViewById(R.id.no_zero);
        inverse = view.findViewById(R.id.inverse);

        switch (sortselected){
            case SORT_AZ:
                sort_az.setChecked(true);
                break;
            case SORT_DUC:
                sort_duc.setChecked(true);
                break;
            case SORT_PLAT:
                sort_plat.setChecked(true);
                break;
            case SORT_DUCPLAT:
                sort_duc_plat.setChecked(true);
                break;
            default:
                sort_az.setChecked(true);
        }
        sort_az.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { //manage avaiable of the no_zero checkbox
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                no_Zero.setEnabled(!isChecked);
            }
        });
        no_Zero.setEnabled(!sort_az.isChecked());
        no_Zero.setChecked(bundle.getBoolean(EXTRA_NO_ZERO));
        inverse.setChecked(bundle.getBoolean(EXTRA_INVERSE));

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (SortListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+ "must implement SortListener");
        }
    }

    public interface SortListener{
        void sortSelected(Bundle bundle);
    }
}
