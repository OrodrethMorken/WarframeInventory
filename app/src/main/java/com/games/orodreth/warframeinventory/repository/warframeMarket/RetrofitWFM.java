package com.games.orodreth.warframeinventory.repository.warframeMarket;

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

public class RetrofitWFM implements Runnable {

    private static final String TAG = "RetrofitWFM";
    private Retrofit retrofit;
    private ArrayList<Items> mItems;
    private Repository repository;
    private LiveData<List<Items>> livedata;
    private Observer<List<Items>> observerDatabase;
    private Observer<List<Items>> observerDucats;
    private WfMaApi wfMaApi;

    @Override
    public void run() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.warframe.market/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        repository = Repository.getInstance();
        livedata = repository.getCatalog();
        wfMaApi = retrofit.create(WfMaApi.class);
        buildDatabase();
    }

    private void buildDatabase(){
        //repository.deleteAllItems();
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
                    Items item = new Items(object.getItem_name(), object.getThumb());
                    item.setUrlWFM(object.getUrl_name());
                    mItems.add(item);
                }
                checkDatabase();
            }

            @Override
            public void onFailure(Call<ObjectWFM> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    private void checkDatabase(){
        observerDatabase = new Observer<List<Items>>() {
            @Override
            public void onChanged(List<Items> items) {
                updateDatabase(items);
            }
        };
        livedata.observeForever(observerDatabase);
    }

    private void updateDatabase(List<Items> items){
        livedata.removeObserver(observerDatabase);
        Log.d(TAG, "updateDatabase: start");
        repository.setLoadingSize(mItems.size());
        for (Items object:mItems) {
            repository.setLoadingProgress(mItems.indexOf(object));
            Items found = null;
            for (Items i :items) {
                if(i.getName().equals(object.getName())){
                    found = i;
                    break;
                }
            }
            if(found != null){
                object.setId(found.getId());
                repository.updateItem(object);
            }else {
                repository.insertItem(object);
            }
        }
        Log.d(TAG, "updateDatabase: end");
        checkDucats();
    }

    private void checkDucats(){
        observerDucats = new Observer<List<Items>>() {
            @Override
            public void onChanged(List<Items> items) {
                updateDucats(items);
            }
        };
        livedata.observeForever(observerDucats);
    }

    private void updateDucats(List<Items> items){
        mItems = (ArrayList<Items>) items;
        Log.d(TAG, "updateDucats: start");
        repository.setLoadingSize(mItems.size());
        livedata.removeObserver(observerDucats);
        for (int i=0; i<mItems.size(); i++) {
            WfMaApi wfMaApi = retrofit.create(WfMaApi.class);
            Call<DucatsWFM> ducatsWFMCall = wfMaApi.getDucats(mItems.get(i).getUrlWFM());
            final int finalI = i;
            ducatsWFMCall.enqueue(new Callback<DucatsWFM>() {
                @Override
                public void onResponse(Call<DucatsWFM> call, retrofit2.Response<DucatsWFM> response) {
                    if(!response.isSuccessful()){
                        Log.d(TAG, "onResponse: code: "+response.code());
                    }
                    DucatsWFM ducatsWFM = response.body();
                    repository.setLoadingProgress(finalI);
                    Log.d(TAG, "Item :"+mItems.get(finalI).getName()+" i: "+finalI);
                    String id = ducatsWFM.getPayload().getItem().getId();
                    for (DucatsWFM.Payload.Item.ItemInSet item:ducatsWFM.getPayload().getItem().getItems_in_set()) {
                        if(item.getId().equals(id)){
                            mItems.get(finalI).setDucat(item.getDucats());
                            if(item.getDucats()>0) {
                                Log.d(TAG, mItems.get(finalI).getName()+"ducat site: " + item.getDucats() + " ducat stored: " + mItems.get(finalI).getDucat());
                            }
                            repository.updateItem(mItems.get(finalI));
                            break;
                        }
                    }
                    if(finalI == mItems.size()-1){
                        Log.d(TAG, "onResponse ducats: end");
                        checkPrice();
                    }
                }

                @Override
                public void onFailure(Call<DucatsWFM> call, Throwable t) {
                    Log.d(TAG, "onFailure: "+t.getMessage());
                }
            });
        }
    }

    private void checkPrice(){
        new RetrofitPlatinumWFM().start();
    }
}
