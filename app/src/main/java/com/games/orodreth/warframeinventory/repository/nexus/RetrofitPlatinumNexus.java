package com.games.orodreth.warframeinventory.repository.nexus;

import android.os.Handler;
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

public class RetrofitPlatinumNexus extends Thread {

    private static final String TAG = "RetrofitPlatinumNexus";
    private Retrofit retrofit;
    private Repository repository;
    private Observer<List<Items>> observerLoad;
    private LiveData<List<Items>> liveData;
    private ArrayList<Items> itemsArrayList;
    private Handler handler = new Handler();

    @Override
    public void run() {
        repository = Repository.getInstance();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        liveData = repository.getCatalog();
        observerLoad = new Observer<List<Items>>() {
            @Override
            public void onChanged(List<Items> items) {
                loadItems(items);
            }
        };
        handler.post(new Runnable() {
            @Override
            public void run() {
                liveData.observeForever(observerLoad);
            }
        });
        updatePrice();
    }

    private void loadItems(List<Items> items){
        itemsArrayList = (ArrayList<Items>) items;
        liveData.removeObserver(observerLoad);
    }

    private void updatePrice() {
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
                for (ObjectNexus object : prices) {
                    if (object.getComponents() != null) {
                        String name = object.getName();
                        for (ObjectNexus component : object.getComponents()) {
                            for (Items item : itemsArrayList) {
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
                        for (Items item : itemsArrayList) {
                            if (object.getName().equals(item.getName())) {
                                item.setPlat(object.getPrices().getSelling().getCurrent().getMin());
                                item.setPlatAvg(object.getPrices().getSelling().getCurrent().getMedian());
                                if(object.getPrices().getSelling().getCurrent().getMedian()!=0) {
                                    item.setDucPlat((double) item.getDucat() / object.getPrices().getSelling().getCurrent().getMedian());
                                }else {
                                    item.setDucPlat(item.getDucat());
                                }
                                repository.updateItem(item);
                                break;
                            }
                        }
                    }
                }
                repository.setLoadingProgress(0);
                Log.d(TAG, "onResponse: finished updating price");
            }

            @Override
            public void onFailure(Call<List<ObjectNexus>> call, Throwable t) {

            }
        });
    }
}
