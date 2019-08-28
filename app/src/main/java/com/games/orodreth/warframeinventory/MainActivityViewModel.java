package com.games.orodreth.warframeinventory;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.games.orodreth.warframeinventory.repository.Repository;
import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.Items;

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

    public LiveData<List<Items>> getCatalog(String search){
        return repository.getCatalog(search);
    }

    public LiveData<List<Inventory>> getInventory(){
        return repository.getInventory();
    }

    public void getCatalogNexus(){
        repository.getCatalogNexus();
    }

    public int getCount(){
        return repository.getCount();
    }

    public void deleteAll(){
        repository.deleteAllItems();
    }
}
