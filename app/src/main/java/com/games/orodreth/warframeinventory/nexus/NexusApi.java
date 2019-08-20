package com.games.orodreth.warframeinventory.nexus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NexusApi {

    @GET("https://api.nexushub.co/warframe/v1/items")
    Call<List<ObjectNexus>> getPrices();
}
