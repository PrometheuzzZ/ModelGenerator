/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.data;

import java.util.Objects;
import org.mineskin.data.Skin;
import org.mineskin.data.SkinData;

public class BaseSkin
implements Skin {
    private final String uuid;
    private final String name;
    private final SkinData data;
    private final long timestamp;
    private final int visibility;
    private final int views;

    public BaseSkin(String uuid, String name, SkinData data, long timestamp, int visibility, int views) {
        this.uuid = uuid;
        this.name = name;
        this.data = data;
        this.timestamp = timestamp;
        this.visibility = visibility;
        this.views = views;
    }

    @Override
    public String uuid() {
        return this.uuid;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public SkinData data() {
        return this.data;
    }

    @Override
    public long timestamp() {
        return this.timestamp;
    }

    @Override
    public int visibility() {
        return this.visibility;
    }

    @Override
    public int views() {
        return this.views;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BaseSkin that = (BaseSkin)obj;
        return Objects.equals(this.uuid, that.uuid) && Objects.equals(this.name, that.name) && Objects.equals((Object)this.data, (Object)that.data) && this.timestamp == that.timestamp && this.visibility == that.visibility && this.views == that.views;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.uuid, this.name, this.data, this.timestamp, this.visibility, this.views});
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[uuid=" + this.uuid + ", name=" + this.name + ", data=" + String.valueOf((Object)this.data) + ", timestamp=" + this.timestamp + ", visibility=" + this.visibility + ", views=" + this.views + "]";
    }
}

