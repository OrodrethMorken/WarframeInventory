package com.games.orodreth.warframeinventory;

import java.io.Serializable;

public class Items implements Serializable {
    private String mImageUrl;
    private String mItem;
    private String mUrl;
    private String mId;
    private int mDucat;
    private int mPlat;
    private int mPlatAvg;
    private float mDucPlat;

    public Items(String imageUrl, String item, String url_name, String id){
        mImageUrl = imageUrl;
        mItem = item;
        mDucat = 0;
        mUrl = url_name;
        mId = id;
        mPlat = 0;
        mPlatAvg = 0;
        mDucPlat = 0;
    }

    public String getImageUrl(){
        return mImageUrl;
    }

    public String getItem(){
        return mItem;
    }

    public String getId(){
        return mId;
    }

    public String getUrl(){
        return mUrl;
    }

    public int getDucats(){
        return mDucat;
    }

    public int getPlat(){
        return mPlat;
    }

    public float getDucPlat(){
        return mDucPlat;
    }

    public void setDucPlat(float ducPlat){
        mDucPlat = ducPlat;
    }

    public void setDucat(int ducat){
        mDucat = ducat;
    }

    public void setPlat(int plat){
        mPlat = plat;
    }

    public int getPlatAvg() {
        return mPlatAvg;
    }

    public void setPlatAvg(int mPlatAvg) {
        this.mPlatAvg = mPlatAvg;
    }
}
