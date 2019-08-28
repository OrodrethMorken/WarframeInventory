package com.games.orodreth.warframeinventory.repository.warframeMarket;

import android.util.Log;

import com.games.orodreth.warframeinventory.repository.database.Items;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitWFM implements Runnable {

    private static final String TAG = "RetrofitWFM";
    private Retrofit retrofit;
    private ArrayList<Items> mItems;

    @Override
    public void run() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.warframe.market/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        buildDatabase();
    }

    private void buildDatabase(){
        final WfMaApi wfMaApi = retrofit.create(WfMaApi.class);
        Call<ObjectWFM> listObject = wfMaApi.getItems();
        listObject.enqueue(new Callback<ObjectWFM>() {
            @Override
            public void onResponse(Call<ObjectWFM> call, retrofit2.Response<ObjectWFM> response) {
                if(!response.isSuccessful()){
                    Log.d(TAG, "onResponse: code: "+response.code());
                    return;
                }
                ObjectWFM objectWFM = response.body();
                mItems = new ArrayList<>();
                for (ObjectWFM.Payload.WFItems object:objectWFM.getPayload().getItems()) {
                    Items item = new Items(object.getThumb(), object.getItem_name());
                    item.setUrlWFM(object.getUrl_name());
                    mItems.add(item);
                }
                for (int i=0; i<mItems.size(); i++
                ) {
                    Call<DucatsWFM> ducatsWFMCall = wfMaApi.getDucats(mItems.get(i).getUrlWFM());
                    final int finalI = i;
                    ducatsWFMCall.enqueue(new Callback<DucatsWFM>() {
                        @Override
                        public void onResponse(Call<DucatsWFM> call, retrofit2.Response<DucatsWFM> response) {
                            if(!response.isSuccessful()){
                                Log.d(TAG, "onResponse: code: "+response.code());
                            }
                            DucatsWFM ducatsWFM = response.body();
                            String id = ducatsWFM.getPayload().getItem().getId();
                            for (DucatsWFM.Payload.Item.ItemInSet item:ducatsWFM.getPayload().getItem().getItems_in_set()) {
                                if(item.getId().equals(id)){
                                    mItems.get(finalI).setDucat(item.getDucats());
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DucatsWFM> call, Throwable t) {
                            Log.d(TAG, "onFailure: "+t.getMessage());
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<ObjectWFM> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    private void updatePrice(){
        WfMaApi wfMaApi = retrofit.create(WfMaApi.class);
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
                    List<PlatinumWFM.Payload.StatClose.Orders> orders = platinumWFM.getPayload().getStatistics_closed().getOrders();
                    if (orders.isEmpty()) return;
                    mItems.get(finalI).setPlat(orders.get(orders.size() - 1).getMin_price());
                    mItems.get(finalI).setPlatAvg((int) orders.get(orders.size() - 1).getMedian());
                    //repository.updatePrice(mItems.get(finalI); TODO update prize on database
                }

                @Override
                public void onFailure(Call<PlatinumWFM> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                }
            });
        }
    }
}