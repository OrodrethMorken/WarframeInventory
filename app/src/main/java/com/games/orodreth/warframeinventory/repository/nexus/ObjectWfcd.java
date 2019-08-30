package com.games.orodreth.warframeinventory.repository.nexus;

import java.util.List;

public class ObjectWfcd {

    private String name;
    private String category;
    private String description;
    private boolean tradable;
    private List<ObjectWfcd> components;
    private int itemCount;
    private String imageName;
    private int ducats;
    private String type;

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTradable() {
        return tradable;
    }

    public List<ObjectWfcd> getComponents() {
        return components;
    }

    public int getItemCount() {
        return itemCount;
    }

    public String getImageName() {
        return imageName;
    }

    public int getDucats() {
        return ducats;
    }

    public String getType() {
        return type;
    }
}
