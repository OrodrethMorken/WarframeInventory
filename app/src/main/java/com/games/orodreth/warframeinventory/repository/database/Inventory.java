package com.games.orodreth.warframeinventory.repository.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Base of the inventory, store name and quantity of a single item
 */
@Entity(tableName = "inventory_table")
public class Inventory{

    @PrimaryKey (autoGenerate = true)
    private int id;

    private String name;
    private int quantity;

    public Inventory(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
