package com.games.orodreth.warframeinventory.repository.warframeMarket;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlatinumWFM {

    private Payload payload;

    public Payload getPayload() {
        return payload;
    }

    public class Payload{
        private StatClose statistics_closed;

        public StatClose getStatistics_closed() {
            return statistics_closed;
        }

        public class StatClose {

            @SerializedName("90days")
            private List<Orders> orders;

            public List<Orders> getOrders() {
                return orders;
            }

            public class Orders {
                private int min_price;
                private float median;

                public int getMin_price() {
                    return min_price;
                }

                public float getMedian() {
                    return median;
                }
            }
        }
    }
}
