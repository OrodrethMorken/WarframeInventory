package com.games.orodreth.warframeinventory;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.games.orodreth.warframeinventory.nexus.NexusApi;
import com.games.orodreth.warframeinventory.nexus.ObjectNexus;
import com.games.orodreth.warframeinventory.nexus.ObjectWfcd;
import com.games.orodreth.warframeinventory.nexus.WfcdApi;
import com.games.orodreth.warframeinventory.warframeMarket.DucatsWFM;
import com.games.orodreth.warframeinventory.warframeMarket.ObjectWFM;
import com.games.orodreth.warframeinventory.warframeMarket.PlatinumWFM;
import com.games.orodreth.warframeinventory.warframeMarket.WfMaApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.games.orodreth.warframeinventory.Adapter.ADD_ONE;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ALL;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ONE;

public class MainActivity extends AppCompatActivity implements Adapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener, SourceDialog.SourceListener, SortDialog.SortListener {
    public static final String EXTRA_URL = "com.games.orodreth.warframeinventory.image_url";
    public static final String EXTRA_NAME = "com.games.orodreth.warframeinventory.item_name";
    public static final String EXTRA_DUCATS = "com.games.orodreth.warframeinventory.ducats";
    public static final String EXTRA_PLATINUM = "com.games.orodreth.warframeinventory.platinum";
    public static final String EXTRA_STORAGE = "com.games.orodreth.warframeinventory.storage";
    public static final String EXTRA_DUC_PLAT = "com.games.orodreth.warframeinventory.duc_plat";
    public static final String EXTRA_SOURCE = "com.games.orodreth.warframeinventory.source";
    public static final String EXTRA_SORT = "com.games.orodreth.warframeinventory.sort";
    public static final String EXTRA_NO_ZERO = "com.games.orodreth.warframeinventory.no_zero";
    public static final String EXTRA_INVERSE = "com.games.orodreth.warframeinventory.inverse";
    public static final String SHARED = "com.games.orodreth.warframeinventory.shared";
    public static final int STORAGE_VALUE = 17;
    public static final int SORT_AZ = 0;
    public static final int SORT_DUCPLAT = 1;
    public static final int SORT_DUC = 2;
    public static final int SORT_PLAT = 3;
    private static final int ADAPTER_CATALOG = 0;
    private static final int ADAPTER_STORAGE = 1;
    public static final int INVERTED = -1;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ArrayList<Items> mItems;
    private ArrayList<Items> filteredList;
    private ArrayList<Inventory> mInventory;
    private RequestQueue mRequestQueue;
    private Catalog mCatalog;
    private Storage mStorage;
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();
    private int focus; //determine if it's visible the catalog or the storage
    private int sorting;
    private EditText mSearch;
    private boolean searchToggle;
    private boolean no_zero;
    private boolean inverse_order;
    private int sourceSelected;

    private int mProgressStatus = 0;
    private int creditCounter = 0;

    private static final String TAG = "MainActivity";

    /**
     * on Creation, check if exist an ArrayList of items, if not create one, and update the platinum prices
     *
     * @param savedInstanceState savedState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(searchToggle){
                    searchToggle = false;
                    mSearch.setVisibility(View.GONE);
                    keyboard();
                }else {
                    searchToggle = true;
                    mSearch.setVisibility(View.VISIBLE);
                    keyboard();
                }*/
                retrofitWFM();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        ImageView logo = header.findViewById(R.id.logoView);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (creditCounter < 7) {
                    creditCounter++;
                } else {
                    creditCounter = 0;
                    CreditDialog creditDialog = new CreditDialog();
                    creditDialog.show(getSupportFragmentManager(), Integer.toString(creditCounter));
                }
            }
        });

        mProgressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recycler_view);

        mSearch = findViewById(R.id.search_item);
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        mSearch.setVisibility(View.GONE);
        searchToggle = false;

        loadData();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mItems = new ArrayList<>();
        filteredList = new ArrayList<>();
        mInventory = new ArrayList<>();
        focus = ADAPTER_CATALOG;
        sorting = SORT_AZ;

        mRequestQueue = Volley.newRequestQueue(this);
        mCatalog = new Catalog(this);
        mStorage = new Storage(this);
        if (mCatalog.exist()) { //check if the list already exist
            mItems = mCatalog.getItems(); //and get it from the file
            draw();
        } else {
            if (sourceSelected == 0) parseJSON2();
            else parseJSON();
        }
        if (mStorage.exist()) {
            mInventory = mStorage.getInventory();
        }
    }

    /**
     * Search the JSON list of Warframe.market and register all the items
     */

    private void parseJSON() {
        String url = "https://api.warframe.market/v1/items";
        if (hasConnection()) {
            mItems = new ArrayList<>();

            mProgressStatus = 0;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                //JSONArray jsonArray = response.getJSONArray("en");
                                JSONObject payload = response.getJSONObject("payload");
                                JSONObject arrayItem = payload.getJSONObject("items");
                                JSONArray jsonArray = arrayItem.getJSONArray("en");


                                for (int i = 0; i < jsonArray.length(); i++) {  //limited jsonArray.length() for test purpose TODO Remove Limit
                                    JSONObject en = jsonArray.getJSONObject(i);

                                    String url_name = en.getString("url_name");
                                    String item = en.getString("item_name");
                                    String imageUrl = en.getString("thumb");
                                    String item_id = en.getString("id");
                                    mItems.add(new Items(imageUrl, item, url_name, item_id));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Collections.sort(mItems, new Comparator<Items>() {
                                @Override
                                public int compare(Items o1, Items o2) {
                                    return o1.getItem().compareTo(o2.getItem());
                                }
                            });

                            mProgressStatus = 100;

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            mRequestQueue.add(request);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (mProgressStatus < 100) {

                        SystemClock.sleep(50);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            draw();
                            SystemClock.sleep(100);
                            ducats();
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * search the ducat value of the items that are in the list
     */

    private void ducats() {
        if (hasConnection()) {
            mProgressStatus = 0;  //reset progress value
            //mProgressBar.setVisibility(View.VISIBLE);
            new Thread(new Runnable() { //start thread were search ducat values
                @Override
                public void run() {

                    for (int j = 0; j < mItems.size(); j++) {
                        String url = "https://api.warframe.market/v1/items/" + mItems.get(j).getUrl();  //url of the single item
                        final String item_id = mItems.get(j).getId();                                   //get it's id
                        final int finalJ = j;                                                           //register position in the array

                        JsonObjectRequest request_duc = new JsonObjectRequest(Request.Method.GET, url, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONObject payload_duc = response.getJSONObject("payload");
                                            JSONObject arrayItem_duc = payload_duc.getJSONObject("item");
                                            JSONArray jsonArray_duc = arrayItem_duc.getJSONArray("items_in_set");

                                            for (int i = 0; i < jsonArray_duc.length(); i++) {
                                                JSONObject item_in_set = jsonArray_duc.getJSONObject(i);
                                                String temp_id = item_in_set.getString("id");
                                                if (temp_id.equals(item_id)) {
                                                    mItems.get(finalJ).setDucat(item_in_set.getInt("ducats"));
                                                }
                                            }
                                            if (finalJ < mItems.size()) {
                                                mProgressStatus++;
                                                if (mProgressStatus % 30 == 0)
                                                    Toast.makeText(MainActivity.this, "" + finalJ + " of " + mItems.size(), Toast.LENGTH_SHORT).show();
                                            } else mProgressStatus = mItems.size();
                                        } catch (JSONException e) {
                                            //e.printStackTrace();
                                            Log.d(TAG, "no ducat value j: " + finalJ);
                                            mProgressStatus++;
                                            if (mProgressStatus % 30 == 0)
                                                Toast.makeText(MainActivity.this, "" + finalJ + " of " + mItems.size(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mProgressStatus++;
                                Log.d(TAG, "error duc mProgressStatus: " + mProgressStatus);
                                if (mProgressStatus % 30 == 0)
                                    Toast.makeText(MainActivity.this, "" + finalJ + " of " + mItems.size(), Toast.LENGTH_SHORT).show();
                                error.printStackTrace();
                            }
                        });
                        mRequestQueue.add(request_duc);
                        //SystemClock.sleep(333);
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setMax(mItems.size());
                    while (mProgressStatus < mItems.size()) {
                        SystemClock.sleep(100);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setProgress(mProgressStatus);
                            }
                        });
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Update Ducats Completed", Toast.LENGTH_SHORT).show();
                            mProgressBar.setProgress(0);
                            mCatalog.write(mItems);
                            draw();
                            plats();
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * search the platinum value of the items that are in the list
     */

    private void plats() {
        if (hasConnection()) {
            mProgressStatus = 0;  //reset progress value
            //mProgressBar.setVisibility(View.VISIBLE);
            new Thread(new Runnable() { //start thread were search plat values
                @Override
                public void run() {

                    for (int j = 0; j < mItems.size(); j++) {
                    /*String url;
                    if(mItems.get(j).getItem().contains(" ")) url = "https://api.warframe.market/v1/items/" + mItems.get(j).getItem().toLowerCase().replace(" ","_") + "/orders";  //url of the single item
                    else url = "https://api.warframe.market/v1/items/" + mItems.get(j).getItem().toLowerCase()+ "/orders";  //url of the single item*/
                        String url = "https://api.warframe.market/v1/items/" + mItems.get(j).getUrl() + "/orders";
                        final int finalJ = j;                                                           //register position in the array

                        JsonObjectRequest request_duc = new JsonObjectRequest(Request.Method.GET, url, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONObject payload_plat = response.getJSONObject("payload");
                                            JSONArray jsonArray_plat = payload_plat.getJSONArray("orders");
                                            ArrayList<JSONObject> orders = new ArrayList<>();

                                            for (int i = 0; i < jsonArray_plat.length(); i++) {         //looking for all sell orders that are ingame on pc
                                                JSONObject itemOrder = jsonArray_plat.getJSONObject(i);
                                                JSONObject user = itemOrder.getJSONObject("user");
                                                if (itemOrder.getString("order_type").equals("sell") &&
                                                        itemOrder.getString("platform").equals("pc") &&
                                                        itemOrder.getString("region").equals("en") &&
                                                        user.getString("status").equals("ingame") &&
                                                        itemOrder.getString("visible").equals("true")) {
                                                    orders.add(itemOrder);
                                                }
                                            }

                                            if (orders.isEmpty()) {
                                                if (finalJ < mItems.size()) {
                                                    mProgressStatus++;
                                                } else mProgressStatus = mItems.size();
                                            } else {
                                                Collections.sort(orders, new Comparator<JSONObject>() {
                                                    @Override
                                                    public int compare(JSONObject o1, JSONObject o2) {
                                                        int result = 0;
                                                        try {
                                                            result = Integer.compare(o1.getInt("platinum"), o2.getInt("platinum"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        return result;
                                                    }
                                                });
                                                mItems.get(finalJ).setPlat(orders.get(0).getInt("platinum"));
                                                if (finalJ < mItems.size()) {
                                                    mProgressStatus++;
                                                } else mProgressStatus = mItems.size();
                                            }
                                        } catch (JSONException e) {
                                            //e.printStackTrace();
                                            Log.d(TAG, "no plat value j: " + finalJ);
                                            mProgressStatus++;
                                            //if(mProgressStatus%30==0) Toast.makeText(MainActivity.this, ""+finalJ+" of "+mItems.size(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "plat " + mItems.get(finalJ).getItem());
                                error.printStackTrace();
                            }
                        });

                        mRequestQueue.add(request_duc);
                        //SystemClock.sleep(333);
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setMax(mItems.size());
                    while (mProgressStatus < mItems.size()) {
                        SystemClock.sleep(100);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setProgress(mProgressStatus);
                            }
                        });
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(0);
                            Toast.makeText(MainActivity.this, "Update Platinum Ended", Toast.LENGTH_SHORT).show();
                            mCatalog.write(mItems);
                            draw();
                            ducPlat();
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void ducPlat() {
        if (hasConnection()) {
            for (Items item : mItems) {
                if (item.getPlat() != 0) {
                    float mDucPlat = (float) item.getDucats() / (float) item.getPlat();
                    item.setDucPlat(mDucPlat);
                } else item.setDucPlat(0);
            }
            mCatalog.write(mItems);
            draw();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * create and refresh the recyclerView
     */

    private void draw() {
        if (mAdapter != null) {
            filter(mSearch.getText().toString());
            mAdapter.setImageSource(sourceSelected);
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter = new Adapter(MainActivity.this, mItems);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setImageSource(sourceSelected);
            mAdapter.setOnItemListener(MainActivity.this);
        }
    }

    /**
     * Function called when clicking on an item
     *
     * @param position is the position on the index
     */

    @Override
    public void onItemClick(int position) {
        Intent itemIntent = new Intent(this, ItemActivity.class);
        Items selectedItem;
        if (mSearch.getText().toString().isEmpty() && focus == ADAPTER_CATALOG) { //check if there is a filter applied
            selectedItem = mItems.get(position);
        } else selectedItem = filteredList.get(position);
        int j = -1;
        if (!mInventory.isEmpty()) {
            for (int i = 0; i < mInventory.size(); i++) {
                String name = mInventory.get(i).getName();
                if (name.equals(selectedItem.getItem())) {
                    j = i;
                }
            }
        }
        itemIntent.putExtra(EXTRA_SOURCE, sourceSelected);
        itemIntent.putExtra(EXTRA_URL, selectedItem.getImageUrl());
        itemIntent.putExtra(EXTRA_NAME, selectedItem.getItem());
        itemIntent.putExtra(EXTRA_DUCATS, selectedItem.getDucats());
        itemIntent.putExtra(EXTRA_PLATINUM, selectedItem.getPlat());
        itemIntent.putExtra(EXTRA_DUC_PLAT, selectedItem.getDucPlat());
        if (j < 0) {
            itemIntent.putExtra(EXTRA_STORAGE, 0);
        } else {
            itemIntent.putExtra(EXTRA_STORAGE, mInventory.get(j).getQuantity());
        }
        //startActivity(itemIntent);
        startActivityForResult(itemIntent, STORAGE_VALUE); //require intent+int REQUEST_CODE
    }

    /**
     * Visualize the buttons on the toolbar
     *
     * @param menu menu
     * @return true
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Function for the button on the toolbar
     *
     * @param item button selected
     * @return true
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()){
            case R.id.refresh_button:
                return true;
            case R.id.update_all:
                Toast.makeText(this, "Updating all the data", Toast.LENGTH_SHORT).show();
                if(sourceSelected==0)parseJSON2();
                else parseJSON();
                return true;
            case R.id.update_plat:
                Toast.makeText(this, "Updating Platinum", Toast.LENGTH_SHORT).show();
                if(sourceSelected==0)plats2();
                else plats();
                return true;
            case R.id.save_button:
                Toast.makeText(this,"Saving", Toast.LENGTH_SHORT).show();
                if(!mInventory.isEmpty()){
                    mStorage.write(mInventory);
                }else Toast.makeText(this,"No item in the Inventory", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.search_bar:
                if(searchToggle){
                    searchToggle = false;
                    //mSearch.setText("");
                    mSearch.setVisibility(View.GONE);
                }else {
                    searchToggle = true;
                    mSearch.setVisibility(View.VISIBLE);
                }return true;
            case R.id.inventory_button:
                if(focus==ADAPTER_CATALOG){
                    focus = ADAPTER_STORAGE;
                    item.setTitle(getResources().getString(R.string.catalog));
                }else {
                    focus = ADAPTER_CATALOG;
                    item.setTitle(getResources().getString(R.string.inventory));
                }
                filter(mSearch.getText().toString());
                return true;
            case R.id.sorting:
                if(sorting==SORT_AZ){
                    sorting = SORT_DUCPLAT;
                    item.setTitle(getResources().getString(R.string.sort_az));
                }else {
                    sorting = SORT_AZ;
                    item.setTitle(getResources().getString(R.string.sort_duc_plat));
                }
                sort();
                return true;
            case R.id.quit_button:
                Toast.makeText(this, "Closing App", Toast.LENGTH_SHORT).show();
                if(!mInventory.isEmpty()){
                    mStorage.write(mInventory);
                }
                finish();
                return true;
            case R.id.nexus:
                sourceSelected = 0;
                item.setChecked(true);
                parseJSON2();
                return true;
            case R.id.market:
                sourceSelected = 1;
                item.setChecked(true);
                parseJSON();
                return true;
            default: return super.onOptionsItemSelected(item);
        }*/
        return super.onOptionsItemSelected(item);
    }

    /**
     * active the buttons of the contextual menu
     *
     * @param item selected item on the menu
     * @return true
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String name;
        if (mSearch.getText().toString().isEmpty() && focus == ADAPTER_CATALOG) { //check if there is a filter applied
            name = mItems.get(item.getGroupId()).getItem();
        } else name = filteredList.get(item.getGroupId()).getItem();
        switch (item.getItemId()) {
            case ADD_ONE:
                if (mInventory.isEmpty()) {
                    mInventory.add(new Inventory(name, 1));
                    filter(mSearch.getText().toString());
                } else {
                    for (Inventory i : mInventory) {
                        if (name.equals(i.getName())) {
                            i.add();
                            return true;
                        }
                    }
                    mInventory.add(new Inventory(name, 1));
                    filter(mSearch.getText().toString());
                }
                return true;
            case REMOVE_ONE:
                if (mInventory.isEmpty()) {
                    Toast.makeText(this, "No item to be removed", Toast.LENGTH_SHORT).show();
                } else {
                    for (Inventory i : mInventory) {
                        if (name.equals(i.getName())) {
                            if (i.getQuantity() > 1) {
                                i.subtract();
                            } else {
                                mInventory.remove(i);
                                filter(mSearch.getText().toString());
                            }
                            return true;
                        }
                    }
                    Toast.makeText(this, "No item to be removed", Toast.LENGTH_SHORT).show();
                }
                return true;
            case REMOVE_ALL:
                if (mInventory.isEmpty()) {
                    Toast.makeText(this, "No item to be removed", Toast.LENGTH_SHORT).show();
                } else {
                    for (Inventory i : mInventory) {
                        if (name.equals(i.getName())) {
                            mInventory.remove(i);
                            filter(mSearch.getText().toString());
                            return true;
                        }
                    }
                    Toast.makeText(this, "No item to be removed", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * catch the intent with the number of item registered
     *
     * @param requestCode is the request code send when called the activity
     * @param resultCode  is a response on positive or negative response
     * @param data        the data of the intent on a positive response
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case STORAGE_VALUE:
                if (resultCode == RESULT_OK) {
                    int j = -1;
                    if (!mInventory.isEmpty() && data != null) {
                        for (int i = 0; i < mInventory.size(); i++) {
                            if (mInventory.get(i).getName().equals(data.getStringExtra(EXTRA_NAME))) {
                                j = i;
                            }
                        }
                        if (j < 0 && data.getIntExtra(EXTRA_STORAGE, 0) > 0) {
                            mInventory.add(new Inventory(data.getStringExtra(EXTRA_NAME), data.getIntExtra(EXTRA_STORAGE, 0)));
                        } else {
                            if (j > -1) {
                                if (data.getIntExtra(EXTRA_STORAGE, 0) < 1) {
                                    mInventory.remove(j);
                                    filter(mSearch.getText().toString());
                                } else mInventory.get(j).set(data.getIntExtra(EXTRA_STORAGE, 0));
                            }
                        }
                    } else if (data != null && data.getIntExtra(EXTRA_STORAGE, 0) > 0) {
                        mInventory.add(new Inventory(data.getStringExtra(EXTRA_NAME), data.getIntExtra(EXTRA_STORAGE, 0)));
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void filter(String toString) {
        filteredList = new ArrayList<>();
        if (focus == ADAPTER_CATALOG) {
            for (Items item : mItems) {
                if (item.getItem().toLowerCase().contains(toString.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        } else {
            for (Items item : mItems) {
                for (Inventory inv : mInventory) {
                    if (item.getItem().toLowerCase().contains(toString.toLowerCase()) && item.getItem().equals(inv.getName())) {
                        filteredList.add(item);
                    }
                }
            }
        }
        mAdapter.filteredList(filteredList);
    }

    private void sort() {
        final int order;
        if (inverse_order) {
            order = INVERTED;
        } else order = 1;
        switch (sorting) {
            case SORT_AZ:
                Collections.sort(mItems, new Comparator<Items>() {
                    @Override
                    public int compare(Items o1, Items o2) {
                        return o1.getItem().compareTo(o2.getItem()) * order;
                    }
                });
                if (!filteredList.isEmpty()) {
                    Collections.sort(filteredList, new Comparator<Items>() {
                        @Override
                        public int compare(Items o1, Items o2) {
                            return o1.getItem().compareTo(o2.getItem()) * order;
                        }
                    });
                }
                break;
            case SORT_DUC:
                Collections.sort(mItems, new Comparator<Items>() {
                    @Override
                    public int compare(Items o1, Items o2) {
                        return Integer.compare(o1.getDucats(), o2.getDucats()) * order;

                    }
                });
                if (!filteredList.isEmpty()) {
                    Collections.sort(filteredList, new Comparator<Items>() {
                        @Override
                        public int compare(Items o1, Items o2) {
                            return Integer.compare(o1.getDucats(), o2.getDucats()) * order;
                        }
                    });
                }
                break;
            case SORT_PLAT:
                Collections.sort(mItems, new Comparator<Items>() {
                    @Override
                    public int compare(Items o1, Items o2) {
                        return Integer.compare(o1.getPlat(), o2.getPlat()) * order;
                    }
                });
                if (!filteredList.isEmpty()) {
                    Collections.sort(filteredList, new Comparator<Items>() {
                        @Override
                        public int compare(Items o1, Items o2) {
                            return Integer.compare(o1.getPlat(), o2.getPlat()) * order;
                        }
                    });
                }
                break;
            case SORT_DUCPLAT:
                Collections.sort(mItems, new Comparator<Items>() {
                    @Override
                    public int compare(Items o1, Items o2) {
                        return Float.compare(o1.getDucPlat(), o2.getDucPlat()) * order;
                    }
                });
                if (!filteredList.isEmpty()) {
                    Collections.sort(filteredList, new Comparator<Items>() {
                        @Override
                        public int compare(Items o1, Items o2) {
                            return Float.compare(o1.getDucPlat(), o2.getDucPlat()) * order;
                        }
                    });
                }
                break;
        }
        mAdapter.setImageSource(sourceSelected);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        mStorage.write(mInventory);
        super.onDestroy();
    }

    /**
     * new parser for the JSON of Warframe Community
     */
    private void parseJSON2() {
        if (hasConnection()) {
            String url = "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/All.json";
            mItems = new ArrayList<>();

            mProgressStatus = 0;
            mProgressBar.setMax(100);
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            mProgressBar.setMax(response.length());
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject item = response.getJSONObject(i);
                                    if ((item.getBoolean("tradable") && (
                                            item.getString("category").equals("Arcanes") || item.getString("category").equals("Archwing") || item.getString("category").equals("Melee") || item.getString("category").equals("Pets") || item.getString("category").equals("Primary") || item.getString("category").equals("Relics") || item.getString("category").equals("Secondary") || item.getString("category").equals("Sentinels") || item.getString("category").equals("Warframes")
                                    )) || item.getString("category").equals("Mods")) {
                                        String name = item.getString("name");
                                        String img = item.getString("imageName");
                                        if (!item.isNull("components")) {
                                            JSONArray comp = item.getJSONArray("components");
                                            for (int j = 0; j < comp.length(); j++) {
                                                if (comp.getJSONObject(j).getBoolean("tradable") && comp.getJSONObject(j).isNull("type")) {
                                                    String fullname = name + " " + comp.getJSONObject(j).getString("name");
                                                    String fullImage = comp.getJSONObject(j).getString("imageName");
                                                    Items items = new Items(fullImage, fullname, name, "");
                                                    if (!comp.getJSONObject(j).isNull("ducats")) {
                                                        int ducats = comp.getJSONObject(j).getInt("ducats");
                                                        items.setDucat(ducats);
                                                    }
                                                    mItems.add(items);
                                                }
                                            }
                                        } else {
                                            Items singleItem = new Items(img, name, name, "");
                                            if (!item.isNull("ducats"))
                                                singleItem.setDucat(item.getInt("ducats"));
                                            mItems.add(singleItem);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                mProgressStatus++;
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            mRequestQueue.add(request);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (mProgressStatus < mProgressBar.getMax()) {

                        android.os.SystemClock.sleep(50);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            draw();
                            android.os.SystemClock.sleep(100);
                            mCatalog.write(mItems);
                            plats2();
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Look plat prices on NexusHub
     */
    private void plats2() {
        if (hasConnection()) {
            mProgressStatus = 0;  //reset progress value
            new Thread(new Runnable() { //start thread were search plat values
                @Override
                public void run() {
                    String url = "https://api.nexushub.co/warframe/v1/items/";
                    JsonArrayRequest request_plat = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                for (Items item : mItems) {
                                    int index = -1;
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject object = response.getJSONObject(i);
                                        if (item.getUrl().equals(object.getString("name"))) {
                                            index = i;
                                            break;
                                        }
                                    }
                                    if (index >= 0) {
                                        JSONArray component = response.getJSONObject(index).getJSONArray("components");
                                        JSONObject price;
                                        boolean foundPrice = false;
                                        if (item.getItem().equals(item.getUrl())) {
                                            if (component.getJSONObject(0).has("prices")) {
                                                foundPrice = true;
                                                price = component.getJSONObject(0).getJSONObject("prices").getJSONObject("selling").getJSONObject("current");
                                            } else price = component.getJSONObject(0);
                                        } else {
                                            String parts = item.getItem().substring(item.getItem().lastIndexOf(" ") + 1);
                                            Log.d(TAG, "parts: " + parts);
                                            int j;
                                            for (j = 0; j < component.length(); j++) {
                                                if (parts.equals(component.getJSONObject(j).getString("name"))) {
                                                    break;
                                                }
                                            }
                                            if (j == component.length()) {
                                                if (component.getJSONObject(0).has("prices")) {
                                                    foundPrice = true;
                                                    price = component.getJSONObject(0).getJSONObject("prices").getJSONObject("selling").getJSONObject("current");
                                                } else price = component.getJSONObject(0);
                                            } else {
                                                if (component.getJSONObject(j).has("prices")) {
                                                    foundPrice = true;
                                                    price = component.getJSONObject(j).getJSONObject("prices").getJSONObject("selling").getJSONObject("current");
                                                } else price = component.getJSONObject(0);
                                            }
                                        }
                                        if (!price.isNull("min") && !price.isNull("median") && foundPrice) {
                                            item.setPlat(price.getInt("min"));
                                            item.setPlatAvg(price.getInt("median"));
                                            if (price.getInt("median") != 0) {
                                                item.setDucPlat((float) item.getDucats() / (float) price.getInt("median"));
                                            }
                                        }
                                    }
                                    mProgressStatus++;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                    mRequestQueue.add(request_plat);
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setMax(mItems.size());
                    while (mProgressStatus < mItems.size()) {
                        SystemClock.sleep(100);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setProgress(mProgressStatus);
                            }
                        });
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(0);
                            Toast.makeText(MainActivity.this, "Update Platinum Ended", Toast.LENGTH_SHORT).show();
                            mCatalog.write(mItems);
                            draw();
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * manage the navigation bar
     *
     * @param item the item that was selected
     * @return true
     */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_search:
                creditCounter = 0;
                if (searchToggle) {
                    searchToggle = false;
                    //mSearch.setText("");
                    mSearch.setVisibility(View.GONE);
                    keyboard();
                } else {
                    searchToggle = true;
                    mSearch.setVisibility(View.VISIBLE);
                    keyboard();
                }
                break;
            case R.id.nav_inventory:
                creditCounter = 0;
                if (focus == ADAPTER_CATALOG) {
                    focus = ADAPTER_STORAGE;
                    item.setTitle(getResources().getString(R.string.catalog));
                    item.setIcon(R.drawable.ic_catalog);
                } else {
                    focus = ADAPTER_CATALOG;
                    item.setTitle(getResources().getString(R.string.inventory));
                    item.setIcon(R.drawable.ic_inventory);
                }
                filter(mSearch.getText().toString());
                break;
            case R.id.nav_update:
                creditCounter = 0;
                Toast.makeText(this, "Updating Platinum", Toast.LENGTH_SHORT).show();
                if (sourceSelected == 0) plats2();
                else plats();
                break;
            case R.id.nav_source:
                creditCounter = 0;
                SourceDialog sourceDialog = new SourceDialog();
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRA_SOURCE, sourceSelected);
                sourceDialog.setArguments(bundle);
                sourceDialog.show(getSupportFragmentManager(), Integer.toString(sourceSelected));
                break;
            case R.id.nav_save:
                creditCounter = 0;
                Toast.makeText(this, "Saving", Toast.LENGTH_SHORT).show();
                if (!mInventory.isEmpty()) {
                    mStorage.write(mInventory);
                } else Toast.makeText(this, "No item in the Inventory", Toast.LENGTH_SHORT).show();
                saveData();
                break;
            case R.id.nav_sort:
                creditCounter = 0;
                SortDialog sortDialog = new SortDialog();
                Bundle bundle1 = new Bundle();
                bundle1.putInt(EXTRA_SORT, sorting);
                bundle1.putBoolean(EXTRA_NO_ZERO, no_zero);
                bundle1.putBoolean(EXTRA_INVERSE, inverse_order);
                sortDialog.setArguments(bundle1);
                sortDialog.show(getSupportFragmentManager(), Integer.toString(sorting));
                /*if(sorting==SORT_AZ){
                    sorting = SORT_DUCPLAT;
                    item.setTitle(getResources().getString(R.string.sort_az));
                    item.setIcon(R.drawable.ic_sort_by_alpha);
                }else {
                    sorting = SORT_AZ;
                    item.setTitle(getResources().getString(R.string.sort_duc_plat));
                    item.setIcon(R.drawable.ic_sort_duc);
                }
                sort();*/
                break;
            case R.id.nav_credit:
                creditCounter = 0;
                CreditDialog creditDialog = new CreditDialog();
                creditDialog.show(getSupportFragmentManager(), "Credits");
                break;
            case R.id.nav_quit:
                creditCounter = 0;
                Toast.makeText(this, "Closing App", Toast.LENGTH_SHORT).show();
                if (!mInventory.isEmpty()) {
                    mStorage.write(mInventory);
                }
                finish();
            default:
                creditCounter = 0;
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * manage opening and closing of the keyboard
     */
    private void keyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (searchToggle) {
                imm.showSoftInput(view, 0);
                mSearch.requestFocus();
            } else {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void selectSource(int source) {
        if (source == 0) {
            sourceSelected = 0;
            parseJSON2();
        } else if (source == 1) {
            sourceSelected = 1;
            parseJSON();
        }
        saveData();
        sorting = SORT_AZ;
    }


    private boolean hasConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED, MODE_PRIVATE);
        sourceSelected = sharedPreferences.getInt(EXTRA_SOURCE, 0);
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt(EXTRA_SOURCE, sourceSelected);
        editor.apply();
    }

    @Override
    public void sortSelected(Bundle bundle) {
        sorting = bundle.getInt(EXTRA_SORT);
        no_zero = bundle.getBoolean(EXTRA_NO_ZERO);
        inverse_order = bundle.getBoolean(EXTRA_INVERSE);
        removeZero();
        sort();
    }

    /**
     * Remove or restore the data sorted with value of zero
     */

    private void removeZero() {
        filter(mSearch.getText().toString()); //reform the array so to restore eventualy data lost from the previews sort
        if (no_zero) {
            if (!filteredList.isEmpty()) {  //if is filtered
                ArrayList<Items> newFiltered = new ArrayList<>();
                switch (sorting) {
                    case SORT_DUC:
                        for (Items i : filteredList) {
                            if (i.getDucats() != 0) newFiltered.add(i);
                        }
                        filteredList = newFiltered;
                        break;
                    case SORT_PLAT:
                        for (Items i : filteredList) {
                            if (i.getPlat() != 0) newFiltered.add(i);
                        }
                        filteredList = newFiltered;
                        break;
                    case SORT_DUCPLAT:
                        for (Items i : filteredList) {
                            if (i.getDucPlat() != 0) newFiltered.add(i);
                        }
                        filteredList = newFiltered;
                        break;
                }
            } else {
                switch (sorting) {
                    case SORT_DUC:
                        for (Items i : mItems) {
                            if (i.getDucats() != 0) filteredList.add(i);
                        }
                        break;
                    case SORT_PLAT:
                        for (Items i : mItems) {
                            if (i.getPlat() != 0) filteredList.add(i);
                        }
                        break;
                    case SORT_DUCPLAT:
                        for (Items i : mItems) {
                            if (i.getDucPlat() != 0) filteredList.add(i);
                        }
                        break;
                }
            }
            mAdapter.filteredList(filteredList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SORT, sorting);
        outState.putInt(EXTRA_SOURCE, sourceSelected);
        outState.putInt(EXTRA_STORAGE, focus);
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

        sorting = savedInstanceState.getInt(EXTRA_SORT);
        sourceSelected = savedInstanceState.getInt(EXTRA_SOURCE);
        focus = savedInstanceState.getInt(EXTRA_STORAGE);
        filter(mSearch.getText().toString());
        sort();
    }

    private void retrofitNexus() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WfcdApi wfcdApi = retrofit.create(WfcdApi.class);

        Call<List<ObjectWfcd>> listCall = wfcdApi.getItems();
        listCall.enqueue(new Callback<List<ObjectWfcd>>() {
            @Override
            public void onResponse(Call<List<ObjectWfcd>> call, retrofit2.Response<List<ObjectWfcd>> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: code: " + response.code());
                    return;
                }

                List<ObjectWfcd> items = response.body();
                int i = items.get(0).getComponents().get(0).getDucats();
            }

            @Override
            public void onFailure(Call<List<ObjectWfcd>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

        NexusApi nexusApi = retrofit.create(NexusApi.class);
        Call<List<ObjectNexus>> listPrice = nexusApi.getPrices();
        listPrice.enqueue(new Callback<List<ObjectNexus>>() {
            @Override
            public void onResponse(Call<List<ObjectNexus>> call, retrofit2.Response<List<ObjectNexus>> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: code: " + response.code());
                    return;
                }

                List<ObjectNexus> prices = response.body();
            }

            @Override
            public void onFailure(Call<List<ObjectNexus>> call, Throwable t) {

            }
        });
    }

    private void retrofitWFM(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.warframe.market/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final WfMaApi wfMaApi = retrofit.create(WfMaApi.class);
        Call<ObjectWFM> listObject = wfMaApi.getItems();
        listObject.enqueue(new Callback<ObjectWFM>() {
            @Override
            public void onResponse(Call<ObjectWFM> call, retrofit2.Response<ObjectWFM> response) {
                if(!response.isSuccessful()){
                    Log.d(TAG, "onResponse: code: "+response.code());
                    return;
                }
                ObjectWFM objectWFM = response.body();
                mItems = new ArrayList<>();
                for (ObjectWFM.Payload.WFItems object:objectWFM.getPayload().getItems()) {
                    mItems.add(new Items(object.getThumb(),object.getItem_name(),object.getUrl_name(),object.getId()));
                }
                for (int i=0; i<mItems.size(); i++
                     ) {
                    Call<DucatsWFM> ducatsWFMCall = wfMaApi.getDucats(mItems.get(i).getUrl());
                    final int finalI = i;
                    ducatsWFMCall.enqueue(new Callback<DucatsWFM>() {
                        @Override
                        public void onResponse(Call<DucatsWFM> call, retrofit2.Response<DucatsWFM> response) {
                            if(!response.isSuccessful()){
                                Log.d(TAG, "onResponse: code: "+response.code());
                            }
                            DucatsWFM ducatsWFM = response.body();
                            String id = ducatsWFM.getPayload().getItem().getId();
                            for (DucatsWFM.Payload.Item.ItemInSet item:ducatsWFM.getPayload().getItem().getItems_in_set()) {
                                if(item.getId().equals(id)){
                                    mItems.get(finalI).setDucat(item.getDucats());
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DucatsWFM> call, Throwable t) {
                            Log.d(TAG, "onFailure: "+t.getMessage());
                        }
                    });

                    Call<PlatinumWFM> platinumWFMCall = wfMaApi.getstatistics(mItems.get(i).getUrl());
                    platinumWFMCall.enqueue(new Callback<PlatinumWFM>() {
                        @Override
                        public void onResponse(Call<PlatinumWFM> call, retrofit2.Response<PlatinumWFM> response) {
                            if(!response.isSuccessful()){
                                Log.d(TAG, "onResponse: code: "+response.code());
                                return;
                            }
                            PlatinumWFM platinumWFM = response.body();
                            List<PlatinumWFM.Payload.StatClose.Orders> orders = platinumWFM.getPayload().getStatistics_closed().getOrders();
                            if(orders.isEmpty()) return;
                            mItems.get(finalI).setPlat(orders.get(orders.size()-1).getMin_price());
                            mItems.get(finalI).setPlatAvg((int)orders.get(orders.size()-1).getMedian());
                            //memorizzare i valori di platinum e valore medio
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<PlatinumWFM> call, Throwable t) {
                            Log.d(TAG, "onFailure: "+t.getMessage());
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<ObjectWFM> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        });
    }
}