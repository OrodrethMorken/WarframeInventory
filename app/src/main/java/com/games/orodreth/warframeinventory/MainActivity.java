package com.games.orodreth.warframeinventory;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.games.orodreth.warframeinventory.repository.Repository;
import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.Items;
import com.games.orodreth.warframeinventory.repository.database.ItemsAndInventory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.games.orodreth.warframeinventory.Adapter.ADD_ONE;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ALL;
import static com.games.orodreth.warframeinventory.Adapter.REMOVE_ONE;

public class MainActivity extends AppCompatActivity implements Adapter.OnItemClickListener {
    public static final String EXTRA_ID = "com.games.orodreth.warframeinventory.item_id";
    public static final String SHARED = "com.games.orodreth.warframeinventory.shared";
    public static final int STORAGE_VALUE = 17;
    public static final int ADAPTER_CATALOG = 0;
    private static final int ADAPTER_STORAGE = 1;
    public static final int INVERTED = -1;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ArrayList<String> category;
    private Observer<List<ItemsAndInventory>> observer;
    private LiveData<List<ItemsAndInventory>> livedata;
    private ArrayList<Items> mItems;
    private ArrayList<Items> filteredList;
    private ArrayList<Inventory> mInventory;
    private RequestQueue mRequestQueue;
    private Spinner spinner;
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();
    private int focus; //determine if it's visible the catalog or the storage
    private String sorting;
    private SearchView mSearch;
    private ConstraintLayout searchLayout;
    private boolean searchToggle;
    private boolean order;
    private boolean removeZero;
    private int sourceSelected;

    private MainActivityViewModel viewModel;
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
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_inventory:
                        if (menuItem.getTitle().equals(getResources().getString(R.string.catalog))) {
                            menuItem.setTitle(R.string.inventory);
                            menuItem.setIcon(R.drawable.ic_inventory);
                            focus = ADAPTER_CATALOG;
                            updateRecycleView();
                        } else {
                            menuItem.setTitle(R.string.catalog);
                            menuItem.setIcon(R.drawable.ic_catalog);
                            focus = ADAPTER_STORAGE;
                            updateRecycleView();
                        }
                        return true;
                    case R.id.nav_search:
                        if (searchLayout.getVisibility() == View.GONE) {
                            searchLayout.setVisibility(View.VISIBLE);
                        } else {
                            searchLayout.setVisibility(View.GONE);
                        }
                        return false;
                    case R.id.nav_update:
                        viewModel.updatePlatinum();
                        return false;
                    default:
                        return false;
                }
            }
        });

        mProgressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recycler_view);

        mSearch = findViewById(R.id.searchView);
        mSearch.setSubmitButtonEnabled(true);
        spinner = findViewById(R.id.spinnerCategory);

        searchLayout = findViewById(R.id.search_layout);
        searchLayout.setVisibility(View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new Adapter(this, new ArrayList<ItemsAndInventory>());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemListener(this);

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        observer = new Observer<List<ItemsAndInventory>>() {
            @Override
            public void onChanged(List<ItemsAndInventory> items) {
                mAdapter.setItemList(items);
                mAdapter.notifyDataSetChanged();
            }
        };

        category = new ArrayList<>();
        category.add("All");
        category.addAll(viewModel.getCategory());
        while (category.remove(null)) {
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, category);
        spinner.setAdapter(arrayAdapter);

        updateRecycleView();

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateRecycleView();
                return true;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateRecycleView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getLoadingProgress().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer progress) {
                mProgressBar.setProgress(progress);
            }
        });

        viewModel.getLoadingMax().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer size) {
                mProgressBar.setMax(size);
            }
        });
        focus = ADAPTER_CATALOG;
        sorting = Repository.fields[0];
        order = true;

        mRequestQueue = Volley.newRequestQueue(this);
    }

    private void updateRecycleView() {
        if (livedata != null) {
            livedata.removeObservers(this);
        }
        livedata = viewModel.getCatalog(mSearch.getQuery().toString(), category.get(spinner.getSelectedItemPosition()), sorting, order, focus, removeZero);
        livedata.observe(this, observer);
    }


    /**
     * Function called when clicking on an item
     *
     * @param id is the position on the index
     */

    @Override
    public void onItemClick(int id) {
        Intent itemIntent = new Intent(this, ItemActivity.class);
        itemIntent.putExtra(EXTRA_ID, id);
        startActivity(itemIntent); //require intent+int REQUEST_CODE*/
    }

    /**
     * Visualize the buttons on the toolbar
     *
     * @param menu menu
     * @return true
     */

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        viewModel.getSource().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean source) {
                if (source) {
                    MenuItem wfm = menu.findItem(R.id.market);
                    wfm.setChecked(true);
                } else {
                    MenuItem nexus = menu.findItem(R.id.nexus);
                    nexus.setChecked(true);
                }
            }
        });

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
        switch (item.getItemId()) {
            case R.id.sort_az:
                sorting = Repository.fields[0];
                item.setChecked(true);
                updateRecycleView();
                return true;
            case R.id.sort_duc:
                sorting = Repository.fields[1];
                item.setChecked(true);
                updateRecycleView();
                return true;
            case R.id.sort_duc_plat:
                sorting = Repository.fields[2];
                item.setChecked(true);
                updateRecycleView();
                return true;
            case R.id.sort_plat:
                sorting = Repository.fields[3];
                item.setChecked(true);
                updateRecycleView();
                return true;
            case R.id.sort_direction:
                if (item.isChecked()) {
                    item.setTitle(R.string.sort_desc);
                    order = false;
                } else {
                    item.setTitle(R.string.sort_asc);
                    order = true;
                }
                item.setChecked(order);
                updateRecycleView();
                return true;
            case R.id.sort_zero:
                removeZero = !item.isChecked();
                item.setChecked(removeZero);
                updateRecycleView();
                return true;
            case R.id.nexus:
                viewModel.setSource(false);
                item.setChecked(true);
                viewModel.updatePlatinum();
                return true;
            case R.id.market:
                viewModel.setSource(true);
                item.setChecked(true);
                viewModel.updatePlatinum();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                if (item.getOrder() == 0) {
                    viewModel.insertInventory(new Inventory(item.getGroupId(), 1));
                } else {
                    viewModel.updateInventory(new Inventory(item.getGroupId(), item.getOrder() + 1));
                }
                return true;
            case REMOVE_ONE:
                if (item.getOrder() > 1) {
                    viewModel.updateInventory(new Inventory(item.getGroupId(), item.getOrder() - 1));
                } else if (item.getOrder() == 1) {
                    viewModel.deleteInventory(new Inventory(item.getGroupId(), item.getOrder()));
                }
                return true;
            case REMOVE_ALL:
                if (item.getOrder() > 0) {
                    viewModel.deleteInventory(new Inventory(item.getGroupId(), item.getOrder()));
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
}