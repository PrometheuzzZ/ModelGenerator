/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.mineskin.data.Variant;
import org.mineskin.data.Visibility;

public class GenerateOptions {
    private String name;
    private Variant variant;
    private Visibility visibility;

    private GenerateOptions() {
    }

    public GenerateOptions name(String name) {
        this.name = name;
        return this;
    }

    public static GenerateOptions create() {
        return new GenerateOptions();
    }

    public GenerateOptions variant(Variant variant) {
        this.variant = variant;
        return this;
    }

    public GenerateOptions visibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    protected JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (!Strings.isNullOrEmpty(this.name)) {
            json.addProperty("name", this.name);
        }
        if (this.variant != null && this.variant != Variant.AUTO) {
            json.addProperty("variant", this.variant.getName());
        }
        if (this.visibility != null) {
            json.addProperty("visibility", this.visibility.getCode());
        }
        return json;
    }

    protected Map<String, String> toMap() {
        HashMap<String, String> data = new HashMap<String, String>();
        this.addTo(data);
        return data;
    }

    protected void addTo(Map<String, String> data) {
        if (!Strings.isNullOrEmpty(this.name)) {
            data.put("name", this.name);
        }
        if (this.variant != null && this.variant != Variant.AUTO) {
            data.put("variant", this.variant.getName());
        }
        if (this.visibility != null) {
            data.put("visibility", String.valueOf(this.visibility.getCode()));
        }
    }

    public String getName() {
        return this.name;
    }
}

