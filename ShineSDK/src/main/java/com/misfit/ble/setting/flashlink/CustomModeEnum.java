package com.misfit.ble.setting.flashlink;

/**
 * Created by Quoc-Hung Le on 9/14/15.
 */
public class CustomModeEnum {

	/**
	 * Keyboard or Media?
	 */
	public enum ActionType {
		HID_KEYBOARD(0x50),
		HID_MEDIA(0x51);

		private final byte id;

		ActionType(int id) {
			this.id = (byte) id;
		}

		public byte getId() {
			return this.id;
		}
	}

	/**
	 * Event
	 */
	public enum MemEventNumber {
		// Flash
		SINGLE_CLICK(0x13),
		DOUBLE_CLICK(0x14),
		TRIPLE_CLICK(0x15),
		LONG_PRESS(0x0c),
		DOUBLE_PRESS_N_HOLD(0x16),

		// Pluto
		PLUTO_TRIPLE_TAP(0x0c);

		private final byte id;

		MemEventNumber(int id) {
			this.id = (byte) id;
		}

		public byte getId() {
			return this.id;
		}
	}

	/**
	 * Animation on flash when action
	 */
	public enum AnimNumber {
		NO_ANIMATION(0x00),
		SUCCESS(0x0b),
		ERROR(0x0c),
		SINGLE_CLICK_SUCCEEDED(0x14),
		DOUBLE_CLICK_SUCCEEDED(0x0f),
		TRIPLE_PRESS_SUCCEEDED(0x10),
		LONG_PRESS_SUCCEEDED(0x15),
		DOUBLE_PRESS_N_HOLD_SUCCEEDED(0x16),
		SINGLE_CLICK_RECEIVED(0x11),
		DOUBLE_CLICK_RECEIVED(0x0d),
		TRIPLE_CLICK_RECEIVED(0x0e),
		LONG_PRESS_RECEIVED(0x12),
		DOUBLE_PRESS_N_HOLD_RECEIVED(0x13);

		private final byte id;

		AnimNumber(int id) {
			this.id = (byte) id;
		}

		public byte getId() {
			return this.id;
		}
	}

	/**
	 * Keycode: for Keyboard type and Media type
	 */
	public enum KeyCode {
		KB_KEY_RIGHT(0x4f),
		KB_KEY_LEFT(0x50),
		KB_B(0x05),
		KB_W(0x1A),

		MEDIA_PLAY_PAUSE(0xCD),
		MEDIA_VOLUME_UP_OR_SELFIE(0xE9),
		MEDIA_VOLUME_DOWN(0xEA),
		MEDIA_NEXT_SONG(0xB5),
		MEDIA_PREVIOUS_SONG(0xB6);

		private final int id;

		KeyCode(int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}
	}
}
