package com.games.orodreth.warframeinventory;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;



public class Catalog implements Serializable {

    private ArrayList<Items> mItems;
    private File catalog;
    private Context mContext;

    public Catalog(Context context){
        mContext = context;
        mItems = new ArrayList<>();
        catalog = new File(mContext.getFilesDir(), "Catalog"); //TODO change from File system to DB system
    }

    public ArrayList<Items> getItems(){
        copyFile();
        System.out.println("XXX Number of items inside: "+mItems.size());
        return mItems;
    }

    private void copyFile(){ //copy the arraylist from the file to mItems
        System.out.println("XXX Reading File");
        try {
            FileInputStream fis = new FileInputStream(catalog);
            ObjectInputStream ois = new ObjectInputStream(fis);
            mItems = (ArrayList<Items>)ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("XXX Error Reading File 1");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("XXX Error Reading File 2");
            e.printStackTrace();
            catalog.delete();
        } catch (ClassNotFoundException e) {
            System.out.println("XXX Error Reading File 3");
            e.printStackTrace();
        }
    }

    private void saveFile(){ //write arraylist to the file
        System.out.println("XXX Writing File");
        try {
            FileOutputStream fos = new FileOutputStream(catalog, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            System.out.println("XXX Writing File object size: "+mItems.size());
            oos.writeObject(mItems);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            System.out.println("XXX Error Writing File 1");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("XXX Error Writing File 2");
            e.printStackTrace();
            catalog.delete();
        }
    }

    public boolean exist(){
        if(catalog.exists())return true;
        //read file catalog from the res
        try {
            InputStream fis = mContext.getResources().openRawResource(R.raw.catalog);
            ObjectInputStream ois = new ObjectInputStream(fis);
            mItems = (ArrayList<Items>)ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("XXX Error Reading File 1");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println("XXX Error Reading File 2");
            e.printStackTrace();
            catalog.delete();
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("XXX Error Reading File 3");
            e.printStackTrace();
            return false;
        }
        //write new file
        try {
            FileOutputStream fos = new FileOutputStream(catalog, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            System.out.println("XXX Writing File object size: "+mItems.size());
            oos.writeObject(mItems);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            System.out.println("XXX Error Writing File 1");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println("XXX Error Writing File 2");
            e.printStackTrace();
            catalog.delete();
            return false;
        }
        return true;
    }

    public void write(ArrayList<Items> items){
        mItems = items;
        saveFile();
    }
}