package com.misfit.syncsdk.reproduce;

import com.misfit.ble.util.Convertor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetFileResponse {
    int result;
    int status;
    int handle;
    int fileHandle;
    int fileFormat;
    long fileLength;
    long fileTimestamp;
    int fileMilliseconds;
    short fileTimezoneOffsetInMinutes;
    long fileCRC;
    String activityData;

    public byte[] getRawData() {
        StringBuilder rawBuilder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putShort(Convertor.unsignedShortFromInteger(fileHandle));
        byteBuffer.putShort(Convertor.unsignedShortFromInteger(fileFormat));
        byteBuffer.putInt(Convertor.unsignedIntFromLong(fileLength));
        byteBuffer.putInt(Convertor.unsignedIntFromLong(fileTimestamp));
        byteBuffer.putShort(Convertor.unsignedShortFromInteger(fileMilliseconds));
        byteBuffer.putShort(fileTimezoneOffsetInMinutes);

        rawBuilder.append(Convertor.bytesToString(byteBuffer.array()));
        rawBuilder.append(activityData);

        byteBuffer = ByteBuffer.wrap(new byte[4]);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(Convertor.unsignedIntFromLong(fileCRC));
        rawBuilder.append(Convertor.bytesToString(byteBuffer.array()));

        return Convertor.bytesFromString(rawBuilder.toString());
    }

    private String getByteString(short val) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (val >> 8);
        bytes[0] = (byte) (val & 0x00ff);
        return Convertor.bytesToString(bytes);
    }

    private String getByteString(int val) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (val >> 24);
        bytes[2] = (byte) ((val >> 16) & 0xff);
        bytes[1] = (byte) ((val >> 12) & 0xff);
        bytes[0] = (byte) ((val >> 8) & 0xff);
        return Convertor.bytesToString(bytes);
    }
}
