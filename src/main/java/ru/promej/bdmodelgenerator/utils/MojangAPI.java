package ru.promej.bdmodelgenerator.utils;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class MojangAPI {

    public static String getSkinByName(String name) {
        String uuid = MojangAPI.getUUIDbyName(name);
        String profileJson = MojangAPI.getProfileByUUID(uuid);
        JSONObject jsonObject = new JSONObject(profileJson);
        JSONArray propertiesArray = jsonObject.getJSONArray("properties");
        JSONObject propertiesObject = propertiesArray.getJSONObject(0);
        String value = propertiesObject.getString("value");
        byte[] decodedBytes = Base64.getDecoder().decode(value);
        String decodedString = new String(decodedBytes);
        JSONObject jsonObjectValue = new JSONObject(decodedString);
        JSONObject texturesObject = jsonObjectValue.getJSONObject("textures");
        JSONObject skinObject = texturesObject.getJSONObject("SKIN");
        return skinObject.getString("url");
    }

    private static String getUUIDbyName(String name) {
        String urlString = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return "incorrect";
            }
            JSONObject jsonObject = new JSONObject(response.body());
            return jsonObject.getString("id");
        } catch (Exception e) {
            return "incorrect";
        }
    }

    private static String getProfileByUUID(String uuid) {
        String urlString = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "incorrect";
            }
        } catch (Exception e) {
            return "incorrect";
        }
    }

    public static boolean validateName(String name) {
        System.out.println(name);
        String urlString = "https://api.mojang.com/users/profiles/minecraft/" + name;

        try {

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return !response.body().contains("Couldn't find any profile with name");
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

