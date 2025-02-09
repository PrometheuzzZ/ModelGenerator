/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.data;

import org.mineskin.data.SkinData;

public interface Skin {
    public String uuid();

    public String name();

    public SkinData data();

    public long timestamp();

    public int visibility();

    public int views();
}

