/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MineSkinResponse<T> {
    private final boolean success;
    private final int status;
    private final String message;
    private final String error;
    private final List<String> warnings;
    private final String server;
    private final String breadcrumb;
    private final JsonObject rawBody;
    private final T body;

    public MineSkinResponse(int status, Map<String, String> headers, JsonObject rawBody, Gson gson, Class<T> clazz) {
        this.success = rawBody.has("success") ? rawBody.get("success").getAsBoolean() : status == 200;
        this.status = status;
        this.message = rawBody.has("message") ? rawBody.get("message").getAsString() : null;
        this.error = rawBody.has("error") ? rawBody.get("error").getAsString() : null;
        this.warnings = rawBody.has("warnings") ? gson.fromJson(rawBody.get("warnings"), List.class) : Collections.emptyList();
        this.server = headers.get("x-mineskin-server");
        this.breadcrumb = headers.get("x-mineskin-breadcrumb");
        this.rawBody = rawBody;
        this.body = gson.fromJson((JsonElement)rawBody, clazz);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public int getStatus() {
        return this.status;
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(this.message);
    }

    public Optional<String> getError() {
        return Optional.ofNullable(this.error);
    }

    public String getMessageOrError() {
        return this.success ? this.message : this.error;
    }

    public List<String> getWarnings() {
        return this.warnings;
    }

    public String getServer() {
        return this.server;
    }

    public String getBreadcrumb() {
        return this.breadcrumb;
    }

    public T getBody() {
        return this.body;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{success=" + this.success + ", status=" + this.status + ", server='" + this.server + "', breadcrumb='" + this.breadcrumb + "'}\n" + String.valueOf(this.rawBody);
    }
}

