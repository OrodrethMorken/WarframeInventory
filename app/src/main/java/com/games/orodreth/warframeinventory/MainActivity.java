package com.games.orodreth.warframeinventory;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.Items;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.SearchView;
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
import java.util.List;

import static com.games.orodreth.warframeinventory.Adapter.ADD_ONE;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ALL;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ONE;

public class MainActivity extends AppCompatActivity implements Adapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {
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
    private SearchView mSearch;
    private boolean searchToggle;
    private boolean no_zero;
    private boolean inverse_order;
    private int sourceSelected;

    private MainActivityViewModel viewModel;

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
                if(searchToggle){
                    searchToggle = false;
                    mSearch.setVisibility(View.GONE);
                    keyboard();
                }else {
                    searchToggle = true;
                    mSearch.setVisibility(View.VISIBLE);
                    keyboard();
                }
                Toast.makeText(MainActivity.this, "database count: "+viewModel.getCount(), Toast.LENGTH_SHORT).show();
                //viewModel.deleteAll();
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
        mSearch.setVisibility(View.GONE);
        searchToggle = false;

        loadData();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new Adapter(this, new ArrayList<Items>());
        mRecyclerView.setAdapter(mAdapter);

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        viewModel.getCatalog("%%").observe(this, new Observer<List<Items>>() {
            @Override
            public void onChanged(List<Items> items) {
                mAdapter.setItemList(items);
                mAdapter.notifyDataSetChanged();
            }
        });

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String search = "%"+newText+"%";
                viewModel.getCatalog(search).observe(MainActivity.this, new Observer<List<Items>>() {
                    @Override
                    public void onChanged(List<Items> items) {
                        mAdapter.setItemList(items);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                return true;
            }
        });

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
        } else {
        }
        if (mStorage.exist()) {
            mInventory = mStorage.getInventory();
        }
        viewModel.getCatalogNexus();
    }


    /**
     * Function called when clicking on an item
     *
     * @param position is the position on the index
     */

    @Override
    public void onItemClick(int position) {
        /*Intent itemIntent = new Intent(this, ItemActivity.class);
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
        startActivityForResult(itemIntent, STORAGE_VALUE); //require intent+int REQUEST_CODE*/
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
        switch (item.getItemId()) {
            case ADD_ONE:

                return true;
            case REMOVE_ONE:

                return true;
            case REMOVE_ALL:

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        mStorage.write(mInventory);
        super.onDestroy();
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
                break;
            case R.id.nav_update:
                creditCounter = 0;
                Toast.makeText(this, "Updating Platinum", Toast.LENGTH_SHORT).show();
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

    /*@Override
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
    }*/


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
    }
}