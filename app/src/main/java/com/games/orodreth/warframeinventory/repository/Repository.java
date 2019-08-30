package com.games.orodreth.warframeinventory.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.games.orodreth.warframeinventory.repository.database.Database;
import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.InventoryDao;
import com.games.orodreth.warframeinventory.repository.database.Items;
import com.games.orodreth.warframeinventory.repository.database.ItemsDao;
import com.games.orodreth.warframeinventory.repository.nexus.RetrofitNexus;
import com.games.orodreth.warframeinventory.repository.warframeMarket.RetrofitWFM;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Repository {

    private static Repository instance;
    private ItemsDao itemsDao;
    private InventoryDao inventoryDao;
    private Application application;

    private MutableLiveData<Integer> loadingProgress;
    private MutableLiveData<Integer> loadingMax;
    private LiveData<List<Inventory>> inventory;

    public static synchronized Repository getInstance(){
        if(instance == null){
            instance = new Repository();
        }
        return instance;
    }

    private Repository (){
        loadingProgress = new MutableLiveData<>();
        loadingProgress.setValue(0);
        loadingMax = new MutableLiveData<>();
        loadingMax.setValue(0);
    }

    public boolean hasApplication(){
        return application!=null;
    }

    public void setApplication (Application application){
        this.application = application;
        Database database = Database.getInstance(application);
        itemsDao = database.itemsDao();
        inventoryDao = database.inventoryDao();
    }

    public LiveData<List<Items>> getCatalog(String search) {
        return itemsDao.getItems(search);
    }

    public LiveData<List<Inventory>> getInventory(){
        return inventoryDao.getInventory();
    }

    public int getCount(){
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

    private class GetCountAsync extends AsyncTask<String, Void, Integer>{

        private ItemsDao itemsDao;

        public GetCountAsync(ItemsDao itemsDao){
            this.itemsDao = itemsDao;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            return itemsDao.getCount();
        }
    }

    public LiveData<Integer> getLoadingProgress(){
        return loadingProgress;
    }

    public LiveData<Integer> getLoadingMax(){
        return loadingMax;
    }

    public void setLoadingProgress (int progress) {
        loadingProgress.setValue(progress);
    }

    public void setLoadingSize (int size) {
        loadingMax.setValue(size);
    }

    //Items Operation

    public void insertItem (Items item){
        new InsertItemAsync(itemsDao).execute(item);
    }

    public void updateItem (Items item){
        new UpdateItemAsync(itemsDao).execute(item);
    }

    public void deleteItem (Items item){
        new DeleteItemAsync(itemsDao).execute(item);
    }

    public void deleteAllItems(){
        new DeleteAllItemAsync(itemsDao).execute();
    }

    public void getCatalogRetrofit() {
        //getCatalogWFM();
        getCatalogNexus();
    }

    public void getCatalogNexus(){
        new Thread(new RetrofitNexus()).start();
    }

    public void getCatalogWFM(){
        new Thread(new RetrofitWFM()).start();
    }

    private static class InsertItemAsync extends AsyncTask<Items, Void, Void>{

        private ItemsDao itemsDao;

        public InsertItemAsync(ItemsDao itemsDao){
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.insert(items[0]);
            return null;
        }
    }

    private static class UpdateItemAsync extends AsyncTask<Items, Void, Void>{

        private ItemsDao itemsDao;

        public UpdateItemAsync(ItemsDao itemsDao){
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.update(items[0]);
            return null;
        }
    }

    private static class DeleteItemAsync extends AsyncTask<Items, Void, Void>{

        private ItemsDao itemsDao;

        public DeleteItemAsync(ItemsDao itemsDao){
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.delete(items[0]);
            return null;
        }
    }

    private static class DeleteAllItemAsync extends AsyncTask<Items, Void, Void>{

        private ItemsDao itemsDao;

        public DeleteAllItemAsync(ItemsDao itemsDao){
            this.itemsDao = itemsDao;
        }

        @Override
        protected Void doInBackground(Items... items) {
            itemsDao.deleteAll();
            return null;
        }
    }

    //Inventory Operation

    public void insertInventory (Inventory inventory){
        inventoryDao.insert(inventory);
    }

    public void updateInventory (Inventory inventory){
        inventoryDao.update(inventory);
    }

    public void deleteInventory (Inventory inventory){
        inventoryDao.delete(inventory);
    }

    public void deleteAllInventory() {
        inventoryDao.deleteAll();
    }
}
