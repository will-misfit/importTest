package com.misfit.ble.setting.pluto;

public class SpecifiedAnimationSetting {
    public final static byte SEQUENCE_BLINK_SHORT = 0x00;
    public final static byte SEQUENCE_BLINK_LONG = 0x01;
    public final static byte REPEAT_NOT_PLAY = 0x00;
    public final static byte COLOR_BLUE = 0x00;
    public final static byte COLOR_YELLOW = 0x01;
    public final static byte COLOR_ORANGE = 0x02;
    public final static byte COLOR_PURPLE = 0x03;
    public final static byte COLOR_GREEN = 0x04;
    public final static byte COLOR_PINK = 0x05;

    private PlutoSequence.LED mSequence;
    private byte mNumberOfRepeats;
    private short mTimeBetweenRepeats;
    private byte mColor;

    /**
     * Initialize {@link SpecifiedAnimationSetting}
     *
     * @param sequence           can use {@link SpecifiedAnimationSetting#SEQUENCE_BLINK_SHORT} or {@link SpecifiedAnimationSetting#SEQUENCE_BLINK_LONG}
     * @param numberOfRepeats    {@value REPEAT_NOT_PLAY} for not play, 1-20 for number of blinks
     * @param timeBetweenRepeats the time between every repeats in milliseconds, NOT USED YET
     * @param color              the color that the animation will show. It could be {@link SpecifiedAnimationSetting#COLOR_BLUE}/{@link SpecifiedAnimationSetting#COLOR_GREEN} or other color
     */
    public SpecifiedAnimationSetting(PlutoSequence.LED sequence, byte numberOfRepeats, short timeBetweenRepeats, byte color) {
        mSequence = sequence;
        mNumberOfRepeats = numberOfRepeats;
        mTimeBetweenRepeats = timeBetweenRepeats;
        mColor = color;
    }

    public PlutoSequence.LED getSequence() {
        return mSequence;
    }

    public byte getNumberOfRepeats() {
        return mNumberOfRepeats;
    }

    public short getTimeBetweenRepeats() {
        return mTimeBetweenRepeats;
    }

    public byte getColor() {
        return mColor;
    }

    public void setSequence(PlutoSequence.LED sequence) {
        mSequence = sequence;
    }

    public void setNumberOfRepeats(byte numberOfRepeats) {
        mNumberOfRepeats = numberOfRepeats;
    }

    public void setTimeBetweenRepeats(short timeBetweenRepeats) {
        mTimeBetweenRepeats = timeBetweenRepeats;
    }

    public void setColor(byte color) {
        mColor = color;
    }
}
