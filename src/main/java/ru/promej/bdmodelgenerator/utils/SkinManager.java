package ru.promej.bdmodelgenerator.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;

import org.mineskin.MineSkinClient;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.response.MineSkinResponse;

import javax.imageio.ImageIO;

public class SkinManager {

    public static void main(String[] args) {
        BufferedImage skin = Utils.getSkin("https://s.namemc.com/i/595c66c5641810b8.png");

        BufferedImage fullBody = skinToStatue(skin);

        saveImage(fullBody, "test");
    }

    public static String bufferedImageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream); // PNG по умолчанию
            byte[] imageBytes = outputStream.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage skinToStatue(BufferedImage skin){

        skin = mergeOverlay(skin);

        BufferedImage overlay_three = loadImageFromResources("image/statue/overlay_3.png");



        BufferedImage overlay_one = loadImageFromResources("image/statue/overlay_1.png");

        skin = applyOverlay(skin, overlay_one);

        skin = stoneifySkin(skin, true);

        skin = applyOverlay(skin, overlay_three);

        BufferedImage overlay_two = loadImageFromResources("image/statue/overlay_2.png");

        skin = applyOverlay(skin, overlay_two);

        return skin;

    }

    public static BufferedImage mergeOverlay(BufferedImage skin){


        BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        /*--------------------------------------------------
         * 1) Копируем весь исходник
         *-------------------------------------------------*/
        g.setComposite(AlphaComposite.Src);     // полная замена
        g.drawImage(skin, 0, 0, null);

        /*--------------------------------------------------
         * 2) Переносим элементы Layer 2 поверх Layer 1
         *-------------------------------------------------*/
        g.setComposite(AlphaComposite.SrcOver); // обычное наложение

        // Hat
        g.drawImage(skin, 0, 0, 32, 16, 32, 0, 64, 16, null);

        // Jacket
        g.drawImage(skin, 16, 16, 40, 32, 16, 32, 40, 48, null);

        // Right arm (outer)
        g.drawImage(skin, 40, 16, 56, 32, 40, 32, 56, 48, null);

        // Left arm (outer)
        g.drawImage(skin, 32, 48, 48, 64, 48, 48, 64, 64, null);

        // Right leg (outer)
        g.drawImage(skin, 0, 16, 16, 32, 0, 32, 16, 48, null);

        // Left leg (outer)
        g.drawImage(skin, 16, 48, 32, 64, 0, 48, 16, 64, null);

        /*--------------------------------------------------
         * 3) Зачищаем «старые» прямоугольники Layer 2
         *-------------------------------------------------*/
        g.setComposite(AlphaComposite.Clear);

        // Hat
        g.fillRect(32, 0, 32, 16);

        // Jacket
        g.fillRect(16, 32, 24, 16);

        // Right arm (outer)
        g.fillRect(40, 32, 16, 16);

        // Left arm (outer)
        g.fillRect(48, 48, 16, 16);

        // Right leg (outer)
        g.fillRect(0, 32, 16, 16);

        // Left leg (outer)
        g.fillRect(0, 48, 16, 16);

        g.dispose();
        return out;


    }

    public static BufferedImage applyOverlay(BufferedImage base, BufferedImage overlay) {

        int w = base.getWidth(), h = base.getHeight();
        if (w != overlay.getWidth() || h != overlay.getHeight()) {
            throw new IllegalArgumentException("Размеры base и overlay не совпадают!");
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();

        // копируем базу
        g.setComposite(AlphaComposite.Src);
        g.drawImage(base, 0, 0, null);

        // накладываем оверлей (прозрачность overlay сохранится)
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(overlay, 0, 0, null);

        g.dispose();
        return out;
    }

    public static BufferedImage stoneifySkin(BufferedImage skin, boolean addMoss) {
        int w = skin.getWidth();
        int h = skin.getHeight();

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        final float STONE_HUE   = 0.58f;   // ⇽ сдвиг в холодный сектор
        final float TARGET_SAT  = 0.03f;   // почти нет цвета
        final float MIN_V       = 0.2f;
        final float MAX_V       = 0.60f;
        final int   NOISE_RANGE = 6;

        final Color[] MOSS_SET = {
                new Color(111,122, 82),        // #6f7a52
                new Color( 85,105, 64)         // #556940
        };

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                int argb  = skin.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                if (alpha == 0) {                       // прозрачные — как есть
                    out.setRGB(x, y, argb);
                    continue;
                }

                /*--------------------------------------------------
                 * 1. Grayscale (новый шаг)
                 *-------------------------------------------------*/
                int r = (argb >> 16) & 0xff;
                int g = (argb >>  8) & 0xff;
                int b =  argb        & 0xff;

                // перцептивная яркость (sRGB)
                int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
                r = g = b = gray;                       // NEW: чистый Ч/Б

                /*--------------------------------------------------
                 * 2. Каменный тон + контраст
                 *-------------------------------------------------*/
                float[] hsb = Color.RGBtoHSB(r, g, b, null);

                // яркость в «каменный» диапазон
                float v = MIN_V + (MAX_V - MIN_V) * hsb[2];

                // шум ±NOISE_RANGE
                v += (rnd.nextInt(NOISE_RANGE * 2 + 1) - NOISE_RANGE) / 255f;
                v = Math.max(0f, Math.min(1f, v));

                int stoneRGB = Color.HSBtoRGB(
                        STONE_HUE,        // оттенок после Ч/Б не важен
                        TARGET_SAT,    // почти серый
                        v);

                /*--------------------------------------------------
                 * 3. Мох (опционально)
                 *-------------------------------------------------*/
                if (addMoss && rnd.nextFloat() < 0.03f) {
                    Color moss = MOSS_SET[rnd.nextInt(MOSS_SET.length)];
                    stoneRGB = moss.getRGB();
                }

                // вернуть альфу
                stoneRGB = (alpha << 24) | (stoneRGB & 0x00ffffff);
                out.setRGB(x, y, stoneRGB);
            }
        }
        return out;
    }

    public static BufferedImage createFullBodyAvatar(BufferedImage skin) {

        if(Utils.validSkinSize(skin)){
            boolean bl = skin.getRGB(54, 20) == 0 && skin.getRGB(50, 16) == 0;
            if(bl){
                skin = fixSlim(skin);

            }
        } else {
            skin = fixLegacy(skin);
            boolean bl = skin.getRGB(54, 20) == 0 && skin.getRGB(50, 16) == 0;
            if(bl){
                skin = fixSlim(skin);
            }
        }

        if (skin.getWidth() != 64 || skin.getHeight() != 64) {
            throw new IllegalArgumentException("size");
        }

        BufferedImage avatar = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = avatar.createGraphics();

        BufferedImage head = SkinManager.getClipResize(skin, new Integer[]{8, 8, 8, 8}, 8, 8);
        g.drawImage(head, 4, 0, null);

        BufferedImage overlayHead = SkinManager.getClipResize(skin, new Integer[]{40, 8, 8, 8}, 8, 8);
        g.drawImage(overlayHead, 4, 0, null);

        BufferedImage body = SkinManager.getClipResize(skin, new Integer[]{20, 20, 8, 12}, 12, 8);
        g.drawImage(body, 4, 8, null);

        BufferedImage bodyOverlay = SkinManager.getClipResize(skin, new Integer[]{20, 36, 8, 12}, 12, 8);
        g.drawImage(bodyOverlay, 4, 8, null);

        BufferedImage leftArm = SkinManager.getClipResize(skin, new Integer[]{44, 20, 4, 12}, 12, 4);
        g.drawImage(leftArm, 0, 8, null);

        BufferedImage leftArmOverlay = SkinManager.getClipResize(skin, new Integer[]{44, 36, 4, 12}, 12, 4);
        g.drawImage(leftArmOverlay, 0, 8, null);

        BufferedImage rightArm = SkinManager.getClipResize(skin, new Integer[]{36, 52, 4, 12}, 12, 4);
        g.drawImage(rightArm, 12, 8, null);

        BufferedImage rightArmOverlay = SkinManager.getClipResize(skin, new Integer[]{52, 52, 4, 12}, 12, 4);
        g.drawImage(rightArmOverlay, 12, 8, null);

        BufferedImage leftLeg = SkinManager.getClipResize(skin, new Integer[]{4, 20, 4, 12}, 12, 4);
        g.drawImage(leftLeg, 4, 20, null);

        BufferedImage leftLegOverlay = SkinManager.getClipResize(skin, new Integer[]{4, 36, 4, 12}, 12, 4);
        g.drawImage(leftLegOverlay, 4, 20, null);

        BufferedImage rightLeg = SkinManager.getClipResize(skin, new Integer[]{20, 52, 4, 12}, 12, 4);
        g.drawImage(rightLeg, 8, 20, null);

        BufferedImage rightLegOverlay = SkinManager.getClipResize(skin, new Integer[]{4, 52, 4, 12}, 12, 4);
        g.drawImage(rightLegOverlay, 8, 20, null);

        g.dispose();
        return avatar;
    }

    public static BufferedImage loadImageFromResources(String fileName){
        try (InputStream is = SkinManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("Ресурс не найден: " + fileName);
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            return new BufferedImage(0, 0, 2);
        }  
    }

    public static BufferedImage genFakeSteve1(BufferedImage avatar){

        BufferedImage fakeHead = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fakeHead.createGraphics();

        BufferedImage texture = loadImageFromResources("image/fake/1.png");
        g.drawImage(texture, null, 0,0);

        BufferedImage head = SkinManager.getClipResize(avatar, new Integer[]{4, 24, 8, 8}, 8, 8);
        g.drawImage(head, null, 8, 8);

        return fakeHead;
    }

    public static BufferedImage genFakeSteve2(BufferedImage avatar){

        BufferedImage fakeHead = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fakeHead.createGraphics();

        BufferedImage texture = loadImageFromResources("image/fake/2.png");
        g.drawImage(texture, null, 0,0);

        BufferedImage head = SkinManager.getClipResize(avatar, new Integer[]{4, 20, 8, 4}, 8, 8);
        g.drawImage(head, null, 8, 8);

        return fakeHead;
    }

    public static BufferedImage genFakeSteve3(BufferedImage avatar){

        BufferedImage fakeHead = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fakeHead.createGraphics();

        BufferedImage texture = loadImageFromResources("image/fake/3.png");
        g.drawImage(texture, null, 0,0);

        BufferedImage head = SkinManager.getClipResize(avatar, new Integer[]{8, 12, 8, 8}, 8, 8);
        g.drawImage(head, null, 8, 8);

        return fakeHead;
    }

    public static BufferedImage genFakeSteve4(BufferedImage avatar){

        BufferedImage fakeHead = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fakeHead.createGraphics();

        BufferedImage texture = loadImageFromResources("image/fake/4.png");
        g.drawImage(texture, null, 0,0);

        BufferedImage head = SkinManager.getClipResize(avatar, new Integer[]{0, 12, 8, 8}, 8, 8);
        g.drawImage(head, null, 8, 8);

        return fakeHead;
    }

    public static BufferedImage genFakeSteve5(BufferedImage avatar){

        BufferedImage fakeHead = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fakeHead.createGraphics();

        BufferedImage texture = loadImageFromResources("image/fake/5.png");
        g.drawImage(texture, null, 0,0);

        BufferedImage head = SkinManager.getClipResize(avatar, new Integer[]{8, 8, 8, 4}, 8, 8);
        g.drawImage(head, null, 8, 8);

        return fakeHead;
    }

    public static BufferedImage genFakeSteve6(BufferedImage avatar){

        BufferedImage fakeHead = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fakeHead.createGraphics();

        BufferedImage texture = loadImageFromResources("image/fake/6.png");
        g.drawImage(texture, null, 0,0);

        BufferedImage head = SkinManager.getClipResize(avatar, new Integer[]{0, 8, 8, 4}, 8, 8);
        g.drawImage(head, null, 8, 8);

        return fakeHead;
    }

    public static BufferedImage genFakeSteve7(BufferedImage avatar){

        BufferedImage fakeHead = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fakeHead.createGraphics();

        BufferedImage texture = loadImageFromResources("image/fake/7.png");
        g.drawImage(texture, null, 0,0);

        BufferedImage head = SkinManager.getClipResize(avatar, new Integer[]{4, 0, 8, 8}, 8, 8);
        g.drawImage(head, null, 8, 8);

        return fakeHead;
    }

    public static BufferedImage genFakeSteve8(){

        return loadImageFromResources("image/fake/8.png");
    }

    public static BufferedImage genFakeSteve9(){

        return loadImageFromResources("image/fake/9.png");
    }

    public static void saveImage(BufferedImage image, String name) {
        try {
            File outputFile = new File(name+".png");
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
          
        }
    }

    public static CompletableFuture uploadToMineSkin(BufferedImage baseSkin, MineSkinClient client) throws IOException {
        return ((CompletableFuture)client.generateUpload(baseSkin).thenApply(skin -> skin.getSkin().data().texture().value())).exceptionally(throwable -> {

            if (throwable instanceof CompletionException) {
                CompletionException completionException = (CompletionException)throwable;
                throwable = completionException.getCause();
            }
            if (throwable instanceof MineSkinRequestException) {
                MineSkinRequestException requestException = (MineSkinRequestException)throwable;
                MineSkinResponse<?> response = requestException.getResponse();
                System.out.println(response.getMessageOrError());
            }
            return null;
        });
    }

    public static BufferedImage getCape1(BufferedImage skin) {
        BufferedImage cape = new BufferedImage(64, 32, 2);
        Graphics2D graphics = cape.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{1, 1, 8, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{14, 1, 8, 8}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{1, 0, 8, 1}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{0, 1, 1, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        return cape;
    }

    public static BufferedImage getCape2(BufferedImage skin) {
        BufferedImage cape = new BufferedImage(64, 32, 2);
        Graphics2D graphics = cape.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{1, 9, 8, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{14, 9, 8, 8}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{0, 9, 1, 8}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{11, 0, 8, 1}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(down, null, 16, 0);
        return cape;
    }

    public static BufferedImage getCape3(BufferedImage skin) {
        BufferedImage cape = new BufferedImage(64, 32, 2);
        Graphics2D graphics = cape.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{9, 1, 2, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{12, 1, 2, 8}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{9, 0, 2, 1}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{11, 1, 1, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(left, null, 16, 8);
        return cape;
    }

    public static BufferedImage getCape4(BufferedImage skin) {
        BufferedImage cape = new BufferedImage(64, 32, 2);
        Graphics2D graphics = cape.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{9, 9, 2, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{12, 9, 2, 8}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{19, 0, 2, 1}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{11, 9, 1, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(left, null, 16, 8);
        return cape;
    }

    public static BufferedImage getHead(BufferedImage skin) {
        return skin;
    }

    public static BufferedImage getBody1(BufferedImage skin) {
        BufferedImage body1 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = body1.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{20, 20, 8, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{32, 20, 8, 8}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{20, 16, 8, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{16, 20, 4, 8}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{28, 20, 4, 8}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{20, 36, 8, 8}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{32, 36, 8, 8}, 8, 8);
        BufferedImage upnd = SkinManager.getClipResize(skin, new Integer[]{20, 32, 8, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{16, 36, 4, 8}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{28, 36, 4, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(upnd, null, 40, 0);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        System.out.println("body1:"+bufferedImageToBase64(body1));
        return body1;
    }

    public static BufferedImage getBody2(BufferedImage skin) {
        BufferedImage body2 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = body2.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{20, 28, 8, 4}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{32, 28, 8, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{16, 28, 4, 4}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{28, 28, 4, 4}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{28, 16, 8, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{20, 44, 8, 4}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{32, 44, 8, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{16, 44, 4, 4}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{28, 44, 4, 4}, 8, 8);
        BufferedImage downnd = SkinManager.getClipResize(skin, new Integer[]{28, 32, 8, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        graphics.drawImage(downnd, null, 48, 0);
        System.out.println("body2:"+bufferedImageToBase64(body2));
        return body2;
    }



    public static BufferedImage getRightArm1(BufferedImage skin) {
        BufferedImage right_arm1 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = right_arm1.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{44, 20, 4, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{52, 20, 4, 8}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{44, 16, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{40, 20, 4, 8}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{48, 20, 4, 8}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{44, 36, 4, 8}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{52, 36, 4, 8}, 8, 8);
        BufferedImage upnd = SkinManager.getClipResize(skin, new Integer[]{44, 32, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{40, 36, 4, 8}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{48, 36, 4, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(upnd, null, 40, 0);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        return right_arm1;
    }

    public static BufferedImage getRightArm2(BufferedImage skin) {
        BufferedImage right_arm2 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = right_arm2.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{44, 28, 4, 4}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{52, 28, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{40, 28, 4, 4}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{48, 28, 4, 4}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{48, 16, 4, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{44, 44, 4, 4}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{52, 44, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{40, 44, 4, 4}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{48, 44, 4, 4}, 8, 8);
        BufferedImage downnd = SkinManager.getClipResize(skin, new Integer[]{48, 32, 4, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        graphics.drawImage(downnd, null, 48, 0);
        return right_arm2;
    }

    public static BufferedImage getNewRightArm1(BufferedImage skin) {
        BufferedImage right_arm1 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = right_arm1.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{44, 20, 4, 4}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{52, 20, 4, 4}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{44, 16, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{40, 20, 4, 4}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{48, 20, 4, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{44, 36, 4, 4}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{52, 36, 4, 4}, 8, 8);
        BufferedImage upnd = SkinManager.getClipResize(skin, new Integer[]{44, 32, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{40, 36, 4, 4}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{48, 36, 4, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(upnd, null, 40, 0);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        System.out.println("rightArm1:"+bufferedImageToBase64(right_arm1));
        return right_arm1;
    }

    public static BufferedImage getNewRightArm2(BufferedImage skin) {
        BufferedImage right_arm2 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = right_arm2.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{44, 24, 4, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{52, 24, 4, 8}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{40, 24, 4, 8}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{48, 24, 4, 8}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{48, 16, 4, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{44, 40, 4, 8}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{52, 40, 4, 8}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{40, 40, 4, 8}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{48, 40, 4, 8}, 8, 8);
        BufferedImage downnd = SkinManager.getClipResize(skin, new Integer[]{48, 32, 4, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        graphics.drawImage(downnd, null, 48, 0);
        System.out.println("rightArm2:"+bufferedImageToBase64(right_arm2));
        return right_arm2;
    }

    public static BufferedImage getLeftArm1(BufferedImage skin) {
        BufferedImage left_arm1 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = left_arm1.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{36, 52, 4, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{44, 52, 4, 8}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{36, 48, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{32, 52, 4, 8}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{40, 52, 4, 8}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{52, 52, 4, 8}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{60, 52, 4, 8}, 8, 8);
        BufferedImage upnd = SkinManager.getClipResize(skin, new Integer[]{52, 48, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{48, 52, 4, 8}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{56, 52, 4, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(upnd, null, 40, 0);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        return left_arm1;
    }

    public static BufferedImage getLeftArm2(BufferedImage skin) {
        BufferedImage left_arm2 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = left_arm2.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{36, 60, 4, 4}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{44, 60, 4, 4}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{40, 48, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{32, 60, 4, 4}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{40, 60, 4, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{52, 60, 4, 4}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{60, 60, 4, 4}, 8, 8);
        BufferedImage downnd = SkinManager.getClipResize(skin, new Integer[]{56, 48, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{48, 60, 4, 4}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{56, 60, 4, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        graphics.drawImage(downnd, null, 48, 0);
        return left_arm2;
    }

    public static BufferedImage getNewLeftArm1(BufferedImage skin) {
        BufferedImage left_arm1 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = left_arm1.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{36, 52, 4, 4}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{44, 52, 4, 4}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{36, 48, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{32, 52, 4, 4}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{40, 52, 4, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{52, 52, 4, 4}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{60, 52, 4, 4}, 8, 8);
        BufferedImage upnd = SkinManager.getClipResize(skin, new Integer[]{52, 48, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{48, 52, 4, 4}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{56, 52, 4, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(upnd, null, 40, 0);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        System.out.println("leftArm1:"+bufferedImageToBase64(left_arm1));
        return left_arm1;
    }

    public static BufferedImage getNewLeftArm2(BufferedImage skin) {
        BufferedImage left_arm2 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = left_arm2.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{36, 56, 4, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{44, 56, 4, 8}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{40, 48, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{32, 56, 4, 8}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{40, 56, 4, 8}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{52, 56, 4, 8}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{60, 56, 4, 8}, 8, 8);
        BufferedImage downnd = SkinManager.getClipResize(skin, new Integer[]{56, 48, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{48, 56, 4, 8}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{56, 56, 4, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        graphics.drawImage(downnd, null, 48, 0);
        System.out.println("leftArm2:"+bufferedImageToBase64(left_arm2));
        return left_arm2;
    }

    public static BufferedImage getRightLeg1(BufferedImage skin) {
        BufferedImage right_leg1 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = right_leg1.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{4, 20, 4, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{12, 20, 4, 8}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{4, 16, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{0, 20, 4, 8}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{8, 20, 4, 8}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{4, 36, 4, 8}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{12, 36, 4, 8}, 8, 8);
        BufferedImage upnd = SkinManager.getClipResize(skin, new Integer[]{4, 32, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{0, 36, 4, 8}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{8, 36, 4, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(upnd, null, 40, 0);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        System.out.println("rightLeg1:"+bufferedImageToBase64(right_leg1));
        return right_leg1;
    }

    public static BufferedImage getRightLeg2(BufferedImage skin) {
        BufferedImage right_leg2 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = right_leg2.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{4, 28, 4, 4}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{12, 28, 4, 4}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{8, 16, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{0, 28, 4, 4}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{8, 28, 4, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{4, 44, 4, 4}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{8, 32, 4, 4}, 8, 8);
        BufferedImage downnd = SkinManager.getClipResize(skin, new Integer[]{8, 32, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{0, 44, 4, 4}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{8, 44, 4, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        graphics.drawImage(downnd, null, 48, 0);
        System.out.println("rightLeg2:"+bufferedImageToBase64(right_leg2));
        return right_leg2;
    }

    public static BufferedImage getLeftLeg1(BufferedImage skin) {
        BufferedImage left_leg1 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = left_leg1.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{20, 52, 4, 8}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{28, 52, 4, 8}, 8, 8);
        BufferedImage up = SkinManager.getClipResize(skin, new Integer[]{20, 48, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{16, 52, 4, 8}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{24, 52, 4, 8}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{4, 52, 4, 8}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{12, 52, 4, 8}, 8, 8);
        BufferedImage upnd = SkinManager.getClipResize(skin, new Integer[]{4, 48, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{0, 52, 4, 8}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{8, 52, 4, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(up, null, 8, 0);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(upnd, null, 40, 0);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        System.out.println("leftLeg1:"+bufferedImageToBase64(left_leg1));
        return left_leg1;
    }

    public static BufferedImage getLeftLeg2(BufferedImage skin) {
        BufferedImage left_leg2 = new BufferedImage(64, 64, 2);
        Graphics2D graphics = left_leg2.createGraphics();
        BufferedImage front = SkinManager.getClipResize(skin, new Integer[]{20, 60, 4, 4}, 8, 8);
        BufferedImage back = SkinManager.getClipResize(skin, new Integer[]{28, 60, 4, 4}, 8, 8);
        BufferedImage down = SkinManager.getClipResize(skin, new Integer[]{24, 48, 4, 4}, 8, 8);
        BufferedImage right = SkinManager.getClipResize(skin, new Integer[]{16, 60, 4, 4}, 8, 8);
        BufferedImage left = SkinManager.getClipResize(skin, new Integer[]{24, 60, 4, 4}, 8, 8);
        BufferedImage frontnd = SkinManager.getClipResize(skin, new Integer[]{4, 60, 4, 4}, 8, 8);
        BufferedImage backnd = SkinManager.getClipResize(skin, new Integer[]{12, 60, 4, 4}, 8, 8);
        BufferedImage downnd = SkinManager.getClipResize(skin, new Integer[]{8, 48, 4, 4}, 8, 8);
        BufferedImage rightnd = SkinManager.getClipResize(skin, new Integer[]{0, 60, 4, 4}, 8, 8);
        BufferedImage leftnd = SkinManager.getClipResize(skin, new Integer[]{8, 60, 4, 4}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        graphics.drawImage(right, null, 0, 8);
        graphics.drawImage(left, null, 16, 8);
        graphics.drawImage(down, null, 16, 0);
        graphics.drawImage(frontnd, null, 40, 8);
        graphics.drawImage(backnd, null, 56, 8);
        graphics.drawImage(rightnd, null, 32, 8);
        graphics.drawImage(leftnd, null, 48, 8);
        graphics.drawImage(downnd, null, 48, 0);
        System.out.println("leftLeg2:"+bufferedImageToBase64(left_leg2));
        return left_leg2;
    }

    public static BufferedImage getArtHead(BufferedImage fullArt, int x, int y) throws IOException {
        BufferedImage artImage = new BufferedImage(64, 64, 2);
        Graphics2D graphics = artImage.createGraphics();
        BufferedImage front = SkinManager.getClipResize(fullArt, new Integer[]{y * 8, x * 8, 8, 8}, 8, 8);
        graphics.drawImage(front, null, 8, 8);
        BufferedImage down = SkinManager.getClipResize(fullArt, new Integer[]{56, 0, 8, 1}, 8, 8);
        graphics.drawImage(down, null, 16, 0);
        BufferedImage up = SkinManager.getClipResize(fullArt, new Integer[]{48, 0, 8, 1}, 8, 8);
        graphics.drawImage(up, null, 8, 0);
        BufferedImage right = SkinManager.getClipResize(fullArt, new Integer[]{48, 8, 1, 8}, 8, 8);
        graphics.drawImage(right, null, 16, 8);
        BufferedImage left = SkinManager.getClipResize(fullArt, new Integer[]{48, 8, 1, 8}, 8, 8);
        graphics.drawImage(left, null, 0, 8);
        BufferedImage back = SkinManager.getClipResize(fullArt, new Integer[]{56, 8, 8, 8}, 8, 8);
        graphics.drawImage(back, null, 24, 8);
        return artImage;
    }

    public static BufferedImage getClipResize(BufferedImage base, Integer[] offset, int sizex, int sizey) {
        int x = offset[0];
        int y = offset[1];
        int w = offset[2];
        int h2 = offset[3];
        BufferedImage image = base.getSubimage(x, y, w, h2);
        image = SkinManager.scale(image, sizex, sizey);
        return image;
    }

    public static BufferedImage scale(BufferedImage src, double y, double x) {
        BufferedImage before = src;
        BufferedImage after = new BufferedImage((int)x, (int)y, 2);
        AffineTransform at = new AffineTransform();
        at.scale(x / (double)src.getWidth(), y / (double)src.getHeight());
        AffineTransformOp scaleOp = new AffineTransformOp(at, 1);
        after = scaleOp.filter(before, after);
        return after;
    }

    public static BufferedImage fixSlim(BufferedImage image) {
        int color;
        int y;
        int x;
        BufferedImage slim = new BufferedImage(64, 64, 2);
        Graphics2D graphics = slim.createGraphics();
        Graphics2D g2 = image.createGraphics();
        BufferedImage fix = new BufferedImage(64, 64, 2);
        for (x = 44; x < 56; ++x) {
            for (y = 16; y < 32; ++y) {
                if (x == 44 || x == 45) {
                    color = image.getRGB(x, y);
                    fix.setRGB(x, y, color);
                }
                if (x == 46 || x == 47 || x == 48 || x == 49) {
                    color = image.getRGB(x - 1, y);
                    fix.setRGB(x, y, color);
                }
                if (x <= 49) continue;
                color = image.getRGB(x - 2, y);
                fix.setRGB(x, y, color);
            }
        }
        for (x = 44; x < 56; ++x) {
            for (y = 32; y < 48; ++y) {
                if (x == 44 || x == 45) {
                    color = image.getRGB(x, y);
                    fix.setRGB(x, y, color);
                }
                if (x == 46 || x == 47 || x == 48 || x == 49) {
                    color = image.getRGB(x - 1, y);
                    fix.setRGB(x, y, color);
                }
                if (x == 50 || x == 51) {
                    color = image.getRGB(x - 2, y);
                    fix.setRGB(x, y, color);
                }
                if (x <= 51) continue;
                color = image.getRGB(x - 2, y);
                fix.setRGB(x, y, color);
            }
        }
        for (x = 36; x < 56; ++x) {
            for (y = 48; y < 64; ++y) {
                if (x == 32 || x == 33) {
                    color = image.getRGB(x, y);
                    fix.setRGB(x, y, color);
                }
                if (x == 34 || x == 35 || x == 36 || x == 37) {
                    color = image.getRGB(x - 1, y);
                    fix.setRGB(x, y, color);
                }
                if (x == 38 || x == 39) {
                    color = image.getRGB(x - 2, y);
                    fix.setRGB(x, y, color);
                }
                if (x <= 39) continue;
                color = image.getRGB(x - 2, y);
                fix.setRGB(x, y, color);
            }
        }
        for (x = 52; x < 64; ++x) {
            for (y = 48; y < 64; ++y) {
                if (x == 48 || x == 49) {
                    color = image.getRGB(x, y);
                    fix.setRGB(x, y, color);
                }
                if (x == 50 || x == 51 || x == 52 || x == 53) {
                    color = image.getRGB(x - 1, y);
                    fix.setRGB(x, y, color);
                }
                if (x == 54 || x == 55) {
                    color = image.getRGB(x - 2, y);
                    fix.setRGB(x, y, color);
                }
                if (x <= 55) continue;
                color = image.getRGB(x - 2, y);
                fix.setRGB(x, y, color);
            }
        }
        graphics.drawImage(image, null, 0, 0);
        graphics.drawImage(fix, null, 0, 0);
        return slim;
    }

    public static BufferedImage fixLegacy(BufferedImage skin) {
        BufferedImage fixSkin = new BufferedImage(64, 64, 2);
        Graphics2D graphics = fixSkin.createGraphics();
        BufferedImage skinLegacy = SkinManager.getClipResize(skin, new Integer[]{0, 0, 64, 32}, 32, 64);
        BufferedImage arm = SkinManager.getClipResize(skin, new Integer[]{40, 16, 16, 16}, 16, 16);
        BufferedImage leg = SkinManager.getClipResize(skin, new Integer[]{0, 16, 16, 16}, 16, 16);
        graphics.drawImage(skinLegacy, null, 0, 0);
        graphics.drawImage(arm, null, 32, 48);
        graphics.drawImage(leg, null, 16, 48);
        BufferedImage arm2 = SkinManager.getClipResize(fixSkin, new Integer[]{32, 52, 16, 12}, 12, 16);
        BufferedImage leg2 = SkinManager.getClipResize(fixSkin, new Integer[]{16, 52, 16, 12}, 12, 16);
        leg2 = SkinManager.mirrorImageX(leg2);
        graphics.drawImage(leg2, null, 16, 52);
        arm2 = SkinManager.mirrorImageX(arm2);
        graphics.drawImage(arm2, null, 32, 52);
        BufferedImage leg3 = SkinManager.getClipResize(fixSkin, new Integer[]{20, 52, 12, 12}, 12, 12);
        graphics.drawImage(leg3, null, 16, 52);
        BufferedImage leg4 = SkinManager.getClipResize(skin, new Integer[]{12, 20, 4, 12}, 12, 4);
        graphics.drawImage(SkinManager.mirrorImageX(leg4), null, 28, 52);
        BufferedImage arm3 = SkinManager.getClipResize(fixSkin, new Integer[]{36, 52, 12, 12}, 12, 12);
        graphics.drawImage(arm3, null, 32, 52);
        BufferedImage arm4 = SkinManager.getClipResize(skin, new Integer[]{52, 20, 4, 12}, 12, 4);
        graphics.drawImage(SkinManager.mirrorImageX(arm4), null, 44, 52);
        BufferedImage arm5 = SkinManager.mirrorImageX(SkinManager.getClipResize(fixSkin, new Integer[]{36, 48, 4, 4}, 4, 4));
        graphics.drawImage(arm5, null, 36, 48);
        BufferedImage arm6 = SkinManager.mirrorImageX(SkinManager.getClipResize(fixSkin, new Integer[]{40, 48, 4, 4}, 4, 4));
        graphics.drawImage(arm6, null, 40, 48);
        BufferedImage leg5 = SkinManager.mirrorImageX(SkinManager.getClipResize(fixSkin, new Integer[]{20, 48, 4, 4}, 4, 4));
        graphics.drawImage(leg5, null, 20, 48);
        BufferedImage leg6 = SkinManager.mirrorImageX(SkinManager.getClipResize(fixSkin, new Integer[]{24, 48, 4, 4}, 4, 4));
        graphics.drawImage(leg6, null, 24, 48);
        int startX = 32;
        int startY = 0;
        int width = 32;
        int height = 16;
        for (int y = startY; y < startY + height; ++y) {
            for (int x = startX; x < startX + width; ++x) {
                fixSkin.setRGB(x, y, 0);
            }
        }
        return fixSkin;
    }

    public static BufferedImage mirrorImageX(BufferedImage image) {
        AffineTransform transform = AffineTransform.getScaleInstance(-1.0, 1.0);
        transform.translate(-image.getWidth(), 0.0);
        AffineTransformOp op = new AffineTransformOp(transform, 1);
        BufferedImage mirroredImage = op.filter(image, null);
        return mirroredImage;
    }

    public static BufferedImage mirrorImageY(BufferedImage image) {
        try {
            AffineTransform transform = AffineTransform.getScaleInstance(1.0, -1.0);
            transform.translate(0.0, -image.getHeight());
            AffineTransformOp op = new AffineTransformOp(transform, 1);
            BufferedImage mirroredImage = op.filter(image, null);
            return mirroredImage;
        } catch (Exception e) {
            return image;
        }
    }


}

