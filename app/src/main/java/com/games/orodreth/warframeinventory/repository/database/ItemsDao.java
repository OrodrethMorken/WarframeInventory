package com.games.orodreth.warframeinventory.repository.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public abstract class ItemsDao {

    @Insert
    public abstract void insert(Items item);

    @Update
    public abstract void update (Items item);

    @Delete
    public abstract void delete (Items item);

    @Query("DELETE FROM catalog_table")
    public abstract void deleteAll ();

    @Query("SELECT * FROM catalog_table WHERE name LIKE :search ORDER BY name ASC ")
    public abstract LiveData<List<Items>> getItems (String search);

    /*@Query("SELECT * FROM catalog_table WHERE name LIKE :search ORDER BY CASE WHEN :direction = 1 THEN :field END ASC, CASE WHEN :direction = 0 THEN :field END DESC")
    LiveData<List<ItemsAndInventory>> getItems (String search, String field, boolean direction);*/

    /*@Query("SELECT * FROM catalog_table A LEFT JOIN inventory_table B ON A.id = B.item_id WHERE A.name LIKE :search ORDER BY CASE WHEN :direction = 1 THEN :field END ASC, CASE WHEN :direction = 0 THEN :field END DESC")
    public abstract LiveData<List<ItemsAndInventory>> getItems (String search, String field, boolean direction);*/

    @RawQuery(observedEntities = ItemsAndInventory.class)
    abstract LiveData<List<ItemsAndInventory>> getItemsViaQuery (SupportSQLiteQuery query);

    public LiveData<List<ItemsAndInventory>> getItems(String search, String field, boolean direction ){
        String text = "SELECT * FROM catalog_table A LEFT JOIN inventory_table B ON A.id = B.item_id";
        if(search!=null && !search.trim().isEmpty()){
            text += " WHERE name LIKE \""+search+"\"";
        }
        text += " ORDER BY "+field;
        if(direction){
            text += " ASC";
        }else {
            text = " DESC";
        }
        SupportSQLiteQuery query = new SimpleSQLiteQuery(text);
        return getItemsViaQuery(query);
    }

    public LiveData<List<ItemsAndInventory>> getItems(String search, String category, String field, boolean direction){
        String text = "SELECT * FROM catalog_table A LEFT JOIN inventory_table B ON A.id = B.item_id";
        boolean search_found = false;
        if(search!=null && !search.trim().isEmpty()){
            text += " WHERE name LIKE \""+search+"\"";
            search_found = true;
        }
        if(category!= null && !category.trim().isEmpty()){
            if(search_found){
                text += " AND category LIKE \""+category+"\"";
            }else {
                text += " WHERE category LIKE \""+category+"\"";
            }
        }
        text += " ORDER BY "+field;
        if(direction){
            text += " ASC";
        }else {
            text = " DESC";
        }
        SupportSQLiteQuery query = new SimpleSQLiteQuery(text);
        return getItemsViaQuery(query);
    }

    @Query("SELECT * FROM catalog_table")
    public abstract LiveData<List<Items>> getItems ();

    @Query("SELECT COUNT(id) FROM catalog_table")
    public abstract int getCount();

    @Query("SELECT DISTINCT category FROM catalog_table ORDER BY category ASC")
    public abstract List<String> getCategory();
}
