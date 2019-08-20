package com.games.orodreth.warframeinventory.nexus;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ObjectNexus {

    @SerializedName("name")
    private String name;

    private List<ObjectNexus>[] components;

    private Prices prices;

    public String getName() {
        return name;
    }

    public List<ObjectNexus>[] getComponents() {
        return components;
    }

    public Prices getPrices() {
        return prices;
    }

    private class Prices {
        private Selling selling;

        public Selling getSelling() {
            return selling;
        }

        private class Selling {
            private Current current;

            public Current getCurrent() {
                return current;
            }

            private class Current{
                private int min;
                private int median;

                public int getMin() {
                    return min;
                }

                public int getMedian() {
                    return median;
                }
            }
        }
    }


}
