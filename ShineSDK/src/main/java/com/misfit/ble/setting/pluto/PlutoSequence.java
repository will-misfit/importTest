package com.misfit.ble.setting.pluto;

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
	}
}
