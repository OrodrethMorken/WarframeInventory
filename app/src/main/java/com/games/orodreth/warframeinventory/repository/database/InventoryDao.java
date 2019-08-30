package com.games.orodreth.warframeinventory.repository.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InventoryDao {

    @Insert
    void insert(Inventory inventory);

    @Update
    void update (Inventory inventory);

    @Delete
    void delete (Inventory inventory);

    @Query("DELETE FROM inventory_table")
    void deleteAll ();

    @Query("SELECT * FROM inventory_table")
    LiveData<List<Inventory>> getInventory();

    @Query("SELECT COUNT(item_id) FROM inventory_table")
    int getCount();
}
