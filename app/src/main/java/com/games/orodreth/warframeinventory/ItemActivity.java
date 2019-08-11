package com.games.orodreth.warframeinventory;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_DUCATS;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_DUC_PLAT;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_NAME;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_PLATINUM;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_SOURCE;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_STORAGE;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_URL;

public class ItemActivity extends AppCompatActivity implements View.OnClickListener {

    private int storage;
    private String itemName;
    private TextView storageDisplay;
    private NumberPicker np;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = findViewById(R.id.toolbar_item);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        int source = intent.getIntExtra(EXTRA_SOURCE,0);
        String imageUrl;
        if(source==0){
            imageUrl = "https://cdn.warframestat.us/img/";
        }else {
            imageUrl = "https://warframe.market/static/assets/";
        }
        imageUrl += intent.getStringExtra(EXTRA_URL);
        itemName = intent.getStringExtra(EXTRA_NAME);
        int itemDucs = intent.getIntExtra(EXTRA_DUCATS,0);
        int itemPlats = intent.getIntExtra(EXTRA_PLATINUM,0);
        float itemDucPlat = intent.getFloatExtra(EXTRA_DUC_PLAT,0);
        storage = intent.getIntExtra(EXTRA_STORAGE, 0);

        getSupportActionBar().setTitle(itemName);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.item_image_view);
        TextView nameView = findViewById(R.id.item_view_name);
        TextView ducView = findViewById(R.id.item_view_duc);
        TextView platView = findViewById(R.id.item_view_plat);
        TextView ducPlatView = findViewById(R.id.item_view_duc_plat);
        storageDisplay = findViewById(R.id.storage_value);
        np = findViewById(R.id.number_pick);

        AppCompatButton minusButton = findViewById(R.id.minus_button);
        AppCompatButton plusButton = findViewById(R.id.plus_button);

        Picasso.with(this).load(imageUrl).fit().centerInside().into(imageView);
        nameView.setText(itemName);
        ducView.setText(String.format(Locale.getDefault(),"%s%d", this.getResources().getString(R.string.ducats), itemDucs));
        platView.setText(String.format(Locale.getDefault(),"%s%d", this.getResources().getString(R.string.platinum), itemPlats));
        ducPlatView.setText(String.format(Locale.getDefault(),"%s%.2f", this.getResources().getString(R.string.duc_plat), itemDucPlat));
        np.setMinValue(0);
        np.setMaxValue(1000);
        np.setValue(storage);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                storage = np.getValue();
            }
        });
        np.setOnClickListener(this);
        storageDisplay.setText(String.valueOf(storage));
        storageDisplay.setOnClickListener(this);

        minusButton.setOnClickListener(this);
        plusButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.number_pick:
                np.setVisibility(View.GONE);
                storageDisplay.setVisibility(View.VISIBLE);
                break;
            case R.id.storage_value:
                storageDisplay.setVisibility(View.GONE);
                np.setVisibility(View.VISIBLE);
                break;
            case R.id.minus_button:
                if(storage==0){
                    Toast.makeText(this,"No item to be removed", Toast.LENGTH_SHORT).show();
                }else {
                    storage--;
                }
                break;
            case R.id.plus_button:
                storage++;
                break;
        }
        storageDisplay.setText(String.valueOf(storage));
        np.setValue(storage);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_STORAGE, storage);
        resultIntent.putExtra(EXTRA_NAME, itemName);
        setResult(RESULT_OK, resultIntent); //require int RESULT_CODE, intent to send back
        finish();
        super.onBackPressed();
    }

    private void showKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_STORAGE, storage);
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     *
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        storage = savedInstanceState.getInt(EXTRA_STORAGE);
        storageDisplay.setText(String.valueOf(storage));
        np.setValue(storage);
    }
}
