package com.misfit.ble.sample.model;

import com.misfit.ble.setting.pluto.PlutoSequence;

public class SpecifiedAnimationSetting {
    public PlutoSequence.LED led;
    public PlutoSequence.Color color;
    public byte repeats;
    public short timeBetweenRepeats;
}
