/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.mineskin.MineSkinClientImpl;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.exception.MineskinException;
import org.mineskin.request.RequestHandler;
import org.mineskin.response.MineSkinResponse;
import org.mineskin.response.ResponseConstructor;

public class JsoupRequestHandler
extends RequestHandler {
    private final String userAgent;
    private final String apiKey;
    private final int timeout;

    public JsoupRequestHandler(String userAgent, String apiKey, int timeout2, Gson gson) {
        super(userAgent, apiKey, timeout2, gson);
        this.userAgent = userAgent;
        this.apiKey = apiKey;
        this.timeout = timeout2;
    }

    private Connection requestBase(Connection.Method method, String url) {
        MineSkinClientImpl.LOGGER.log(Level.FINE, String.valueOf((Object)method) + " " + url);
        Connection connection = Jsoup.connect(url).method(method).userAgent(this.userAgent).ignoreContentType(true).ignoreHttpErrors(true).timeout(this.timeout);
        if (this.apiKey != null) {
            connection.header("Authorization", "Bearer " + this.apiKey);
        }
        return connection;
    }

    private <T, R extends MineSkinResponse<T>> R wrapResponse(Connection.Response response, Class<T> clazz, ResponseConstructor<T, R> constructor) {
        try {
            JsonObject jsonBody = this.gson.fromJson(response.body(), JsonObject.class);
            R wrapped = constructor.construct(response.statusCode(), this.lowercaseHeaders(response.headers()), jsonBody, this.gson, clazz);
            if (!((MineSkinResponse)wrapped).isSuccess()) {
               // throw new MineSkinRequestException(((MineSkinResponse)wrapped).getError().orElse("Request Failed"), (MineSkinResponse<?>)wrapped);
            }
            return wrapped;
        } catch (JsonParseException e) {
            MineSkinClientImpl.LOGGER.log(Level.WARNING, "Failed to parse response body: " + response.body(), e);
            throw new MineskinException("Failed to parse response", e);
        }
    }

    private Map<String, String> lowercaseHeaders(Map<String, String> headers) {
        return headers.entrySet().stream().collect(Collectors.toMap(e -> ((String)e.getKey()).toLowerCase(), Map.Entry::getValue));
    }

    @Override
    public <T, R extends MineSkinResponse<T>> R getJson(String url, Class<T> clazz, ResponseConstructor<T, R> constructor) throws IOException {
        Connection.Response response = this.requestBase(Connection.Method.GET, url).execute();
        return this.wrapResponse(response, clazz, constructor);
    }

    @Override
    public <T, R extends MineSkinResponse<T>> R postJson(String url, JsonObject data, Class<T> clazz, ResponseConstructor<T, R> constructor) throws IOException {
        Connection.Response response = this.requestBase(Connection.Method.POST, url).requestBody(data.toString()).header("Content-Type", "application/json").execute();
        return this.wrapResponse(response, clazz, constructor);
    }

    @Override
    public <T, R extends MineSkinResponse<T>> R postFormDataFile(String url, String key, String filename, InputStream in, Map<String, String> data, Class<T> clazz, ResponseConstructor<T, R> constructor) throws IOException {
        Connection connection = this.requestBase(Connection.Method.POST, url).header("Content-Type", "multipart/form-data");
        connection.data(key, filename, in);
        connection.data(data);
        Connection.Response response = connection.execute();
        return this.wrapResponse(response, clazz, constructor);
    }
}

