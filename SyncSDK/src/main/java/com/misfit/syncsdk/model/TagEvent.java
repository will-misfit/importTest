package com.misfit.syncsdk.model;

import java.util.Locale;

public class TagEvent {
    public static final int TAG_IN = 1;
    public static final int TAG_OUT = 2;

    private int id;

    private long taggedTimestamp;

    private int tagType;

    private boolean isFixed;

    public TagEvent() {
    }

    public TagEvent(long taggedTime, int type) {
        taggedTimestamp = taggedTime;
        tagType = type;
        isFixed = false;
    }

    public long getTaggedTimestamp() {
        return taggedTimestamp;
    }

    public void setTaggedTimestamp(long taggedTimestamp) {
        this.taggedTimestamp = taggedTimestamp;
    }

    public int getTagType() {
        return tagType;
    }

    public void setTagType(int tagType) {
        this.tagType = tagType;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "[%d,%d]", taggedTimestamp, tagType);
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean isFixed) {
        this.isFixed = isFixed;
    }
}
