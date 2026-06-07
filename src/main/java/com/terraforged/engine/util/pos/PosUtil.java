package com.terraforged.engine.util.pos;

public class PosUtil {
    public static long pack(int left, int right) {
        return ((long) left << 32) | (right & 0xFFFFFFFFL);
    }

    public static int unpackLeft(long value) {
        return (int) (value >> 32);
    }

    public static int unpackRight(long value) {
        return (int) value;
    }

    public static long packf(float left, float right) {
        return pack(Float.floatToRawIntBits(left), Float.floatToRawIntBits(right));
    }

    public static float unpackLeftf(long value) {
        return Float.intBitsToFloat(unpackLeft(value));
    }

    public static float unpackRightf(long value) {
        return Float.intBitsToFloat(unpackRight(value));
    }
}
