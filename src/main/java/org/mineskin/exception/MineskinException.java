/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.exception;

public class MineskinException
extends RuntimeException {
    public MineskinException() {
    }

    public MineskinException(String message) {
        super(message);
    }

    public MineskinException(String message, Throwable cause) {
        super(message, cause);
    }

    public MineskinException(Throwable cause) {
        super(cause);
    }

    public MineskinException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

