package com.games.orodreth.warframeinventory.nexus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WfcdApi {

    @GET("All.json")
    Call<List<ObjectWfcd>> getItems();
}
