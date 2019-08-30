package com.games.orodreth.warframeinventory.repository.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Items.class, Inventory.class}, version = 2, exportSchema = false)
public abstract class Database extends RoomDatabase {
    private static Database instance;

    public abstract ItemsDao itemsDao();

    public abstract InventoryDao inventoryDao();

    public static synchronized Database getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), Database.class, "warframe_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
