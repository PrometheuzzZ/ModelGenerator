/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.mineskin.response.MineSkinResponse;
import org.mineskin.response.ResponseConstructor;

public abstract class RequestHandler {
    protected final Gson gson;
    protected final String userAgent;
    protected final String apiKey;

    public RequestHandler(String userAgent, String apiKey, int timeout2, Gson gson) {
        this.userAgent = userAgent;
        this.apiKey = apiKey;
        this.gson = gson;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public abstract <T, R extends MineSkinResponse<T>> R getJson(String var1, Class<T> var2, ResponseConstructor<T, R> var3) throws IOException;

    public abstract <T, R extends MineSkinResponse<T>> R postJson(String var1, JsonObject var2, Class<T> var3, ResponseConstructor<T, R> var4) throws IOException;

    public abstract <T, R extends MineSkinResponse<T>> R postFormDataFile(String var1, String var2, String var3, InputStream var4, Map<String, String> var5, Class<T> var6, ResponseConstructor<T, R> var7) throws IOException;
}

