package com.games.orodreth.warframeinventory.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.games.orodreth.warframeinventory.repository.database.Database;
import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.InventoryDao;
import com.games.orodreth.warframeinventory.repository.database.Items;
import com.games.orodreth.warframeinventory.repository.database.ItemsAndInventory;
import com.games.orodreth.warframeinventory.repository.database.ItemsDao;
import com.games.orodreth.warframeinventory.repository.nexus.RetrofitNexus;
import com.games.orodreth.warframeinventory.repository.nexus.RetrofitPlatinumNexus;
import com.games.orodreth.warframeinventory.repository.warframeMarket.RetrofitPlatinumWFM;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Repository {
    private static Repository instance;

    private ItemsDao itemsDao;
    private InventoryDao inventoryDao;
    private Application application;

    private MutableLiveData<Integer> loadingProgress;
    private MutableLiveData<Integer> loadingMax;
    private MutableLiveData<Boolean> source;

    public static String[] fields = {"name", "ducat", "ducPlat", "plat", "platAvg"};

    public static synchronized Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    private Repository() {
        loadingProgress = new MutableLiveData<>();
        loadingProgress.setValue(0);
        loadingMax = new MutableLiveData<>();
        loadingMax.setValue(0);
        source = new MutableLiveData<>();
        source.setValue(false);
    }

    public boolean hasApplication() {
        return application != null;
    }

    public void setApplication(Application application) {
        this.application = application;
        Database database = Database.getInstance(application);
        itemsDao = database.itemsDao();
        inventoryDao = database.inventoryDao();
        if (getCount() == 0) {
            getCatalogRetrofit();
        }
    }

    public LiveData<List<Items>> getCatalog() {
        return itemsDao.getItems();
    }

    public LiveData<List<ItemsAndInventory>> getCatalog(String search, String category, String fields, boolean direction, int focus) {
        return itemsDao.getItems(search, category, fields, direction, focus);
    }

    public LiveData<List<Inventory>> getInventory() {
        return inventoryDao.getInventory();
    }

    public int getCount() {
        AsyncTask<String, Void, Integer> async = new GetCountAsync(itemsDao).execute("");
        try {
            return async.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static class GetCountAsync extends AsyncTask<String, Void, Integer> {

        private ItemsDao itemsDao;

        public GetCountAsync(ItemsDao itemsDao) {
            this.itemsDao = itemsDao;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            return itemsDao.getCount();
        }
    }

    public LiveData<Integer> getLoadingProgress() {
        return loadingProgress;
    }

    public LiveData<Integer> getLoadingMax() {
        return loadingMax;
    }

    public LiveData<Boolean> getSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source.setValue(source);
    }

    public void setLoadingProgress(int progress) {
        loadingProgress.setValue(progress);
    }

    public void setLoadingSize(int size) {
        loadingMax.setValue(size);
    }

    public void getCatalogRetrofit() {
        getCatalogNexus();
    }

    public void getCatalogNexus() {
        new Thread(new RetrofitNexus()).start();
    }

    public void updatePlatinum() {
        if (source.getValue()) {
            platinumWFM();
        } else {
            platinumNexus();
        }
    }

    public void platinumWFM() {
        new RetrofitPlatinumWFM().start();
    }

    public void platinumNexus() {
        new RetrofitPlatinumNexus().start();
    }

    public List<String> getCategory() {
        AsyncTask<Void, Void, List<String>> async = new GetCategoryAsync(itemsDao).execute();
        try {
            return async.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetCategoryAsync extends AsyncTask<Void, Void, List<String>> {

        private ItemsDao itemsDao;

        public GetCategoryAsync(ItemsDao itemsDao) {
            this.itemsDao = itemsDao;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            return itemsDao.getCategory();
        }
    }

    //Items Operation

    public void insertItem(Items item) {
        new InsertItemAsync(itemsDao).execute(item);
    }

    public void updateItem(Items item) {
        new UpdateItemAsync(itemsDao).execute(item);
    }

    public void deleteItem(Items item) {
        new DeleteItemAsync(itemsDao).execute(item);
    }

    public void deleteAllItems() {
        new DeleteAllItemAsync(itemsDao).execute();
    }

    private static class InsertItemAsync extends AsyncTask<Items, Void, Void> {

        private ItemsDao itemsDao;

        public InsertItemAsync(ItemsDao itemsDao) {
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.insert(items[0]);
            return null;
        }
    }

    private static class UpdateItemAsync extends AsyncTask<Items, Void, Void> {

        private ItemsDao itemsDao;

        public UpdateItemAsync(ItemsDao itemsDao) {
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.update(items[0]);
            return null;
        }
    }

    private static class DeleteItemAsync extends AsyncTask<Items, Void, Void> {

        private ItemsDao itemsDao;

        public DeleteItemAsync(ItemsDao itemsDao) {
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.delete(items[0]);
            return null;
        }
    }

    private static class DeleteAllItemAsync extends AsyncTask<Items, Void, Void> {

        private ItemsDao itemsDao;

        public DeleteAllItemAsync(ItemsDao itemsDao) {
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.deleteAll();
            return null;
        }
    }

    //Inventory Operation

    public void insertInventory(Inventory inventory) {
        new InsertInventoryAsync(inventoryDao).execute(inventory);
    }

    public void updateInventory(Inventory inventory) {
        new UpdateInventoryAsync(inventoryDao).execute(inventory);
    }

    public void deleteInventory(Inventory inventory) {
        new DeleteInventoryAsync(inventoryDao).execute(inventory);
    }

    public void deleteAllInventory() {
        new DeleteAllInventoryAsync(inventoryDao).execute();
    }

    private static class InsertInventoryAsync extends AsyncTask<Inventory, Void, Void> {

        private InventoryDao inventoryDao;

        public InsertInventoryAsync(InventoryDao inventoryDao) {
            this.inventoryDao = inventoryDao;
        }

        @Override
        protected Void doInBackground(Inventory... inventories) {
            inventoryDao.insert(inventories[0]);
            return null;
        }
    }

    private static class UpdateInventoryAsync extends AsyncTask<Inventory, Void, Void> {

        private InventoryDao inventoryDao;

        public UpdateInventoryAsync(InventoryDao inventoryDao) {
            this.inventoryDao = inventoryDao;
        }

        @Override
        protected Void doInBackground(Inventory... inventories) {
            inventoryDao.update(inventories[0]);
            return null;
        }
    }

    private static class DeleteInventoryAsync extends AsyncTask<Inventory, Void, Void> {

        private InventoryDao inventoryDao;

        public DeleteInventoryAsync(InventoryDao inventoryDao) {
            this.inventoryDao = inventoryDao;
        }

        @Override
        protected Void doInBackground(Inventory... inventories) {
            inventoryDao.delete(inventories[0]);
            return null;
        }
    }

    private static class DeleteAllInventoryAsync extends AsyncTask<Void, Void, Void> {

        private InventoryDao inventoryDao;

        public DeleteAllInventoryAsync(InventoryDao inventoryDao) {
            this.inventoryDao = inventoryDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            inventoryDao.deleteAll();
            return null;
        }
    }
}
