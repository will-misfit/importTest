package com.misfit.ble.setting.pluto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Quoc-Hung Le on 8/27/15.
 */
public class PlutoSequence {

    public static class LED {
        public static final short DEFAULT_SEQUENCE = 0;
        public final static byte SPECIFIED_SHORT = 0x00;
        public final static byte SPECIFIED_LONG = 0x01;

        private short mValue;

        public short getValue() {
            return mValue;
        }

        public LED(short mValue) {
            this.mValue = mValue;
        }

        @Override
        public String toString() {
            JSONObject valueJSON = new JSONObject();
            try {
                valueJSON.put("value", mValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return valueJSON.toString();
        }
    }

    public static class Vibe {
        public static final short DEFAULT_SEQUENCE = 3;
        public static final short SPECIFIED_SHORT = 0;
        public static final short SPECIFIED_LONG = 1;

        private short mValue;

        public short getValue() {
            return mValue;
        }

        public Vibe(short mValue) {
            this.mValue = mValue;
        }

        @Override
        public String toString() {
            JSONObject valueJSON = new JSONObject();
            try {
                valueJSON.put("value", mValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return valueJSON.toString();
        }
    }

    public static class Sound {
        public static final short DEFAULT_SEQUENCE = 1;

        private short mValue;

        public short getValue() {
            return mValue;
        }

        public Sound(short mValue) {
            this.mValue = mValue;
        }

        @Override
        public String toString() {
            JSONObject valueJSON = new JSONObject();
            try {
                valueJSON.put("value", mValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return valueJSON.toString();
        }
    }

    public static class Color {

        public final static byte SPECIFIED_BLUE = 0x00;
        public final static byte SPECIFIED_YELLOW = 0x01;
        public final static byte SPECIFIED_ORANGE = 0x02;
        public final static byte SPECIFIED_PURPLE = 0x03;
        public final static byte SPECIFIED_GREEN = 0x04;
        public final static byte SPECIFIED_PINK = 0x05;
        
        private short mValue;

        public short getValue() {
            return mValue;
        }

        public Color(short mValue) {
            this.mValue = mValue;
        }

        @Override
        public String toString() {
            JSONObject valueJSON = new JSONObject();
            try {
                valueJSON.put("value", mValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return valueJSON.toString();
        }
    }
}
