package com.games.orodreth.warframeinventory.repository.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemsDao {

    @Insert
    void insert (Items item);

    @Update
    void update (Items item);

    @Delete
    void delete (Items item);

    @Query("DELETE FROM catalog_table")
    void deleteAll ();

    @Query("SELECT * FROM catalog_table WHERE name LIKE :search ORDER BY name ASC ")
    LiveData<List<Items>> getItems (String search);

    @Query("SELECT * FROM catalog_table")
    LiveData<List<Items>> getItems ();

    @Query("SELECT COUNT(id) FROM catalog_table")
    int getCount();
}
