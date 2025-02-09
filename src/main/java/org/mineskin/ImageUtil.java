/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.mineskin;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

public class ImageUtil {
    public static BufferedImage randomImage(int width, int height) {
        Random random = new Random();
        BufferedImage image = new BufferedImage(width, height, 1);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                float r = random.nextFloat();
                float g2 = random.nextFloat();
                float b = random.nextFloat();
                image.setRGB(x, y, new Color(r, g2, b).getRGB());
            }
        }
        return image;
    }
}

