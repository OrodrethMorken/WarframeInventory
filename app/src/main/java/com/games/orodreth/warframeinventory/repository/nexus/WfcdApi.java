package com.games.orodreth.warframeinventory.repository.nexus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WfcdApi {

    @GET("All.json")
    Call<List<ObjectWfcd>> getItems();
}
