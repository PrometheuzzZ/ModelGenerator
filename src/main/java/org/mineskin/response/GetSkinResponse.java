/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import org.mineskin.data.ExistingSkin;
import org.mineskin.data.Skin;
import org.mineskin.response.MineSkinResponse;

public class GetSkinResponse
extends MineSkinResponse<ExistingSkin> {
    public GetSkinResponse(int status, Map<String, String> headers, JsonObject rawBody, Gson gson, Class<ExistingSkin> clazz) {
        super(status, headers, rawBody, gson, clazz);
    }

    public Skin getSkin() {
        return (Skin)this.getBody();
    }
}

