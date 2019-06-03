package com.games.orodreth.warframeinventory;

import java.io.Serializable;

/**
 * Base of the inventory, store name and quantity of a single item
 */

public class Inventory implements Serializable {

    private String mName;
    private int mQuantity = 0;

    public Inventory(String name, int quantity) {
        mName = name;
        mQuantity = quantity;
    }

    public String getName() {
        return mName;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void add(){
        mQuantity++;
    }

    public void substract(){
        mQuantity--;
    }

    public void set(int intExtra) {
        mQuantity = intExtra;
    }
}
