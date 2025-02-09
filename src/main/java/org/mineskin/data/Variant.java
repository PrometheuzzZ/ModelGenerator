/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.data;

public enum Variant {
    AUTO(""),
    CLASSIC("classic"),
    SLIM("slim");

    private final String name;

    private Variant(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

