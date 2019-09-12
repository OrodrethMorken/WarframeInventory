package com.games.orodreth.warframeinventory.repository.warframeMarket;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.games.orodreth.warframeinventory.repository.Repository;
import com.games.orodreth.warframeinventory.repository.database.Items;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitPlatinumWFM extends Thread {

    private Repository repository;
    private LiveData<List<Items>> livedata;
    private WfMaApi wfMaApi;
    private Observer<List<Items>> observerPrice;
    private ArrayList<Items> mItems;
    private Handler handler = new Handler();

    private static final String TAG = "RetrofitPlatinumWFM";

    @Override
    public void run() {
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.warframe.market/v1/")
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        
        repository = Repository.getInstance();
        livedata = repository.getCatalog();
        wfMaApi = retrofit.create(WfMaApi.class);
        observerPrice = new Observer<List<Items>>() {
            @Override
            public void onChanged(List<Items> items) {
                updatePrice(items);
            }
        };
        handler.post(new Runnable() {
            @Override
            public void run() {

                livedata.observeForever(observerPrice);
            }
        });
    }

    private void updatePrice(List<Items> items){
        livedata.removeObserver(observerPrice);
        Log.d(TAG, "updatePrice: start");
        mItems = (ArrayList<Items>) items;
        repository.setLoadingSize(mItems.size());
        for (int i=0; i < mItems.size(); i++) {
            if(mItems.get(i).getUrlWFM()==null){
                mItems.get(i).setUrlWFM(mItems.get(i).getName().toLowerCase().replace(" ","_"));
            }
            Call<PlatinumWFM> platinumWFMCall = wfMaApi.getstatistics(mItems.get(i).getUrlWFM());
            final int finalI = i;
            platinumWFMCall.enqueue(new Callback<PlatinumWFM>() {
                @Override
                public void onResponse(Call<PlatinumWFM> call, retrofit2.Response<PlatinumWFM> response) {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, "onResponse: code: " + response.code());
                        return;
                    }
                    PlatinumWFM platinumWFM = response.body();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            repository.setLoadingProgress(finalI);
                        }
                    });
                    List<PlatinumWFM.Payload.StatClose.Orders> orders = platinumWFM.getPayload().getStatistics_closed().getOrders();
                    if (orders.isEmpty()) return;
                    mItems.get(finalI).setPlat(orders.get(orders.size() - 1).getMin_price());
                    mItems.get(finalI).setPlatAvg(orders.get(orders.size() - 1).getMedian());
                    if(orders.get(orders.size() - 1).getMedian()!=0) {
                        mItems.get(finalI).setDucPlat((double) mItems.get(finalI).getDucat() / orders.get(orders.size() - 1).getMedian());
                    }else {
                        mItems.get(finalI).setDucPlat(mItems.get(finalI).getDucat());
                    }
                    repository.updateItem(mItems.get(finalI));
                    if(finalI==mItems.size()-1){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                repository.setLoadingProgress(0);
                            }
                        });
                        Log.d(TAG, "onResponse plat: end");
                    }
                }

                @Override
                public void onFailure(Call<PlatinumWFM> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                }
            });
        }
    }
}
