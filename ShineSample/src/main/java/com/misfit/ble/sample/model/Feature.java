package com.misfit.ble.sample.model;

import android.view.View;

import com.misfit.ble.sample.MisfitShineService;

public abstract class Feature {
    String name;

    public Feature(String name) {
        this.name = name;
    }

    public abstract void action(MisfitShineService shineService);
    public abstract View getParameterView();

    @Override
    public String toString() {
        return name;
    }
}
