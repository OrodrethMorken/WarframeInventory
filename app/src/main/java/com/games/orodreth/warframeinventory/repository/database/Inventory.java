package com.games.orodreth.warframeinventory.repository.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Base of the inventory, store name and quantity of a single item
 */
@Entity(tableName = "inventory_table")
public class Inventory{

    @PrimaryKey (autoGenerate = false)
    private int item_id;
    private int quantity;

    public Inventory(int item_id, int quantity) {
        this.item_id = item_id;
        this.quantity = quantity;
    }

    /*public String getName() {
        return name;
    }*/

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
