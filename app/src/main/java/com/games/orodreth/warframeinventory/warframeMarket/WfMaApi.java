package com.games.orodreth.warframeinventory.warframeMarket;

import com.games.orodreth.warframeinventory.nexus.ObjectWfcd;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WfMaApi {

    @GET("items")
    Call<ObjectWFM> getItems();

    @GET("items/{url_name}")
    Call<DucatsWFM> getDucats(@Path("url_name") String url_name);

    @GET("items/{url_name}/statistics")
    Call<PlatinumWFM> getstatistics(@Path("url_name") String url_name);
}
