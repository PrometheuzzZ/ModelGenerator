package ru.promej.bdmodelgenerator.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;


public class MojangAPI {


    public static String getSkinByName(String name) {
        String uuid = MojangAPI.getUUIDbyName(name);
        String profileJson = MojangAPI.getProfileByUUID(uuid);

        Gson gson = new Gson();
        MojangFullProfile profile;

        try {
            profile = gson.fromJson(profileJson, MojangFullProfile.class);
        } catch (Exception e) {
            return null;
        }

        for (MojangFullProfile.Property property : profile.getProperties()) {
            if (property.getName().equals("textures")) {
                String value = property.getValue();
                byte[] decodedBytes = Base64.getDecoder().decode(value);
                String decodedString = new String(decodedBytes);
                TextureProfile textureProfile;
                try {
                    textureProfile = gson.fromJson(decodedString, TextureProfile.class);
                    return textureProfile.getTextures().getSkin().getUrl();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }

        return null;
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

            Gson gson = new Gson();
            MojangProfile profile = gson.fromJson(response.body(), MojangProfile.class);

            return profile.getId();
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

    public class TextureProfile {

        @SerializedName("textures")
        private Textures textures;

        public Textures getTextures() {
            return textures;
        }

        public static class Textures {

            @SerializedName("SKIN")
            private Skin skin;

            public Skin getSkin() {
                return skin;
            }

            public static class Skin {
                @SerializedName("url")
                private String url;

                public String getUrl() {
                    return url;
                }
            }
        }
    }

    public class MojangProfile {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        public MojangProfile() {
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public class MojangFullProfile {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("properties")
        private List<Property> properties;

        @SerializedName("profileActions")
        private List<Object> profileActions;

        public MojangFullProfile() {
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Property> getProperties() {
            return properties;
        }

        public List<Object> getProfileActions() {
            return profileActions;
        }

        public static class Property {
            @SerializedName("name")
            private String name;

            @SerializedName("value")
            private String value;

            public Property() {
            }

            public String getName() {
                return name;
            }

            public String getValue() {
                return value;
            }
        }
    }
}

