/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.exception;

import org.mineskin.response.MineSkinResponse;

public class MineSkinRequestException
extends RuntimeException {
    private final MineSkinResponse<?> response;

    public MineSkinRequestException(String message, MineSkinResponse<?> response) {
        super(message);
        this.response = response;
    }

    public MineSkinRequestException(String message, MineSkinResponse<?> response, Throwable cause) {
        super(message, cause);
        this.response = response;
    }

    public MineSkinResponse<?> getResponse() {
        return this.response;
    }
}

