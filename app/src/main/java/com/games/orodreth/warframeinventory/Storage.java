package com.games.orodreth.warframeinventory;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class Storage implements Serializable {

    private ArrayList<Inventory> mInventory;
    private File storage;
    private Context mContext;
    private static final String TAG = "Storage";

    public Storage(Context context){
        mContext = context;
        mInventory = new ArrayList<>();
        storage = new File(mContext.getFilesDir(), "Storage"); //TODO change from File system to DB system
    }

    public ArrayList<Inventory> getInventory(){
        copyFile();
        Log.d(TAG, "Number of items inside: "+ mInventory.size());
        return mInventory;
    }

    private void copyFile(){ //copy the arraylist from the file to mInventory
        Log.d(TAG, "Reading File");
        try {
            FileInputStream fis = new FileInputStream(storage);
            ObjectInputStream ois = new ObjectInputStream(fis);
            mInventory = (ArrayList<Inventory>)ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Error Reading File 1");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Error Reading File 2");
            e.printStackTrace();
            storage.delete();
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Error Reading File 3");
            e.printStackTrace();
        }
    }

    private void saveFile(){ //write arraylist to the file
        Log.d(TAG, "Writing File");
        try {
            FileOutputStream fos = new FileOutputStream(storage, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            Log.d(TAG, "Writing File object size: "+ mInventory.size());
            oos.writeObject(mInventory);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Error Writing File 1");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Error Writing File 2");
            e.printStackTrace();
            storage.delete();
        }
    }

    public boolean exist(){
        return storage.exists();
    }

    public void write(ArrayList<Inventory> items){
        mInventory = items;
        saveFile();
    }
}