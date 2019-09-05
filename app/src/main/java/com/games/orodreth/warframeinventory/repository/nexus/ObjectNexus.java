package com.games.orodreth.warframeinventory.repository.nexus;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ObjectNexus {

    @SerializedName("name")
    private String name;

    private List<ObjectNexus> components;

    private Prices prices;

    public String getName() {
        return name;
    }

    public List<ObjectNexus> getComponents() {
        return components;
    }

    public Prices getPrices() {
        return prices;
    }

    public class Prices {
        private Selling selling;

        public Selling getSelling() {
            return selling;
        }

        public class Selling {
            private Current current;

            public Current getCurrent() {
                return current;
            }

            public class Current{
                private int min;
                private double median;

                public int getMin() {
                    return min;
                }

                public double getMedian() {
                    return median;
                }
            }
        }
    }


}
