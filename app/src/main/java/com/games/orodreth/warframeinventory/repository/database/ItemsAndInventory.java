package com.games.orodreth.warframeinventory.repository.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.RawQuery;
import androidx.room.Relation;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

public class ItemsAndInventory {

    @Embedded
    public Items items;

    @Embedded
    public Inventory inventory;
}
