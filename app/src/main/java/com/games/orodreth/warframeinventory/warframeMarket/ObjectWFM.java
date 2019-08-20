package com.games.orodreth.warframeinventory.warframeMarket;

import java.util.List;

public class ObjectWFM {

    private Payload payload;

    public Payload getPayload() {
        return payload;
    }

    public class Payload {
        private List<WFItems> items;

        public List<WFItems> getItems() {
            return items;
        }

        public class WFItems {

            private String url_name;
            private String id;
            private String item_name;
            private String thumb;

            public String getUrl_name() {
                return url_name;
            }

            public String getId() {
                return id;
            }

            public String getItem_name() {
                return item_name;
            }

            public String getThumb() {
                return thumb;
            }
        }
    }

}
