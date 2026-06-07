package com.terraforged.engine.util;

public class FastRandom {
    private long seed;

    public FastRandom() {
        this(0L);
    }

    public FastRandom(long seed) {
        this.seed = seed;
    }

    public void seed(long left, long right) {
        seed(left ^ Long.rotateLeft(right, 32));
    }

    public void seed(long seed) {
        this.seed = mix(seed);
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        return (int) Long.remainderUnsigned(nextLong(), bound);
    }

    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24F;
    }

    public long nextLong() {
        seed += 0x9E3779B97F4A7C15L;
        return mix(seed);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
