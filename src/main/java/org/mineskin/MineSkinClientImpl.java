/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.mineskin.GenerateOptions;
import org.mineskin.MineSkinClient;
import org.mineskin.data.DelayInfo;
import org.mineskin.data.ExistingSkin;
import org.mineskin.data.GeneratedSkin;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.exception.MineskinException;
import org.mineskin.request.RequestHandler;
import org.mineskin.response.GenerateResponse;
import org.mineskin.response.GetSkinResponse;
import org.mineskin.response.MineSkinResponse;

public class MineSkinClientImpl
implements MineSkinClient {
    public static final Logger LOGGER = Logger.getLogger(MineSkinClientImpl.class.getName());
    private static final String API_BASE = "https://api.mineskin.org";
    private static final String GENERATE_BASE = "https://api.mineskin.org/generate";
    private static final String GET_BASE = "https://api.mineskin.org/get";
    private static final Map<String, AtomicLong> DELAYS = new ConcurrentHashMap<String, AtomicLong>();
    private static final Map<String, AtomicLong> NEXT_REQUESTS = new ConcurrentHashMap<String, AtomicLong>();
    private final Executor generateExecutor;
    private final Executor getExecutor;
    private final RequestHandler requestHandler;

    public MineSkinClientImpl(RequestHandler requestHandler, Executor generateExecutor, Executor getExecutor) {
        this.requestHandler = Preconditions.checkNotNull(requestHandler);
        this.generateExecutor = Preconditions.checkNotNull(generateExecutor);
        this.getExecutor = Preconditions.checkNotNull(getExecutor);
    }

    @Override
    public long getNextRequest() {
        String key = String.valueOf(this.requestHandler.getApiKey());
        return NEXT_REQUESTS.computeIfAbsent(key, k -> new AtomicLong(0L)).get();
    }

    @Override
    public CompletableFuture<GetSkinResponse> getSkinByUuid(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        return this.getSkinByUuid(uuid.toString());
    }

    @Override
    public CompletableFuture<GetSkinResponse> getSkinByUuid(String uuid) {
        Preconditions.checkNotNull(uuid);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.requestHandler.getJson("https://api.mineskin.org/get/uuid/" + uuid, ExistingSkin.class, GetSkinResponse::new);
            } catch (IOException e) {
                throw new MineskinException(e);
            }
        }, this.getExecutor);
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUrl(String url) {
        Preconditions.checkNotNull(url);
        return this.generateUrl(url, GenerateOptions.create());
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUrl(String url, GenerateOptions options) {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(options);
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.delayUntilNext();
                JsonObject body = options.toJson();
                body.addProperty("url", url);
                GenerateResponse res = this.requestHandler.postJson("https://api.mineskin.org/generate/url", body, GeneratedSkin.class, GenerateResponse::new);
                this.handleResponse(res);
                return res;
            } catch (IOException e) {
                throw new MineskinException(e);
            } catch (MineSkinRequestException e) {
                this.handleResponse(e.getResponse());
                throw e;
            }
        }, this.generateExecutor);
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(InputStream is) {
        return this.generateUpload(is, GenerateOptions.create(), null);
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(InputStream is, GenerateOptions options) {
        Preconditions.checkNotNull(options);
        return this.generateUpload(is, options, options.getName() + ".png");
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(InputStream is, String fileName) {
        return this.generateUpload(is, GenerateOptions.create(), fileName);
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(InputStream is, GenerateOptions options, String fileName) {
        Preconditions.checkNotNull(is);
        Preconditions.checkNotNull(options);
        Preconditions.checkNotNull(fileName);
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.delayUntilNext();
                Map<String, String> data = options.toMap();
                GenerateResponse res = this.requestHandler.postFormDataFile("https://api.mineskin.org/generate/upload", "file", fileName, is, data, GeneratedSkin.class, GenerateResponse::new);
                this.handleResponse(res);
                return res;
            } catch (IOException e) {
                throw new MineskinException(e);
            } catch (MineSkinRequestException e) {
                this.handleResponse(e.getResponse());
                throw e;
            }
        }, this.generateExecutor);
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(File file) throws FileNotFoundException {
        return this.generateUpload(file, GenerateOptions.create());
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(File file, GenerateOptions options) throws FileNotFoundException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(options);
        return this.generateUpload(new FileInputStream(file), options, file.getName());
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(RenderedImage image) throws IOException {
        return this.generateUpload(image, GenerateOptions.create());
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUpload(RenderedImage image, GenerateOptions options) throws IOException {
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return this.generateUpload((InputStream)new ByteArrayInputStream(baos.toByteArray()), options);
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUser(UUID uuid) {
        return this.generateUser(uuid, GenerateOptions.create());
    }

    @Override
    public CompletableFuture<GenerateResponse> generateUser(UUID uuid, GenerateOptions options) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkNotNull(options);
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.delayUntilNext();
                JsonObject body = options.toJson();
                body.addProperty("uuid", uuid.toString());
                GenerateResponse res = this.requestHandler.postJson("https://api.mineskin.org/generate/user", body, GeneratedSkin.class, GenerateResponse::new);
                this.handleResponse(res);
                return res;
            } catch (IOException e) {
                throw new MineskinException(e);
            } catch (MineSkinRequestException e) {
                this.handleResponse(e.getResponse());
                throw e;
            }
        }, this.generateExecutor);
    }

    private void handleResponse(MineSkinResponse<?> response) {
        if (response instanceof GenerateResponse) {
            GenerateResponse generateResponse = (GenerateResponse)response;
            this.handleDelayInfo(generateResponse.getDelayInfo());
        }
    }

    private void delayUntilNext() {
        if (System.currentTimeMillis() < this.getNextRequest()) {
            long delay = this.getNextRequest() - System.currentTimeMillis();
            try {
                LOGGER.finer("Waiting for " + delay + "ms until next request");
                Thread.sleep(delay + 1L);
            } catch (InterruptedException e) {
                throw new MineskinException("Interrupted while waiting for next request", e);
            }
        }
    }

    private void handleDelayInfo(DelayInfo delayInfo) {
        if (delayInfo == null) {
            return;
        }
        String key = String.valueOf(this.requestHandler.getApiKey());
        AtomicLong delay = DELAYS.compute(key, (k, v) -> {
            if (v == null) {
                v = new AtomicLong(0L);
            }
            if ((long)delayInfo.millis() > v.get()) {
                v.set(delayInfo.millis());
            }
            return v;
        });
        LOGGER.finer("Delaying next request by " + delay.get() + "ms");
        NEXT_REQUESTS.compute(key, (k, v) -> {
            long next;
            if (v == null) {
                v = new AtomicLong(System.currentTimeMillis());
            }
            if ((next = System.currentTimeMillis() + delay.get() + 1L) > v.get()) {
                v.set(next);
            }
            return v;
        });
    }
}

