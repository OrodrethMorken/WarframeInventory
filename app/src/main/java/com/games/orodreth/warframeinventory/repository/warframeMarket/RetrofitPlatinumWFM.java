package com.games.orodreth.warframeinventory.repository.warframeMarket;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.warframe.market/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        repository = Repository.getInstance();
        livedata = repository.getCatalog("%%");
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
                    repository.setLoadingProgress(finalI);
                    List<PlatinumWFM.Payload.StatClose.Orders> orders = platinumWFM.getPayload().getStatistics_closed().getOrders();
                    if (orders.isEmpty()) return;
                    mItems.get(finalI).setPlat(orders.get(orders.size() - 1).getMin_price());
                    mItems.get(finalI).setPlatAvg((int) orders.get(orders.size() - 1).getMedian());
                    repository.updateItem(mItems.get(finalI));
                    if(finalI==mItems.size()-1){
                        repository.setLoadingProgress(0);
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
