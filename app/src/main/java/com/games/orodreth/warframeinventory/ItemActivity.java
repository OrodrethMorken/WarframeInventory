package com.games.orodreth.warframeinventory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_DUCATS;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_DUC_PLAT;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_NAME;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_PLATINUM;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_SOURCE;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_STORAGE;
import static com.games.orodreth.warframeinventory.MainActivity.EXTRA_URL;

public class ItemActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String RESULT_INTENT = "result";
    private int storage;
    private String itemName;
    private TextView storageDisplay;

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
        String ducPlatString = String.format("%.2f", itemDucPlat);
        storage = intent.getIntExtra(EXTRA_STORAGE, 0);

        getSupportActionBar().setTitle(itemName);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.item_image_view);
        TextView nameView = findViewById(R.id.item_view_name);
        TextView ducView = findViewById(R.id.item_view_duc);
        TextView platView = findViewById(R.id.item_view_plat);
        TextView ducPlatView = findViewById(R.id.item_view_duc_plat);
        storageDisplay = findViewById(R.id.storage_value);

        AppCompatButton minusButton = findViewById(R.id.minus_button);
        AppCompatButton plusButton = findViewById(R.id.plus_button);

        Picasso.with(this).load(imageUrl).fit().centerInside().into(imageView);
        nameView.setText(itemName);
        ducView.setText(this.getResources().getString(R.string.ducats)+ itemDucs);
        platView.setText(this.getResources().getString(R.string.platinum)+ itemPlats);
        ducPlatView.setText(this.getResources().getString(R.string.duc_plat)+ducPlatString);
        storageDisplay.setText(""+storage);

        minusButton.setOnClickListener(this);
        plusButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.minus_button:
                if(storage==0){
                    Toast.makeText(this,"No item to remove", Toast.LENGTH_SHORT).show();
                }else storage--;
                break;
            case R.id.plus_button:
                storage++;
                break;
        }
        storageDisplay.setText(""+storage);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_STORAGE, storage);
        resultIntent.putExtra(EXTRA_NAME, itemName);
        if(resultIntent!=null) {
            System.out.println("XXX " + itemName + " has " + storage + " and intent is " + true);
        }else System.out.println("XXX " + itemName + " has " + storage + " and intent is " + false);
        setResult(RESULT_OK, resultIntent); //require int RESULT_CODE, intent to send back
        finish();
        super.onBackPressed();
    }
}
