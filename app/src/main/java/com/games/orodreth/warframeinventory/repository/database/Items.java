package com.games.orodreth.warframeinventory.repository.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "catalog_table")
public class Items{

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String category;
    private boolean tradable;
    private String imageUrl;
    private int ducat;
    private int plat;
    private int platAvg;
    private double ducPlat;

    private String urlWFM;

    public Items(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isTradable() {
        return tradable;
    }

    public void setTradable(boolean tradable) {
        this.tradable = tradable;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDucat() {
        return ducat;
    }

    public void setDucat(int ducat) {
        this.ducat = ducat;
    }

    public int getPlat() {
        return plat;
    }

    public void setPlat(int plat) {
        this.plat = plat;
    }

    public int getPlatAvg() {
        return platAvg;
    }

    public void setPlatAvg(int platAvg) {
        this.platAvg = platAvg;
    }

    public double getDucPlat() {
        return ducPlat;
    }

    public void setDucPlat(double ducPlat) {
        this.ducPlat = ducPlat;
    }

    public String getUrlWFM() {
        return urlWFM;
    }

    public void setUrlWFM(String urlWFM) {
        this.urlWFM = urlWFM;
    }
}
