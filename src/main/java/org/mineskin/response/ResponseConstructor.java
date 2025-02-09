/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import org.mineskin.response.MineSkinResponse;

public interface ResponseConstructor<T, R extends MineSkinResponse<T>> {
    public R construct(int var1, Map<String, String> var2, JsonObject var3, Gson var4, Class<T> var5);
}

