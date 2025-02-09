/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.data;

public enum Visibility {
    PUBLIC(0),
    UNLISTED(1);

    private final int code;

    private Visibility(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}

