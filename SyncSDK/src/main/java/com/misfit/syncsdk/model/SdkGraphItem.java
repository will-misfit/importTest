package com.misfit.syncsdk.model;

/**
 * model class following prometheus.api.request.GraphItem
 */
public class SdkGraphItem {

    private static final long serialVersionUID = 6037777389903773621L;

    private double value;

    // timestamp in second
    private long startTime;

    // timestamp in second
    private long endTime;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
