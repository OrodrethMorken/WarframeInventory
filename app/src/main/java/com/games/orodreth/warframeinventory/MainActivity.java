package com.games.orodreth.warframeinventory;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.games.orodreth.warframeinventory.Adapter.ADD_ONE;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ALL;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ONE;

public class MainActivity extends AppCompatActivity implements Adapter.OnItemClickListener {
    public static final String EXTRA_URL = "image_url";
    public static final String EXTRA_NAME = "item_name";
    public static final String EXTRA_DUCATS = "ducats";
    public static final String EXTRA_PLATINUM = "platinum";
    public static final String EXTRA_STORAGE = "storage";
    public static final String EXTRA_DUC_PLAT = "duc_plat";
    public static final String EXTRA_SOURCE = "source";
    public static final int STORAGE_VALUE = 17 ;
    private static final int ADAPTER_CATALOG = 0;
    private static final int ADAPTER_STORAGE = 1;
    private static final int SORT_AZ = 0;
    private static final int SORT_DUCPLAT = 1;

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ArrayList<Items> mItems;
    private ArrayList<Items> filteredList;
    private ArrayList<Inventory> mInventory;
    private RequestQueue mRequestQueue;
    private final Object lock = new Object();
    private Catalog mCatalog;
    private Storage mStorage;
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();
    private int focus; //determine if it's visible the catalog or the storage
    private int sorting;
    private EditText mSearch;
    private boolean searchTogle;
    private int sourceSelected;

    private int mProgressStatus = 0;

    /**
     * on Creation, check if exist an arraylist of items, if not create one, and update the platinum prices
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

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
        searchTogle = false;

        sourceSelected = 0;

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
        if(mCatalog.exist()){ //check if the list already exist
            mItems = mCatalog.getItems(); //and get it from the file
            //System.out.println("XXX Number of items file: "+mItems.size());
            draw();
            //progressBar();
        }else{
            if(sourceSelected==0) parseJSON2();
            else parseJSON();
            //System.out.println("XXX Number of items memory: "+mItems.size());
            //mCatalog.write(mItems);  //register the list on the file
        }
        //System.out.println("XXX Number of items 2 memory: "+mItems.size());
        if(mStorage.exist()){
            mInventory = mStorage.getInventory();
        }
    }

    /**
     * Search the JSON list of Warframe.market and register all the items
     */

    private void parseJSON(){
        String url = "https://api.warframe.market/v1/items";
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
                                    //System.out.println("XXX adding Item");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //mCatalog.write(mItems);

                            Collections.sort(mItems, new Comparator<Items>() {
                                @Override
                                public int compare(Items o1, Items o2) {
                                    int result = 0;
                                    result = o1.getItem().compareTo(o2.getItem());
                                    return result;
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

                    while(mProgressStatus<100) {

                        android.os.SystemClock.sleep(50);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            draw();
                            android.os.SystemClock.sleep(100);
                            ducats();
                        }
                    });
                }
            }).start();
    }

    /**
     * search the ducat value of the items that are in the list
     */

    private void ducats () {
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
                                        int ducat = 0;

                                        for (int i = 0; i < jsonArray_duc.length(); i++) {
                                            JSONObject item_in_set = jsonArray_duc.getJSONObject(i);
                                            String temp_id = item_in_set.getString("id");
                                            if (temp_id.equals(item_id)) {
                                                mItems.get(finalJ).setDucat(item_in_set.getInt("ducats"));
                                                //System.out.println("j= " + finalJ + " i= " + i + " ducat= " + ducat);
                                            }
                                        }
                                        if(finalJ<mItems.size()) {
                                            mProgressStatus++;
                                            if(mProgressStatus%30==0) Toast.makeText(MainActivity.this, ""+finalJ+" of "+mItems.size(), Toast.LENGTH_SHORT).show();
                                        }else mProgressStatus = mItems.size();
                                        System.out.println("XXX duc mProgressStatus: "+mProgressStatus);
                                    } catch (JSONException e) {
                                        //e.printStackTrace();
                                        System.out.println("XXX no ducat value j: "+finalJ);
                                        mProgressStatus++;
                                        if(mProgressStatus%30==0) Toast.makeText(MainActivity.this, ""+finalJ+" of "+mItems.size(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressStatus++;
                            System.out.println("XXX error duc mProgressStatus: "+mProgressStatus);
                            if(mProgressStatus%30==0) Toast.makeText(MainActivity.this, ""+finalJ+" of "+mItems.size(), Toast.LENGTH_SHORT).show();
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
                while (mProgressStatus < mItems.size()){
                    SystemClock.sleep(100);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //System.out.println("XXX bar mProgressStatus: "+mProgressStatus);
                            mProgressBar.setProgress(mProgressStatus);
                        }
                    });
                };
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
    }

    /**
     * search the platinum value of the items that are in the list
     */

    private void plats () {
        mProgressStatus = 0;  //reset progress value
        //mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() { //start thread were search plat values
            @Override
            public void run() {

                for (int j = 0; j < mItems.size(); j++) {
                    /*String url;
                    if(mItems.get(j).getItem().contains(" ")) url = "https://api.warframe.market/v1/items/" + mItems.get(j).getItem().toLowerCase().replace(" ","_") + "/orders";  //url of the single item
                    else url = "https://api.warframe.market/v1/items/" + mItems.get(j).getItem().toLowerCase()+ "/orders";  //url of the single item*/
                    String url = "https://api.warframe.market/v1/items/" + mItems.get(j).getUrl()+ "/orders";
                    final int finalJ = j;                                                           //register position in the array

                    JsonObjectRequest request_duc = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONObject payload_plat = response.getJSONObject("payload");
                                        JSONArray jsonArray_plat = payload_plat.getJSONArray("orders");
                                        int plat = 0;
                                        ArrayList<JSONObject> orders = new ArrayList<>();
                                        //System.out.println("XXX is List empty?: " + orders.isEmpty());

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
                                            //System.out.println("XXX no order for: "+mItems.get(finalJ).getItem());
                                            if (finalJ < mItems.size()) {
                                                mProgressStatus++;
                                                //if(mProgressStatus%30==0) Toast.makeText(MainActivity.this, ""+finalJ+" of "+mItems.size(), Toast.LENGTH_SHORT).show();
                                            } else mProgressStatus = mItems.size();
                                        } else {
                                            Collections.sort(orders, new Comparator<JSONObject>() {
                                                @Override
                                                public int compare(JSONObject o1, JSONObject o2) {
                                                    int result = 0;
                                                    try {
                                                        result = Integer.valueOf(o1.getInt("platinum")).compareTo(Integer.valueOf(o2.getInt("platinum")));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    return result;
                                                }
                                            });
                                            plat = orders.get(0).getInt("platinum");
                                            mItems.get(finalJ).setPlat(orders.get(0).getInt("platinum"));
                                            if (finalJ < mItems.size()) {
                                                mProgressStatus++;
                                                //if(mProgressStatus%30==0) Toast.makeText(MainActivity.this, ""+finalJ+" of "+mItems.size(), Toast.LENGTH_SHORT).show();
                                            } else mProgressStatus = mItems.size();
                                            //System.out.println("XXX json mProgressStatus: "+mProgressStatus);
                                        }
                                    }catch (JSONException e) {
                                        //e.printStackTrace();
                                        //System.out.println("XXX no plat value j: "+finalJ);
                                        mProgressStatus++;
                                        //if(mProgressStatus%30==0) Toast.makeText(MainActivity.this, ""+finalJ+" of "+mItems.size(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("XXX plat "+mItems.get(finalJ).getItem());
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
                while (mProgressStatus < mItems.size()){
                    SystemClock.sleep(100);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //System.out.println("XXX bar mProgressStatus: "+mProgressStatus);
                            mProgressBar.setProgress(mProgressStatus);
                        }
                    });
                };
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
    }

    private void ducPlat(){
        for(Items item : mItems){
            if(item.getPlat()!=0) {
                float mDucPlat = (float) item.getDucats() / (float) item.getPlat();
                item.setDucPlat(mDucPlat);
            }else item.setDucPlat(0);
        }
        mCatalog.write(mItems);
        draw();
    }

    /**
     * create and refresh the recyclerView
     */

    private void draw(){
            if (mAdapter != null) {
                //System.out.println("XXX refresh view");
                filter(mSearch.getText().toString());
                mAdapter.setImageSource(sourceSelected);
                mAdapter.notifyDataSetChanged();
            } else {
                //System.out.println("XXX Number of items 3 memory: " + mItems.size());
                mAdapter = new Adapter(MainActivity.this, mItems);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setImageSource(sourceSelected);
                mAdapter.setOnItemListener(MainActivity.this);
            }
    }

    private void progressBar(){
        mProgressStatus = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mProgressStatus<100) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(mProgressStatus);
                        }
                    });
                    mProgressStatus++;
                    SystemClock.sleep(50);
                }
                mProgressBar.setProgress(0);
            }
        }).start();
    }

    /**
     * Function called when clicking on an item
     * @param position is the position on the index
     */

    @Override
    public void onItemClick(int position) {
        Intent itemIntent = new Intent(this, ItemActivity.class);
        Items selectedItem;
        if(mSearch.getText().toString().isEmpty() && focus == ADAPTER_CATALOG) { //check if there is a filter applied
            selectedItem = mItems.get(position);
        }else selectedItem = filteredList.get(position);
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
        if(j<0){
            itemIntent.putExtra(EXTRA_STORAGE, 0);
        }else {
            itemIntent.putExtra(EXTRA_STORAGE, mInventory.get(j).getQuantity());
        }
        //startActivity(itemIntent);
        startActivityForResult(itemIntent, STORAGE_VALUE); //require intent+int REQUEST_CODE
    }

    /**
     * Visualize the buttons on the toolbar
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Function for the button on the toolbar
     * @param item button selected
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
                if(searchTogle){
                    searchTogle = false;
                    //mSearch.setText("");
                    mSearch.setVisibility(View.GONE);
                }else {
                    searchTogle = true;
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
        }
    }

    /**
     * active the buttons of the contectuale menu
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String name;
        if(mSearch.getText().toString().isEmpty() && focus == ADAPTER_CATALOG) { //check if there is a filter applied
            name = mItems.get(item.getGroupId()).getItem();
        }else name = filteredList.get(item.getGroupId()).getItem();
        switch (item.getItemId()){
            case ADD_ONE:
                if(mInventory.isEmpty()){
                    mInventory.add(new Inventory(name,1));
                    filter(mSearch.getText().toString());
                }else {
                    for (Inventory i : mInventory) {
                        if (name.equals(i.getName())) {
                            i.add();
                            return true;
                        }
                    }
                    mInventory.add(new Inventory(name,1));
                    filter(mSearch.getText().toString());
                }
                return true;
            case REMOVE_ONE:
                if(mInventory.isEmpty()){
                    Toast.makeText(this, "No item to be removed", Toast.LENGTH_SHORT).show();
                }else{
                    for (Inventory i : mInventory) {
                        if (name.equals(i.getName())) {
                            if (i.getQuantity() > 1){
                                i.substract();
                            }else {
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
                if(mInventory.isEmpty()){
                Toast.makeText(this, "No item to be removed", Toast.LENGTH_SHORT).show();
                }else{
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
     * @param requestCode is the request code send when called the activity
     * @param resultCode is a response on positive or negative response
     * @param data the data of the intent on a positive response
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case STORAGE_VALUE:
                if(resultCode == RESULT_OK){
                    int j = -1;
                    if(!mInventory.isEmpty() && data!= null) {
                        for (int i = 0; i < mInventory.size(); i++) {
                            if (mInventory.get(i).getName().equals(data.getStringExtra(EXTRA_NAME))) {
                                j = i;
                            }
                        }
                        if (j < 0 && data.getIntExtra(EXTRA_STORAGE, 0)>0) {
                            mInventory.add(new Inventory(data.getStringExtra(EXTRA_NAME), data.getIntExtra(EXTRA_STORAGE, 0)));
                        }else {
                            if(j>-1){
                                if(data.getIntExtra(EXTRA_STORAGE, 0)<1) {
                                    mInventory.remove(j);
                                    filter(mSearch.getText().toString());
                                } else mInventory.get(j).set(data.getIntExtra(EXTRA_STORAGE, 0));
                            }
                        }
                    }else if(data!=null && data.getIntExtra(EXTRA_STORAGE, 0)>0) {
                        mInventory.add(new Inventory(data.getStringExtra(EXTRA_NAME), data.getIntExtra(EXTRA_STORAGE, 0)));
                    }
                }
                //mAdapter.notifyDataSetChanged();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void filter(String toString) {
        filteredList = new ArrayList<>();
        if(focus==ADAPTER_CATALOG) {
            for (Items item : mItems) {
                if (item.getItem().toLowerCase().contains(toString.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }else {
            for (Items item : mItems) {
                for(Inventory inv : mInventory){
                    if (item.getItem().toLowerCase().contains(toString.toLowerCase()) && item.getItem().equals(inv.getName())) {
                        filteredList.add(item);
                    }
                }
            }
        }
        mAdapter.filteredList(filteredList);
    }

    private void sort(){
        if(sorting == SORT_AZ) {
            Collections.sort(mItems, new Comparator<Items>() {
                @Override
                public int compare(Items o1, Items o2) {
                    int result = 0;
                    result = o1.getItem().compareTo(o2.getItem());
                    return result;
                }
            });
            if(!filteredList.isEmpty()){
                Collections.sort(filteredList, new Comparator<Items>() {
                    @Override
                    public int compare(Items o1, Items o2) {
                        int result = 0;
                        result = o1.getItem().compareTo(o2.getItem());
                        return result;
                    }
                });
            }
        }else {
            Collections.sort(mItems, new Comparator<Items>() {
                @Override
                public int compare(Items o1, Items o2) {
                    int result = 0;
                    result = Float.compare(o1.getDucPlat(),o2.getDucPlat())*-1;
                    return result;
                }
            });
            if(!filteredList.isEmpty()){
                Collections.sort(filteredList, new Comparator<Items>() {
                    @Override
                    public int compare(Items o1, Items o2) {
                        int result = 0;
                        result = Float.compare(o1.getDucPlat(),o2.getDucPlat())*-1;
                        return result;
                    }
                });
            }
        }
        mAdapter.setImageSource(sourceSelected);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        //System.out.println("XXX onDestroyMain");
        mStorage.write(mInventory);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //System.out.println("XXX onBackMain");
        //mStorage.write(mInventory);
        super.onBackPressed();
    }

    /**
     * new parser for the JSON of Warframe Community
     */
    private void parseJSON2(){
        String url = "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/All.json";
        mItems = new ArrayList<>();

        mProgressStatus = 0;
        mProgressBar.setMax(100);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mProgressBar.setMax(response.length());
                        for (int i=0; i<response.length(); i++){
                            try {
                                JSONObject item = response.getJSONObject(i);
                                if ((item.getBoolean("tradable")&&(
                                        item.getString("category").equals("Arcanes")||item.getString("category").equals("Archwing")||item.getString("category").equals("Melee")||item.getString("category").equals("Pets")||item.getString("category").equals("Primary")||item.getString("category").equals("Relics")||item.getString("category").equals("Secondary")||item.getString("category").equals("Sentinels")||item.getString("category").equals("Warframes")
                                ))||item.getString("category").equals("Mods")){
                                    String name = item.getString("name");
                                    String img = item.getString("imageName");
                                    if(!item.isNull("components")){
                                        JSONArray comp = item.getJSONArray("components");
                                        for(int j=0; j < comp.length(); j++){
                                            if(comp.getJSONObject(j).getBoolean("tradable")&&comp.getJSONObject(j).isNull("type")) {
                                                String fullname = name + " " + comp.getJSONObject(j).getString("name");
                                                String fullImage = comp.getJSONObject(j).getString("imageName");
                                                Items items = new Items(fullImage, fullname, name, "");
                                                if(!comp.getJSONObject(j).isNull("ducats")){
                                                    int ducats = comp.getJSONObject(j).getInt("ducats");
                                                    items.setDucat(ducats);
                                                }
                                                mItems.add(items);
                                            }
                                        }
                                    }else{
                                        Items singleItem = new Items(img, name, name,"");
                                        if(!item.isNull("ducats")) singleItem.setDucat(item.getInt("ducats"));
                                        mItems.add(singleItem);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mProgressStatus++;
                            //System.out.println("XXX PJ2 prog: "+mProgressStatus+" of "+response.length());
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

                while(mProgressStatus<mProgressBar.getMax()) {

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
    }

    /**
     * Look plat prices on NexusHub
     */
    private void plats2(){
        mProgressStatus = 0;  //reset progress value
        //mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() { //start thread were search plat values
            @Override
            public void run() {
                String url = "https://api.nexushub.co/warframe/v1/items/";
                JsonArrayRequest request_plat = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(Items item:mItems){
                                int index = -1;
                                for (int i=0; i<response.length(); i++){
                                    JSONObject object = response.getJSONObject(i);
                                    if(item.getUrl().equals(object.getString("name"))){
                                        index = i;
                                        break;
                                    }
                                }
                                if(index>=0){
                                    JSONArray component = response.getJSONObject(index).getJSONArray("components");
                                    JSONObject price;
                                    //System.out.println("XXX item: "+item.getItem()+" index: "+index);
                                    boolean foundPrice = false;
                                    if(item.getItem().equals(item.getUrl())) {
                                        if(component.getJSONObject(0).has("prices")){
                                            foundPrice = true;
                                            price = component.getJSONObject(0).getJSONObject("prices").getJSONObject("selling").getJSONObject("current");
                                        } else price = component.getJSONObject(0);
                                    }else {
                                        String parts = item.getItem().substring(item.getItem().lastIndexOf(" ") + 1);
                                        System.out.println("XXX parts: "+parts);
                                        int j;
                                        for (j=0; j<component.length();j++){
                                            if(parts.equals(component.getJSONObject(j).getString("name"))){
                                                break;
                                            }
                                        }
                                        if(j==component.length()){
                                            if(component.getJSONObject(0).has("prices")){
                                                foundPrice = true;
                                                price = component.getJSONObject(0).getJSONObject("prices").getJSONObject("selling").getJSONObject("current");
                                            } else price = component.getJSONObject(0);
                                        }else {
                                            if(component.getJSONObject(j).has("prices")){
                                                foundPrice = true;
                                                price = component.getJSONObject(j).getJSONObject("prices").getJSONObject("selling").getJSONObject("current");
                                            } else price = component.getJSONObject(0);
                                        }
                                    }
                                    if(!price.isNull("min")&&!price.isNull("median")&&foundPrice) {
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
                while (mProgressStatus < mItems.size()){
                    SystemClock.sleep(100);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //System.out.println("XXX bar mProgressStatus: "+mProgressStatus);
                            mProgressBar.setProgress(mProgressStatus);
                        }
                    });
                };
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(0);
                        Toast.makeText(MainActivity.this, "Update Platinum Ended", Toast.LENGTH_SHORT).show();
                        mCatalog.write(mItems);
                        draw();
                        //ducPlat();
                    }
                });
            }
        }).start();
    }

    /**
     *
     */
}