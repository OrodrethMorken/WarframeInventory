package com.games.orodreth.warframeinventory;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.ItemsAndInventory;
import com.squareup.picasso.Picasso;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_ID;

public class ItemActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView storageDisplay;
    private NumberPicker np;
    private ItemsAndInventory item;
    private int id;
    private int quantity;
    private boolean hadQuantity;
    private ItemActivityViewModel viewModel;
    private ImageView imageView;
    private TextView nameView;
    private TextView ducView;
    private TextView platView;
    private TextView ducPlatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = findViewById(R.id.toolbar_item);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_ID,0);
        final String[] imageUrl = {"https://cdn.warframestat.us/img/"};
        viewModel = ViewModelProviders.of(this).get(ItemActivityViewModel.class);
        viewModel.getCatalog(id).observe(this, new Observer<List<ItemsAndInventory>>() {
            @Override
            public void onChanged(List<ItemsAndInventory> itemsAndInventories) {
                item = itemsAndInventories.get(0);
                hadQuantity = item.inventory != null;

                getSupportActionBar().setTitle(item.items.getName());

                imageUrl[0] += item.items.getImageUrl();

                Picasso.with(ItemActivity.this).load(imageUrl[0]).fit().centerInside().into(imageView);
                nameView.setText(item.items.getName());
                ducView.setText(String.format(Locale.getDefault(),"%s%d", ItemActivity.this.getResources().getString(R.string.ducats), item.items.getDucat()));
                platView.setText(String.format(Locale.getDefault(),"%s%d", ItemActivity.this.getResources().getString(R.string.platinum), item.items.getPlat()));
                ducPlatView.setText(String.format(Locale.getDefault(),"%s%.2f", ItemActivity.this.getResources().getString(R.string.duc_plat), item.items.getDucPlat()));
                np.setMinValue(0);
                np.setMaxValue(1000);
                if(item.inventory != null) {
                    quantity = item.inventory.getQuantity();
                } else {
                    quantity = 0;
                }
                np.setValue(quantity);

                storageDisplay.setText(String.valueOf(quantity));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.item_image_view);
        nameView = findViewById(R.id.item_view_name);
        ducView = findViewById(R.id.item_view_duc);
        platView = findViewById(R.id.item_view_plat);
        ducPlatView = findViewById(R.id.item_view_duc_plat);
        storageDisplay = findViewById(R.id.storage_value);
        np = findViewById(R.id.number_pick);

        AppCompatButton minusButton = findViewById(R.id.minus_button);
        AppCompatButton plusButton = findViewById(R.id.plus_button);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                quantity = np.getValue();
            }
        });
        np.setOnClickListener(this);
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
                if(quantity==0){
                    Toast.makeText(this,"No item to be removed", Toast.LENGTH_SHORT).show();
                }else {
                    quantity--;
                }
                break;
            case R.id.plus_button:
                quantity++;
                break;
        }
        storageDisplay.setText(String.valueOf(quantity));
        np.setValue(quantity);
    }

    @Override
    public void onBackPressed() {
        save();
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
        /*storage = savedInstanceState.getInt(EXTRA_STORAGE);
        storageDisplay.setText(String.valueOf(storage));
        np.setValue(storage);*/
    }

    private void save(){
        if (quantity > 0){
            if(hadQuantity){
                item.inventory.setQuantity(quantity);
                viewModel.updateInventory(item.inventory);
            } else {
                viewModel.insertInventory(new Inventory(id, quantity));
            }
        }else {
            if(hadQuantity){
                viewModel.deleteInventory(item.inventory);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        save();
        return super.onOptionsItemSelected(item);
    }
}
