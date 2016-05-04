package com.misfit.ble.sample.utils;

public interface Preference<T> {
    void setTitle(String title);

    void setValue(T value);

    T getValue();
}
