/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.data;

import org.mineskin.data.BaseSkin;
import org.mineskin.data.SkinData;

public class GeneratedSkin
extends BaseSkin {
    private final boolean duplicate;

    public GeneratedSkin(String uuid, String name, SkinData data, long timestamp, int visibility, int views, boolean duplicate) {
        super(uuid, name, data, timestamp, visibility, views);
        this.duplicate = duplicate;
    }

    public boolean duplicate() {
        return this.duplicate;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[uuid=" + this.uuid() + ", name=" + this.name() + ", data=" + String.valueOf((Object)this.data()) + ", timestamp=" + this.timestamp() + ", visibility=" + this.visibility() + ", views=" + this.views() + ", duplicate=" + this.duplicate() + "]";
    }
}

