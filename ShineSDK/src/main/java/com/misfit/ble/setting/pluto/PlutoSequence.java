package com.misfit.ble.setting.pluto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Quoc-Hung Le on 8/27/15.
 */
public class PlutoSequence {

    public static class LED {
        public static final short DEFAULT_SEQUENCE = 0;

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
}
