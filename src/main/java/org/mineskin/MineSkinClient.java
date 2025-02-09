/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.mineskin.ClientBuilder;
import org.mineskin.GenerateOptions;
import org.mineskin.response.GenerateResponse;
import org.mineskin.response.GetSkinResponse;

public interface MineSkinClient {
    public static ClientBuilder builder() {
        return ClientBuilder.create();
    }

    public long getNextRequest();

    public CompletableFuture<GetSkinResponse> getSkinByUuid(UUID var1);

    public CompletableFuture<GetSkinResponse> getSkinByUuid(String var1);

    public CompletableFuture<GenerateResponse> generateUrl(String var1);

    public CompletableFuture<GenerateResponse> generateUrl(String var1, GenerateOptions var2);

    public CompletableFuture<GenerateResponse> generateUpload(InputStream var1);

    public CompletableFuture<GenerateResponse> generateUpload(InputStream var1, GenerateOptions var2);

    public CompletableFuture<GenerateResponse> generateUpload(InputStream var1, String var2);

    public CompletableFuture<GenerateResponse> generateUpload(InputStream var1, GenerateOptions var2, String var3);

    public CompletableFuture<GenerateResponse> generateUpload(File var1) throws FileNotFoundException;

    public CompletableFuture<GenerateResponse> generateUpload(File var1, GenerateOptions var2) throws FileNotFoundException;

    public CompletableFuture<GenerateResponse> generateUpload(RenderedImage var1) throws IOException;

    public CompletableFuture<GenerateResponse> generateUpload(RenderedImage var1, GenerateOptions var2) throws IOException;

    public CompletableFuture<GenerateResponse> generateUser(UUID var1);

    public CompletableFuture<GenerateResponse> generateUser(UUID var1, GenerateOptions var2);
}

