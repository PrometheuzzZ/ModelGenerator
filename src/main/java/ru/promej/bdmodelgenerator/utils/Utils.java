
package ru.promej.bdmodelgenerator.utils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;

import static ru.promej.bdmodelgenerator.Main.*;

public class Utils {

    public static void main(String[] args) {
        System.out.println(checkMineSkinApiKey(""));
    }

    public static boolean validSkinSize(BufferedImage image) {
        if (image.getHeight() != 64) {
            return false;
        }
        return image.getWidth() == 64;
    }

    public static boolean validCapeSize(BufferedImage image) {
        if (image.getHeight() != 32) {
            return false;
        }
        return image.getWidth() == 64;
    }

    public static boolean isPngUrl(String link) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(link))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!contentType.equals("image/png")) {
                return false;
            }

            BufferedImage skin = ImageIO.read(response.body());
            return skin != null && skin.getHeight() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static String compressToGZIP(String str) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);){
                gzipOutputStream.write(str.getBytes());
            }
            byte[] compressedData = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(compressedData);
        } catch (Exception e) {
            return "error compress";
        }
    }

    public static BufferedImage getSkin(String link) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(link))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                return ImageIO.read(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkMineSkinApiKey(String key) {


        if(key.isEmpty()){
            return false;
        }

        String urlString = "https://api.mineskin.org/v2/delay";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "BDModelGenerator/v1.0");
            connection.setRequestProperty("Authorization", "Bearer " + key);
            connection.setRequestProperty("Accept", "application/json"); // Добавлен заголовок

            int responseCode = connection.getResponseCode();
            sendLog("MineSkin API key Response Code: " + responseCode);



            if (responseCode == 200) {
                saveApiKey(key);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Response Body: " + response.toString());
                return true;
            }
            if (responseCode == 403) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Response Body: " + response.toString());
                return false;
            }

            System.out.println(responseCode);

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static void saveFiles(String cmdName, String cmdData, String bdName, String bdData) {
        if (saveModelsCheckbox == null || !saveModelsCheckbox.isSelected()) {
            sendLog("Model saving is disabled (checkbox unchecked).");
            return;
        }

        String currentDirectory = System.getProperty("user.dir");

        Path cmdFilePath = Paths.get(currentDirectory, cmdName);
        Path bdFilePath = Paths.get(currentDirectory, bdName);

        try {
            Files.write(cmdFilePath, cmdData.getBytes());
            Files.write(bdFilePath, bdData.getBytes());
            sendLog("Files saved successfully:");
            sendLogGreen(" - " + cmdFilePath.toString());
            sendLogGreen(" - " + bdFilePath.toString());
        } catch (IOException e) {
            sendLogRed("Error saving files: " + e.getMessage());
        }
    }
}

