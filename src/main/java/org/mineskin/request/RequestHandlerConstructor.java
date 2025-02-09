/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin.request;

import com.google.gson.Gson;
import org.mineskin.request.RequestHandler;

public interface RequestHandlerConstructor {
    public RequestHandler construct(String var1, String var2, int var3, Gson var4);
}

