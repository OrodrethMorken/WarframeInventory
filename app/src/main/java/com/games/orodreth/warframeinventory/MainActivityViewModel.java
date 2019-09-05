package com.games.orodreth.warframeinventory;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.games.orodreth.warframeinventory.repository.Repository;
import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.ItemsAndInventory;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {

    private Repository repository;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance();
        if(!repository.hasApplication()){
            repository.setApplication(application);
        }
    }

    public void getCatalogRetrofit(){
        repository.getCatalogRetrofit();
    }

    public LiveData<Integer> getLoadingProgress(){
        return repository.getLoadingProgress();
    }

    public LiveData<Integer> getLoadingMax(){
        return repository.getLoadingMax();
    }

    public LiveData<Boolean> getSource(){
        return repository.getSource();
    }

    public void setSource(boolean source){
        repository.setSource(source);
    }

    public void updatePlatinum(){
        repository.updatePlatinum();
    }

    public void deleteAll(){
        repository.deleteAllItems();
    }

    public LiveData<List<ItemsAndInventory>> getCatalog(String search, String category, String field, boolean direction, int focus) {
        return repository.getCatalog(search, category, field, direction, focus);
    }

    public void insertInventory(Inventory inventory){
        repository.insertInventory(inventory);
    }

    public void updateInventory(Inventory inventory){
        repository.updateInventory(inventory);
    }

    public void deleteInventory(Inventory inventory){
        repository.deleteInventory(inventory);
    }

    public List<String> getCategory(){
        return repository.getCategory();
    }
}
