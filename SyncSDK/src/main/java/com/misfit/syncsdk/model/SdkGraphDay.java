package com.misfit.syncsdk.model;

import com.google.gson.reflect.TypeToken;
import com.misfit.syncsdk.utils.CheckUtils;
import com.misfit.syncsdk.utils.GsonUtils;
import com.misfit.syncsdk.utils.VolleyRequestUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * similar to prometheus.model.GraphDay
 */
public class SdkGraphDay {
    private String date;

    private int timezoneOffset;

    private List<SdkGraphItem> items;

    private long lastSessionEndTime;

    private String itemsJson;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(int timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public List<SdkGraphItem> getItems() {
        return items;
    }

    public void setItems(List<SdkGraphItem> items) {
        this.items = items;
    }

    public long getLastSessionEndTime() {
        return lastSessionEndTime;
    }

    public void setLastSessionEndTime(long lastSessionEndTime) {
        this.lastSessionEndTime = lastSessionEndTime;
    }

    public static SdkGraphDay createEmptyInstance() {
        SdkGraphDay graphDay = new SdkGraphDay();
        graphDay.setItems(new ArrayList<SdkGraphItem>());
        return graphDay;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }

    public void buildObj() {
        if (!CheckUtils.isStringEmpty(this.itemsJson)) {
            Type listType = new TypeToken<ArrayList<SdkGraphItem>>() {}.getType();
            this.items = GsonUtils.getInstance().getGson().fromJson(this.itemsJson, listType);
        } else {
            this.items = new ArrayList<SdkGraphItem>();
        }
    }

    public void buildJson() {
        if (this.items != null) {
            this.itemsJson = GsonUtils.getInstance().getGson().toJson(this.items);
        } else {
            this.itemsJson = "";
        }
    }
}
