package com.games.orodreth.warframeinventory.repository.nexus;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.games.orodreth.warframeinventory.repository.Repository;
import com.games.orodreth.warframeinventory.repository.database.Items;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitNexus implements Runnable {

    private static final String TAG = "RetrofitNexus";
    private Retrofit retrofit;
    private ArrayList<Items> itemsArrayList;
    private Repository repository;
    private String search = "";
    private Observer<List<Items>> observer;
    private LiveData<List<Items>> liveData;

    @Override
    public void run() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        repository = Repository.getInstance();
        buildDatabase();
    }

    private void buildDatabase() {
        WfcdApi wfcdApi = retrofit.create(WfcdApi.class);
        itemsArrayList = new ArrayList<>();
        Call<List<ObjectWfcd>> listCall = wfcdApi.getItems();
        listCall.enqueue(new Callback<List<ObjectWfcd>>() {
            @Override
            public void onResponse(Call<List<ObjectWfcd>> call, retrofit2.Response<List<ObjectWfcd>> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: code: " + response.code());
                    return;
                }

                List<ObjectWfcd> items = response.body();
                for (ObjectWfcd object : items) {
                    if (object.getComponents() != null && !object.getCategory().equals("Misc")) {
                        String name = object.getName();
                        for (ObjectWfcd i :
                                object.getComponents()) {
                            if(i.isTradable()) {
                                String fullname = name + " " + i.getName();
                                Items item = new Items(fullname, i.getImageName());
                                item.setCategory(i.getCategory());
                                item.setTradable(i.isTradable());
                                item.setDucat(i.getDucats());
                                itemsArrayList.add(item);
                            }
                        }
                    } else {
                        if(object.isTradable()) {
                            Items item = new Items(object.getName(), object.getImageName());
                            item.setCategory(object.getCategory());
                            item.setTradable(object.isTradable());
                            item.setDucat(object.getDucats());
                            itemsArrayList.add(item);
                        }
                    }
                }
                Log.d(TAG, "onResponse: finished reading json");
                readDatabase();
            }

            @Override
            public void onFailure(Call<List<ObjectWfcd>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void readDatabase() {
        Log.d(TAG, "readDatabase: start reading database");
        observer = new Observer<List<Items>>() {
            @Override
            public void onChanged(List<Items> items) {
                updateDatabase(items);
                Log.d(TAG, "onChanged: finished reading database");
            }
        };
        liveData = repository.getCatalog(search);
        liveData.observeForever(observer);
    }

    private void updateDatabase(List<Items> items){
        liveData.removeObserver(observer);
        for (Items i :itemsArrayList) {
            boolean found = false;
            for (Items j :
                    items) {
                if (i.getName().equals(j.getName())) {
                    found = true;
                    j.setCategory(j.getCategory());
                    j.setTradable(j.isTradable());
                    j.setDucat(j.getDucat());
                    repository.updateItem(j);
                    break;
                }
            }
            if(!found){
                repository.insertItem(i);
            }
        }for (Items i :items) {
            boolean found = false;
            for (Items j :
                    itemsArrayList) {
                if (i.getName().equals(j.getName())){
                    found = true;
                    break;
                }
            }
            if(!found){
                repository.deleteItem(i);
            }
        }
        Log.d(TAG, "onChanged: finished updating database");
        updatePrice(itemsArrayList);
    }

    private void updatePrice(List<Items> catalog) {
        itemsArrayList = (ArrayList<Items>) catalog;
        NexusApi nexusApi = retrofit.create(NexusApi.class);
        Call<List<ObjectNexus>> listPrice = nexusApi.getPrices();
        listPrice.enqueue(new Callback<List<ObjectNexus>>() {
            @Override
            public void onResponse(Call<List<ObjectNexus>> call, retrofit2.Response<List<ObjectNexus>> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: code: " + response.code());
                    return;
                }

                List<ObjectNexus> prices = response.body();
                for (ObjectNexus object :
                        prices) {
                    if (object.getComponents() != null) {
                        String name = object.getName();
                        for (ObjectNexus component :
                                object.getComponents()) {

                            for (Items item :
                                    itemsArrayList) {
                                String fullname = name + " " +component.getName();
                                if (fullname.equals(item.getName())) {
                                    try {
                                        item.setPlat(component.getPrices().getSelling().getCurrent().getMin());
                                        item.setPlatAvg(component.getPrices().getSelling().getCurrent().getMedian());
                                        Log.d(TAG, "onResponse: id " + item.getId());
                                        repository.updateItem(item);
                                    }catch (NullPointerException e){
                                        Log.e(TAG, "NullPointer: "+fullname, e);
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        for (Items item :
                                itemsArrayList) {
                            if (object.getName().equals(item.getName())) {
                                item.setPlat(object.getPrices().getSelling().getCurrent().getMin());
                                item.setPlatAvg(object.getPrices().getSelling().getCurrent().getMedian());
                                repository.updateItem(item);
                                break;
                            }
                        }
                    }
                }
                Log.d(TAG, "onResponse: finished updating price");
            }

            @Override
            public void onFailure(Call<List<ObjectNexus>> call, Throwable t) {

            }
        });
    }
}
