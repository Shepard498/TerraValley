package com.terraforged.engine;

public class Seed {
    private final int root;
    private int value;

    public Seed(int value) {
        this.root = value;
        this.value = value;
    }

    public Seed(long value) {
        this((int) value);
    }

    public int next() {
        return value++;
    }

    public int get() {
        return value;
    }

    public int root() {
        return root;
    }

    public Seed split() {
        return new Seed(root);
    }

    public Seed offset(int offset) {
        return new Seed(root + offset);
    }
}
