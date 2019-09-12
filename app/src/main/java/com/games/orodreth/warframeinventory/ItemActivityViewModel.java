package com.games.orodreth.warframeinventory;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.games.orodreth.warframeinventory.repository.Repository;
import com.games.orodreth.warframeinventory.repository.database.Inventory;
import com.games.orodreth.warframeinventory.repository.database.ItemsAndInventory;

import java.util.List;

public class ItemActivityViewModel extends AndroidViewModel {

    private Repository repository;

    public ItemActivityViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance();
        if(!repository.hasApplication()){
            repository.setApplication(application);
        }
    }

    public LiveData<List<ItemsAndInventory>> getCatalog(int id){
        return repository.getCatalog(id);
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
}
