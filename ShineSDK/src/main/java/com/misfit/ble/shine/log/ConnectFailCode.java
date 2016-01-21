package com.misfit.ble.shine.log;

/**
 * Created by bruyu on 8/12/15.
 * used to represent error code of Bluetooth Gatt connection and handshake
 */
public class ConnectFailCode {

    public enum ConnectPhase {
        IDLE,
        BLE_GATT_CONNECT,
        MISFIT_HANDSHAKE
    }

    public enum HandshakePhase {
        DISCOVER_SERVICES,
        SUBSCRIBE_CHARACTERISTIC_NOTIFICATION,
        GET_SERIAL_NUMBER,
        GET_MODEL_NAME,
        GET_FIRMWARE_VERSION,
        DONE
    }

    public final static int CONNECT_SUCCESS = 0;

    public final static int CONNECT_FAIL_GATT_TIMEOUT = 10; /* onConnectionStateChange callback is not called */
    public final static int CONNECT_FAIL_GATT_ERROR = 11;  /* Gatt Status 133 */
    public final static int CONNECT_FAIL_GATT_CONN_TERMINATE_PEER_USER = 12; /* 19 */
    public final static int CONNECT_FAIL_GATT_CONN_TERMINATE_LOCAL_USER = 13; /* 22 */
    public final static int CONNECT_FAIL_GATT_CONN_TIMEOUT = 14; /* 08 */
    public final static int CONNECT_FAIL_GATT_OTHERS = 15;  /* other non GATT_SUCCESS statuses */

    public final static int CONNECT_FAIL_HANDSHAKE_DISCOVER_SERVICES = 20;
    public final static int CONNECT_FAIL_HANDSHAKE_SUBSCRIBE_CHARACTERISTIC = 21;
    public final static int CONNECT_FAIL_HANDSHAKE_GET_SERIAL_NUMBER = 22;
    public final static int CONNECT_FAIL_HANDSHAKE_GET_MODEL_NAME = 23;
    public final static int CONNECT_FAIL_HANDSHAKE_GET_FIRMWARE_VERSION = 24;

    public final static int CONNECT_FAIL_UNKNOWN = 50;

    private ConnectPhase mConnectPhase;
    private int mConnectGattStatus;
    private HandshakePhase mHandshakePhase;

    public ConnectFailCode() {
        mConnectPhase = ConnectPhase.IDLE;
        mConnectGattStatus = -1;
        mHandshakePhase = HandshakePhase.DONE;
    }

    public void reset() {
        mConnectPhase = ConnectPhase.BLE_GATT_CONNECT;
        mConnectGattStatus = -1;
        mHandshakePhase = HandshakePhase.DONE;
    }

    public void setConnectPhase(ConnectPhase phase) {
        mConnectPhase = phase;
    }

    public ConnectPhase getConnectPhase() {
        return mConnectPhase;
    }

    public void setConnectGattStatus(int gattStatus) {
        mConnectGattStatus = gattStatus;
    }

    public int getConnectGattStatus() {
        return mConnectGattStatus;
    }

    public void setHandshakePhase(HandshakePhase phase) {
        mHandshakePhase = phase;
    }

    public HandshakePhase getHandshakePhase() {
        return mHandshakePhase;
    }

    public int sumConnectFailEnum() {
        if (mConnectPhase == ConnectPhase.MISFIT_HANDSHAKE) {
            return sumHandshakeFailEnum();
        } else if (mConnectPhase == ConnectPhase.BLE_GATT_CONNECT){
            return sumGattConnectFailEnum();
        } else {
            return CONNECT_SUCCESS;
        }
    }

    private int sumHandshakeFailEnum() {
        switch (mHandshakePhase) {
            case DONE:
                return CONNECT_SUCCESS;
            case DISCOVER_SERVICES:
                return CONNECT_FAIL_HANDSHAKE_DISCOVER_SERVICES;
            case SUBSCRIBE_CHARACTERISTIC_NOTIFICATION:
                return CONNECT_FAIL_HANDSHAKE_SUBSCRIBE_CHARACTERISTIC;
            case GET_SERIAL_NUMBER:
                return CONNECT_FAIL_HANDSHAKE_GET_SERIAL_NUMBER;
            case GET_MODEL_NAME:
                return CONNECT_FAIL_HANDSHAKE_GET_MODEL_NAME;
            case GET_FIRMWARE_VERSION:
                return CONNECT_FAIL_HANDSHAKE_GET_FIRMWARE_VERSION;
            default:
                return CONNECT_FAIL_UNKNOWN;
        }
    }

    private int sumGattConnectFailEnum() {
        switch(mConnectGattStatus){
            case 0:
                return CONNECT_SUCCESS;
            case 133:
                return CONNECT_FAIL_GATT_ERROR;
            case 19:
                return CONNECT_FAIL_GATT_CONN_TERMINATE_PEER_USER;
            case 22:
                return CONNECT_FAIL_GATT_CONN_TERMINATE_LOCAL_USER;
            case 8:
                return CONNECT_FAIL_GATT_CONN_TIMEOUT;
            case -1:
                return CONNECT_FAIL_GATT_TIMEOUT;
            default:
                return CONNECT_FAIL_GATT_OTHERS;
        }
    }
}
