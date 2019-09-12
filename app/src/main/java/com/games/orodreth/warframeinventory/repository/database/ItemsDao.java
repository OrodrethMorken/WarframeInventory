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

import com.games.orodreth.warframeinventory.repository.Repository;

import java.util.List;

import static com.games.orodreth.warframeinventory.MainActivity.ADAPTER_CATALOG;

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

    @Query("SELECT * FROM catalog_table A LEFT JOIN inventory_table B ON A.id = B.item_id WHERE A.id = :id")
    public abstract LiveData<List<ItemsAndInventory>> getItems(int id);

    @RawQuery(observedEntities = ItemsAndInventory.class)
    abstract LiveData<List<ItemsAndInventory>> getItemsViaQuery (SupportSQLiteQuery query);

    public LiveData<List<ItemsAndInventory>> getItems(String search, String category, String field, boolean direction, int focus, boolean removeZero){
        String text = "SELECT * FROM catalog_table A ";
        if(focus == ADAPTER_CATALOG){
            text += "LEFT JOIN inventory_table B ON A.id = B.item_id";
        }else {
            text += "INNER JOIN inventory_table B ON A.id = B.item_id";
        }
        boolean search_found = false;
        boolean category_found = false;
        if(search!=null && !search.trim().isEmpty()){
            text += " WHERE name LIKE \"%"+search+"%\"";
            search_found = true;
        }
        if(category!= null && !category.trim().isEmpty() && !category.equals("All")){
            if(search_found){
                text += " AND category LIKE \""+category+"\"";
            }else {
                text += " WHERE category LIKE \""+category+"\"";
                category_found = true;
            }
        }
        if(removeZero && field != Repository.fields[0]){
            if(search_found || category_found){
                text += " AND "+ field+ " > 0";
            }
            else {
                text += " WHERE "+ field + " > 0";
            }
        }
        text += " ORDER BY "+field;
        if(direction){
            text += " ASC";
        }else {
            text += " DESC";
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
