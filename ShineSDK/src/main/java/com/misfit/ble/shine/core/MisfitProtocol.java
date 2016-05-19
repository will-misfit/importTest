package com.misfit.ble.shine.core;

public class MisfitProtocol {

    public final class Operation {
        public static final byte GET = 1;
        public static final byte SET = 2;
        public static final byte RESPONSE = 3;
    }

    public final class Algorithm {
        public final static byte PARAMETER_ID = 0x03;
        public final static byte COMMAND_ACTIVIT_TYPE = 0x06;
    }
}
