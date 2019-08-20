package com.games.orodreth.warframeinventory.warframeMarket;

import android.content.ClipData;

import java.util.List;

public class DucatsWFM {

    private Payload payload;

    public Payload getPayload() {
        return payload;
    }

    public class Payload {

        private Item item;

        public Item getItem() {
            return item;
        }

        public class Item {
            private String id;
            private List<ItemInSet> items_in_set;

            public String getId() {
                return id;
            }

            public List<ItemInSet> getItems_in_set() {
                return items_in_set;
            }

            public class ItemInSet {
                private String id;
                private int ducats;

                public String getId() {
                    return id;
                }

                public int getDucats() {
                    return ducats;
                }
            }
        }
    }
}
