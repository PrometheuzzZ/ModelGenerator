/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin;

import com.google.gson.Gson;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.mineskin.MineSkinClient;
import org.mineskin.MineSkinClientImpl;
import org.mineskin.request.RequestHandler;
import org.mineskin.request.RequestHandlerConstructor;

public class ClientBuilder {
    private String userAgent = "MineSkinClient";
    private String apiKey = null;
    private int timeout = 10000;
    private Gson gson = new Gson();
    private Executor getExecutor = null;
    private Executor generateExecutor = null;
    private RequestHandlerConstructor requestHandlerConstructor = null;

    private ClientBuilder() {
    }

    public static ClientBuilder create() {
        return new ClientBuilder();
    }

    public ClientBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public ClientBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public ClientBuilder timeout(int timeout2) {
        this.timeout = timeout2;
        return this;
    }

    public ClientBuilder gson(Gson gson) {
        this.gson = gson;
        return this;
    }

    public ClientBuilder getExecutor(Executor getExecutor) {
        this.getExecutor = getExecutor;
        return this;
    }

    public ClientBuilder generateExecutor(Executor generateExecutor) {
        this.generateExecutor = generateExecutor;
        return this;
    }

    public ClientBuilder requestHandler(RequestHandlerConstructor requestHandlerConstructor) {
        this.requestHandlerConstructor = requestHandlerConstructor;
        return this;
    }

    public MineSkinClient build() {
        if (this.requestHandlerConstructor == null) {
            throw new IllegalStateException("RequestHandlerConstructor is not set");
        }
        if ("MineSkinClient".equals(this.userAgent)) {
            MineSkinClientImpl.LOGGER.log(Level.WARNING, "Using default User-Agent: MineSkinClient - Please set a custom User-Agent");
        }
        if (this.apiKey == null) {
            MineSkinClientImpl.LOGGER.log(Level.WARNING, "Creating MineSkinClient without API key");
        }
        if (this.getExecutor == null) {
            this.getExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread thread2 = new Thread(r);
                thread2.setName("MineSkinClient/get");
                return thread2;
            });
        }
        if (this.generateExecutor == null) {
            this.generateExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread thread2 = new Thread(r);
                thread2.setName("MineSkinClient/generate");
                return thread2;
            });
        }
        RequestHandler requestHandler = this.requestHandlerConstructor.construct(this.userAgent, this.apiKey, this.timeout, this.gson);
        return new MineSkinClientImpl(requestHandler, this.generateExecutor, this.getExecutor);
    }
}

