package ru.promej.bdmodelgenerator;

import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import ru.promej.bdmodelgenerator.utils.MojangAPI;
import ru.promej.bdmodelgenerator.utils.SkinManager;
import ru.promej.bdmodelgenerator.utils.Utils;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static ru.promej.bdmodelgenerator.Main.*;

public class Generator {

    public static boolean validCape(String data, String apiKey) {

        if (data.contains("http")) {
            if (Utils.isPngUrl(data)) {
                BufferedImage baseSkin = Utils.getSkin(data);
                if (Utils.validCapeSize(baseSkin)) {
                    sendLog("The cape model is being generated, please wait!");
                    generateCape(baseSkin, apiKey);
                    return true;
                } else {
                    sendLog("Texture size should be 64x32");
                    return false;
                }
            } else {
                sendLog("This link does not the image!");
                return false;
            }
        } else {
            sendLog("Only png links are allowed!");
            return false;
        }

    }

    public static void generateCape(BufferedImage capeSkin, String apiKey) {
        MineSkinClient client = MineSkinClient.builder().requestHandler(JsoupRequestHandler::new).userAgent("BDModelGenerator/v1.0").apiKey(apiKey).build();
        GenerateModelThread thread = new GenerateModelThread(apiKey, client, capeSkin, false, "cape");
        thread.start();
    }

    public static boolean validPlushe(String data, String apiKey) {
        return validSkinTypeModel(data, apiKey, "plushe");
    }

    public static boolean validMojang(String data, String apiKey) {
        return validSkinTypeModel(data, apiKey, "mojang");
    }

    public static boolean validSkinTypeModel(String data, String apiKey, String modelType) {
        boolean isUrl = Utils.isPngUrl(data);
        if (data.contains("http")) {
            if (isUrl) {
                BufferedImage baseSkin = Utils.getSkin(data);
                if (Utils.validSkinSize(baseSkin)) {
                    generateSkinTypeModel(baseSkin, apiKey, modelType);
                    sendLog("The "+modelType+" model is being generated, please wait!");
                    return true;
                } else {
                    baseSkin = SkinManager.fixLegacy(baseSkin);
                    generateSkinTypeModel(baseSkin, apiKey, modelType);
                    sendLog("The skin probably has a legacy format, the result may be distorted, use the new skin format (64x64). \nThe plush model is being generated, please wait!");
                    return true;
                }
            } else {
                sendLog("This link does not the image!");
                return false;
            }
        } else if (MojangAPI.validateName(data)) {
            String textureUrl = MojangAPI.getSkinByName(data);
            BufferedImage baseSkin = Utils.getSkin(textureUrl.replaceAll("http", "https"));
            if (Utils.validSkinSize(baseSkin)) {
                generateSkinTypeModel(baseSkin, apiKey, modelType);
                sendLog("The "+modelType+" model is being generated, please wait!");
                return true;
            } else {
                baseSkin = SkinManager.fixLegacy(baseSkin);
                generateSkinTypeModel(baseSkin, apiKey, modelType);
                sendLog("The skin probably has a legacy format, the result may be distorted, use the new skin format (64x64).\nThe model is being generated, please wait!");
                return true;
            }
        } else {
            sendLog("Please provide a valid premium nickname!");
            return false;
        }
    }

    public static void generateSkinTypeModel(BufferedImage baseSkin, String apiKey, String modelType) {
        boolean slim;
        MineSkinClient client = MineSkinClient.builder().requestHandler(JsoupRequestHandler::new).userAgent("BDModelGenerator/v1.0").apiKey(apiKey).build();
        boolean bl = slim = baseSkin.getRGB(54, 20) == 0 && baseSkin.getRGB(50, 16) == 0;
        if (slim) {
            baseSkin = SkinManager.fixSlim(baseSkin);
        }
        GenerateModelThread thread = new GenerateModelThread(apiKey, client, baseSkin, slim, modelType);
        thread.start();
    }

    static class GenerateModelThread extends Thread {

        MineSkinClient client;
        boolean slim;
        BufferedImage baseSkin;
        String type;

        GenerateModelThread(String name, MineSkinClient client, BufferedImage bufferedImage, boolean slim, String modelType) {
            super(name);
            this.client = client;
            this.slim = slim;
            this.baseSkin = bufferedImage;
            this.type = modelType;
        }

        @Override
        public void run() {

            if (this.type.contains("cape")) {
                this.upCape();
            }

            if (this.type.contains("plushe")) {
                this.upPlushe();
            }

            if (this.type.contains("mojang")) {
                this.upMojang();
            }
        }

        void upCape() {
            GenerateModelThread.uploadCape(this.baseSkin, this.client).thenAccept(response -> {
                String placeholderCmd = "/summon block_display ~-0.5 ~-0.5 ~-0.5 {Passengers:[{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,tag:{\"SkullOwner\":{\"Id\":[I;841973318,1559534668,-1505430383,-2080512014],\"Name\":\"textures\",\"Properties\":{\"textures\":[{\"Value\":\"cape2\"}]}}}},item_display:\"none\",transformation:[1f,0f,0f,0.0625f,0f,1f,0f,0.5f,0f,0f,0.1f,0f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,tag:{\"SkullOwner\":{\"Id\":[I;1609411612,1230555902,1409753989,620152179],\"Name\":\"textures\",\"Properties\":{\"textures\":[{\"Value\":\"cape1\"}]}}}},item_display:\"none\",transformation:[1f,0f,0f,0.0625f,0f,1f,0f,1f,0f,0f,0.1f,0f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,tag:{\"SkullOwner\":{\"Id\":[I;629862618,-406379466,921524969,1606991389],\"Name\":\"textures\",\"Properties\":{\"textures\":[{\"Value\":\"cape3\"}]}}}},item_display:\"none\",transformation:[0.2f,0f,0f,-0.2369f,0f,1f,0f,1f,0f,0f,0.1f,0f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,tag:{\"SkullOwner\":{\"Id\":[I;-968266829,-1693465733,486990720,-1084904590],\"Name\":\"textures\",\"Properties\":{\"textures\":[{\"Value\":\"cape4\"}]}}}},item_display:\"none\",transformation:[0.2f,0f,0f,-0.2369f,0f,1f,0f,0.5f,0f,0f,0.1f,0f,0f,0f,0f,1f]}]}";
                String placeholderModel = "[{\"isCollection\":true,\"name\":\"Project\",\"nbt\":\"\",\"settings\":{\"defaultBrightness\":false},\"mainNBT\":\"\",\"transforms\":[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"cape2\"},\"customTexture\":null,\"transforms\":[1,0,0,0.0625,0,1,0,0.5,0,0,0.1,0,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"cape1\"},\"customTexture\":null,\"transforms\":[1,0,0,0.0625,0,1,0,1,0,0,0.1,0,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"cape3\"},\"customTexture\":null,\"transforms\":[0.2,0,0,-0.2369,0,1,0,1,0,0,0.1,0,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"cape4\"},\"customTexture\":null,\"transforms\":[0.2,0,0,-0.2369,0,1,0,0.5,0,0,0.1,0,0,0,0,1]}]}]";
                placeholderCmd = placeholderCmd.replaceAll("cape1", response.get("cape1"));
                placeholderCmd = placeholderCmd.replaceAll("cape2", response.get("cape2"));
                placeholderCmd = placeholderCmd.replaceAll("cape3", response.get("cape3"));
                placeholderCmd = placeholderCmd.replaceAll("cape4", response.get("cape4"));
                placeholderModel = placeholderModel.replaceAll("cape1", response.get("cape1"));
                placeholderModel = placeholderModel.replaceAll("cape2", response.get("cape2"));
                placeholderModel = placeholderModel.replaceAll("cape3", response.get("cape3"));
                placeholderModel = placeholderModel.replaceAll("cape4", response.get("cape4"));

                String timeStamp = new SimpleDateFormat("HH-mm-ss").format(new Date());

                sendLog("Command: " + placeholderCmd);
                sendLog("Model: " + placeholderModel);

                showDialog("cape_cmd_"+timeStamp+".txt", placeholderCmd, "cape_model_"+timeStamp+".bdengine", placeholderModel);
            });
        }

        void upPlushe() {
            GenerateModelThread.uploadSkins(this.baseSkin, this.client, "Plushe").thenAcceptAsync(response -> {
                String placeholderCmd = "/summon block_display ~-0.5 ~-0.5 ~-0.5 {Passengers:[{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-55818088,-1782047674,1623097107,-1332018805],properties:[{name:\"textures\",value:\"plchead\"}]}}},item_display:\"none\",transformation:[-1f,0f,0f,0.5f,0f,1f,0f,1f,0f,0f,-1f,0.405f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-2117335698,-473887634,1501157238,-1164528384],properties:[{name:\"textures\",value:\"plcleft_arm1\"}]}}},item_display:\"none\",transformation:[-0.3386f,-0.0597f,0.0656f,0.7561f,-0.0608f,0.3447f,0f,0.5067f,-0.0646f,-0.0114f,-0.3438f,0.3972f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;1639196371,-1438552114,-1761276999,-152680809],properties:[{name:\"textures\",value:\"plcleft_arm2\"}]}}},item_display:\"none\",transformation:[-0.3386f,-0.1194f,0.0656f,0.7859f,-0.0608f,0.6894f,0f,0.3343f,-0.0646f,-0.0228f,-0.3438f,0.4029f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;1398708811,-1761993749,1735204421,1916673543],properties:[{name:\"textures\",value:\"plcbody1\"}]}}},item_display:\"none\",transformation:[-0.76f,0f,0f,0.4981f,0f,0.65f,0f,0.4926f,0f,0f,-0.375f,0.4004f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;1682599109,-1544260749,-462130361,1269951256],properties:[{name:\"textures\",value:\"plcbody2\"}]}}},item_display:\"none\",transformation:[-0.76f,0f,0f,0.4981f,0f,0.325f,0f,0.1676f,0f,0f,-0.375f,0.4004f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;1804871159,-1859310909,-124063484,-1592898927],properties:[{name:\"textures\",value:\"plcright_leg1\"}]}}},item_display:\"none\",transformation:[-0.3622f,0.1941f,0f,0.4345f,0f,0f,-0.375f,0.0879f,-0.0971f,-0.7244f,0f,0.3609f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;776829291,-927915836,2085834805,-81065057],properties:[{name:\"textures\",value:\"plcright_leg2\"}]}}},item_display:\"none\",transformation:[-0.3622f,0.0971f,0f,0.337f,0f,0f,-0.375f,0.0879f,-0.0971f,-0.3622f,0f,0.7249f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;84370037,-451192513,2067671505,-1046007361],properties:[{name:\"textures\",value:\"plcleft_leg1\"}]}}},item_display:\"none\",transformation:[-0.3622f,-0.1941f,0f,0.5487f,0f,0f,-0.375f,0.0879f,0.0971f,-0.7244f,0f,0.3625f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;1691806214,212872248,-1828665610,886084182],properties:[{name:\"textures\",value:\"plcleft_leg2\"}]}}},item_display:\"none\",transformation:[-0.3622f,-0.0971f,0f,0.6462f,0f,0f,-0.375f,0.0879f,0.0971f,-0.3622f,0f,0.7265f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-1143194848,-812530520,1820055344,1102262980],properties:[{name:\"textures\",value:\"plcright_arm1\"}]}}},item_display:\"none\",transformation:[-0.344f,0.0483f,-0.0427f,0.2328f,0.0487f,0.3466f,0f,0.5055f,0.0422f,-0.0059f,-0.3474f,0.392f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;1146888281,-1656207788,297910417,-294423099],properties:[{name:\"textures\",value:\"plcright_arm2\"}]}}},item_display:\"none\",transformation:[-0.344f,0.0967f,-0.0427f,0.209f,0.0487f,0.6932f,0f,0.3322f,0.0422f,-0.0119f,-0.3474f,0.3991f,0f,0f,0f,1f]}]}";
                String placeholderModel = "[{\"isCollection\":true,\"name\":\"Project\",\"nbt\":\"\",\"settings\":{\"defaultBrightness\":false},\"mainNBT\":\"\",\"transforms\":[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plchead\"},\"customTexture\":null,\"transforms\":[-1,-1.2246467991473532e-16,-1.2246467991473532e-16,0.5,-1.2246467991473532e-16,1,-1.2246467991473532e-16,1,1.2246467991473532e-16,-1.2246467991473532e-16,-1,0.405,0,0,0,1]},{\"isCollection\":true,\"name\":\"Group 2\",\"nbt\":\"\",\"transforms\":[-0.6771548701418598,-0.1194006737241468,0.13116692021000723,0.9199999999999999,-0.12155372436685091,0.6893654271085442,-8.257283745649584e-17,-0.004999999999999893,-0.12917419996154877,-0.022776896664651488,-0.6876010755100804,0.5231250000000001,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_arm1\"},\"customTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,0.5,0,0.765625,0,0,0.5,0.1328125,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_arm2\"},\"customTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,1,0,0.515625,0,0,0.5,0.1328125,0,0,0,1]}],\"animation\":[],\"defaultTransform\":{\"position\":[0.9199999999999999,-0.004999999999999893,0.5231250000000001],\"rotation\":{\"x\":3.141592653589793,\"y\":0.188495559215388,\"z\":2.9670597283903604},\"scale\":[0.6999999999999983,0.6999999999999985,0.6999999999999984]}},{\"isCollection\":true,\"name\":\"Group 3\",\"nbt\":\"\",\"transforms\":[-0.76,-7.960204194457797e-17,-9.184850993605147e-17,0.7,-9.307315673519885e-17,0.65,-9.184850993605148e-17,0,9.307315673519882e-17,-7.960204194457797e-17,-0.75,0.5,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcbody1\"},\"customTexture\":null,\"transforms\":[1,0,0,0.265625,0,1,0,0.7578125,0,0,0.5,0.1328125,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcbody2\"},\"customTexture\":null,\"transforms\":[1,0,0,0.265625,0,0.5,0,0.2578125,0,0,0.5,0.1328125,0,0,0,1]}],\"animation\":[],\"defaultTransform\":{\"position\":[0.7,0,0.5],\"rotation\":{\"x\":3.141592653589793,\"y\":-1.224646799147353e-16,\"z\":3.141592653589793},\"scale\":[0.76,0.65,0.75]}},{\"isCollection\":true,\"name\":\"Group 4\",\"nbt\":\"\",\"transforms\":[-0.7244443697167996,0.1941142838268905,-2.289834988289384e-16,0.38312499999999994,6.245004513516492e-17,-6.66133814775094e-16,-0.7499999999999996,0.1875,-0.19411428382689003,-0.7244443697168014,6.661338147750935e-16,0.9375,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_leg1\"},\"customTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,1,0,0.7603125,0,0,0.5,0.1328125,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_leg2\"},\"customTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,0.5,0,0.2578125,0,0,0.5,0.1328125,0,0,0,1]}],\"animation\":[],\"defaultTransform\":{\"position\":[0.38312499999999994,0.1875,0.9375],\"rotation\":{\"x\":1.5707963267948957,\"y\":-3.3306690738754696e-16,\"z\":-2.879793265790644},\"scale\":[0.7499999999999983,0.7500000000000001,0.7499999999999996]}},{\"isCollection\":true,\"name\":\"Group 5\",\"nbt\":\"\",\"transforms\":[-0.7244443697167996,-0.19411428382688994,-5.828670879282057e-16,0.7925,6.036837696399275e-16,8.326672684688656e-17,-0.749999999999998,0.1875,0.19411428382689,-0.7244443697167996,3.330669073875461e-16,0.8875000000000001,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_leg1\"},\"customTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,1,0,0.7603125,0,0,0.5,0.1328125,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_leg2\"},\"customTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,0.5,0,0.2578125,0,0,0.5,0.1328125,0,0,0,1]}],\"animation\":[],\"defaultTransform\":{\"position\":[0.7925,0.1875,0.8875000000000001],\"rotation\":{\"x\":1.5707963267948966,\"y\":-8.049116928532385e-16,\"z\":2.879793265790644},\"scale\":[0.7499999999999983,0.7499999999999983,0.7499999999999981]}},{\"isCollection\":true,\"name\":\"Group 5\",\"nbt\":\"\",\"transforms\":[-0.6880207325059082,0.09669500803893168,-0.0853085403836031,0.26187499999999997,0.09742117067204568,0.6931876481190992,-8.500145032286315e-17,-0.038124999999999964,0.084478323532833,-0.01187265410356318,-0.6947823061489219,0.48625,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_arm1\"},\"customTexture\":null,\"transforms\":[0.5,-1.9515639104739084e-18,-3.4694469519536138e-18,0.13271129767659218,1.9515639104739088e-18,0.49999999999999994,-6.770847460736377e-36,0.7656392230590056,3.469446951953614e-18,-6.770847460736377e-36,0.49999999999999994,0.13874999999999998,0,0,0,1]},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_arm2\"},\"customTexture\":null,\"transforms\":[0.49999999999999983,-1.7997756063259383e-17,1.7347234759768077e-17,0.13281249999999994,8.99887803162969e-18,0.9999999999999999,-3.4694469519536138e-18,0.5156249999999999,-1.734723475976808e-17,6.93889390390723e-18,0.4999999999999997,0.1328125000000001,0,0,0,1]}],\"animation\":[],\"defaultTransform\":{\"position\":[0.26187499999999997,-0.038124999999999964,0.48625],\"rotation\":{\"x\":3.141592653589793,\"y\":-0.12217304763960346,\"z\":-3.0019663134302466},\"scale\":[0.6999999999999976,0.7,0.6999999999999967]}}]}]";
                placeholderCmd = placeholderCmd.replaceAll("plchead", response.get("plchead"));
                placeholderCmd = placeholderCmd.replaceAll("plcbody1", response.get("plcbody1"));
                placeholderCmd = placeholderCmd.replaceAll("plcbody2", response.get("plcbody2"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_arm1", response.get("plcleft_arm1"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_arm2", response.get("plcleft_arm2"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_arm1", response.get("plcright_arm1"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_arm2", response.get("plcright_arm2"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_leg1", response.get("plcleft_leg1"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_leg2", response.get("plcleft_leg2"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_leg1", response.get("plcright_leg1"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_leg2", response.get("plcright_leg2"));
                placeholderModel = placeholderModel.replaceAll("plchead", response.get("plchead"));
                placeholderModel = placeholderModel.replaceAll("plcbody1", response.get("plcbody1"));
                placeholderModel = placeholderModel.replaceAll("plcbody2", response.get("plcbody2"));
                placeholderModel = placeholderModel.replaceAll("plcleft_arm1", response.get("plcleft_arm1"));
                placeholderModel = placeholderModel.replaceAll("plcleft_arm2", response.get("plcleft_arm2"));
                placeholderModel = placeholderModel.replaceAll("plcright_arm1", response.get("plcright_arm1"));
                placeholderModel = placeholderModel.replaceAll("plcright_arm2", response.get("plcright_arm2"));
                placeholderModel = placeholderModel.replaceAll("plcleft_leg1", response.get("plcleft_leg1"));
                placeholderModel = placeholderModel.replaceAll("plcleft_leg2", response.get("plcleft_leg2"));
                placeholderModel = placeholderModel.replaceAll("plcright_leg1", response.get("plcright_leg1"));
                placeholderModel = placeholderModel.replaceAll("plcright_leg2", response.get("plcright_leg2"));

                String timeStamp = new SimpleDateFormat("HH-mm-ss").format(new Date());

                sendLog("Command: " + placeholderCmd);
                sendLog("Model: " + placeholderModel);

                showDialog("plushe_cmd_"+timeStamp+".txt", placeholderCmd, "plushe_model_"+timeStamp+".bdengine", placeholderModel);
            });
        }

        void upMojang() {

            GenerateModelThread.uploadSkins(this.baseSkin, this.client, "Mojang").thenAcceptAsync(response -> {
                String placeholderCmd = "/summon block_display ~-0.5 ~-0.5 ~-0.5 {Passengers:[{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-1039546707,1430076149,-2133728465,-646061287],properties:[{name:\"textures\",value:\"plcleft_leg1\"}]}}},item_display:\"none\",transformation:[0.4989578091f,0.0482150575f,0.002755937f,-0.1294487753f,-0.0322661541f,0.745588686f,0.0426172977f,0.7640049204f,0f,-0.0653668071f,0.4881354021f,0.0599617657f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-1876677724,-480801547,1950833422,1745386750],properties:[{name:\"textures\",value:\"plcleft_leg2\"}]}}},item_display:\"none\",transformation:[0.4989578091f,0.0241075288f,0.002755937f,-0.1536768417f,-0.0322661541f,0.372794343f,0.0426172977f,0.3893466056f,0f,-0.0326834035f,0.4881354021f,0.0928085862f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;259406332,-2126732987,1735869138,1522357881],properties:[{name:\"textures\",value:\"plcright_leg1\"}]}}},item_display:\"none\",transformation:[0.4994030687f,-0.0364298848f,0.002543578f,0.1200064813f,0.0244128327f,0.7469989623f,-0.0366774431f,0.7634448614f,-0.0007672042f,0.056262011f,0.4886187628f,0.0642372805f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-1143471773,-119941970,2098999676,-575955024],properties:[{name:\"textures\",value:\"plcright_leg2\"}]}}},item_display:\"none\",transformation:[0.4994030687f,-0.0182149424f,0.002543578f,0.1383124983f,0.0244128327f,0.3734994811f,-0.0366774431f,0.3880778828f,-0.0007672042f,0.0281310055f,0.4886187628f,0.03596562f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-1671176149,569605972,-590198846,-1729659445],properties:[{name:\"textures\",value:\"plcbody1\"}]}}},item_display:\"none\",transformation:[1f,0f,0f,0f,0f,0.75f,0f,1.30859375f,0f,0f,0.5f,0.0625f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-1350170202,-1848907229,-1106479104,1565748197],properties:[{name:\"textures\",value:\"plcbody2\"}]}}},item_display:\"none\",transformation:[1f,0f,0f,0f,0f,0.375f,0f,0.93359375f,0f,0f,0.5f,0.0625f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;1730401098,-946393240,1720107148,1474502381],properties:[{name:\"textures\",value:\"plcright_arm1\"}]}}},item_display:\"none\",transformation:[0.373627506f,-0.0320430685f,0.0011380461f,0.3442311512f,0.0317567158f,0.3713838138f,0.0548221178f,1.3079289676f,-0.0043586416f,-0.0408938211f,0.4969841449f,0.0538599858f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-152939718,-1083361222,-591005142,-1245510484],properties:[{name:\"textures\",value:\"plcright_arm2\"}]}}},item_display:\"none\",transformation:[0.373627506f,-0.0640861369f,0.0011380461f,0.3602526854f,0.0317567158f,0.7427676276f,0.0548221178f,1.1222370607f,-0.0043586416f,-0.0817876422f,0.4969841449f,0.0743068963f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;2048537024,1461418603,-135921995,1810042006],properties:[{name:\"textures\",value:\"plcleft_arm1\"}]}}},item_display:\"none\",transformation:[0.3740823657f,0.0262066543f,0.001028458f,-0.342732248f,-0.0259640889f,0.3718308519f,-0.0548563153f,1.3016480722f,-0.0036400258f,0.0409881545f,0.4969806102f,0.0694317043f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-1240549952,-339133270,-1885242204,128850375],properties:[{name:\"textures\",value:\"plcleft_arm2\"}]}}},item_display:\"none\",transformation:[0.3740823657f,0.0524133087f,0.001028458f,-0.3558355752f,-0.0259640889f,0.7436617039f,-0.0548563153f,1.1157326462f,-0.0036400258f,0.081976309f,0.4969806102f,0.048937627f,0f,0f,0f,1f]},{id:\"minecraft:item_display\",item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{id:[I;-413316501,828116799,1396552714,-1886128421],properties:[{name:\"textures\",value:\"plchead\"}]}}},item_display:\"none\",transformation:[2f,0f,0f,0.001875f,0f,1.9935577569f,0.1603978487f,2.296783897f,0f,-0.1603978487f,1.9935577569f,0.0043336385f,0f,0f,0f,1f]}]}";
                String placeholderModel = "[{\"isCollection\":true,\"name\":\"Project\",\"nbt\":\"\",\"transforms\":[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],\"children\":[{\"isCollection\":true,\"name\":\"Left Leg\",\"nbt\":\"\",\"transforms\":[0.997915618272179,0.048215057502914195,0.00551187403186724,-0.299375,-0.06453230825295797,0.7455886860492682,0.08523459537180035,0.194375,8.673617379884035e-19,-0.06536680706074369,0.9762708041299106,-0.02,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_leg1\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,1,0,0.7603125,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcleft_leg1\"},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_leg2\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,0.5,0,0.2578125,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcleft_leg2\"}],\"defaultTransform\":{\"position\":[-0.299375,0.194375,-0.02],\"rotation\":{\"x\":-0.08708548519193789,\"y\":0.005624390910411478,\"z\":-0.06433212538059417},\"scale\":[1,0.75,0.98]}},{\"isCollection\":true,\"name\":\"Right Leg\",\"nbt\":\"\",\"transforms\":[0.9988061373414341,-0.036429884752462345,0.005087155913873167,0.014375,0.0488256653830706,0.7469989622604459,-0.07335488622529154,0.19875,-0.001534408350072092,0.05626201097311692,0.9772375256362101,-0.108125,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_leg1\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,1,0,0.7603125,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcright_leg1\"},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_leg2\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,0.5,0,0.2578125,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcright_leg2\"}],\"defaultTransform\":{\"position\":[0.014375,0.19875,-0.108125],\"rotation\":{\"x\":0.07492300974414268,\"y\":0.0051909987354682545,\"z\":0.04859295541513011},\"scale\":[1,0.75,0.98]}},{\"isCollection\":true,\"name\":\"Torso\",\"nbt\":\"\",\"transforms\":[1,0,0,-0.265625,0,0.75,0,0.740234375,0,0,1,-0.0703125,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcbody1\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[1,0,0,0.265625,0,1,0,0.7578125,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcbody1\"},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcbody2\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[1,0,0,0.265625,0,0.5,0,0.2578125,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcbody2\"}],\"defaultTransform\":{\"position\":[-0.265625,0.740234375,-0.0703125],\"rotation\":{\"x\":0,\"y\":0,\"z\":0},\"scale\":[1,0.75,1]}},{\"isCollection\":true,\"name\":\"Right Arm\",\"nbt\":\"\",\"transforms\":[0.7472550120063941,-0.06408613692535084,0.0022760922882428328,0.29375,0.06351343169398063,0.7427676276227022,0.10964423560019082,0.71625,-0.008717283164937712,-0.08178764215777404,0.9939682897374265,-0.014375,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_arm1\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,0.5,0,0.765625,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcright_arm1\"},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcright_arm2\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,1,0,0.515625,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcright_arm2\"}],\"defaultTransform\":{\"position\":[0.29375,0.71625,-0.014375],\"rotation\":{\"x\":-0.10986540629815844,\"y\":0.002276094253499889,\"z\":0.0855527296323138},\"scale\":[0.7499999999999999,0.7500000000000001,0.9999999999999997]}},{\"isCollection\":true,\"name\":\"Left Arm\",\"nbt\":\"\",\"transforms\":[0.7481647313207183,0.052413308657656435,0.0020569160755938417,-0.4825,-0.05192817785360102,0.7436617038983862,-0.10971263068988694,0.75375,-0.0072800516895446185,0.08197630895878509,0.9939612204524689,-0.124375,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_arm1\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,0.5,0,0.765625,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcleft_arm1\"},{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plcleft_arm2\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[0.5,0,0,0.1328125,0,1,0,0.515625,0,0,0.5,0.1328125,0,0,0,1],\"defaultTextureValue\":\"plcleft_arm2\"}],\"defaultTransform\":{\"position\":[-0.4825,0.75375,-0.124375],\"rotation\":{\"x\":0.1099341642784476,\"y\":0.002056917526032257,\"z\":-0.06994156906560929},\"scale\":[0.7500000000000001,0.7499999999999999,0.9999999999999996]}},{\"isCollection\":true,\"name\":\"Head\",\"nbt\":\"\",\"transforms\":[2,0,0,-0.529375,0,1.9935577569124936,0.1603978486577179,1.22625,0,-0.1603978486577179,1.9935577569124936,-0.4425,0,0,0,1],\"children\":[{\"isItemDisplay\":true,\"name\":\"player_head[display=none]\",\"brightness\":{\"sky\":15,\"block\":0},\"nbt\":\"\",\"tagHead\":{\"Value\":\"plchead\"},\"textureValueList\":[],\"paintTexture\":null,\"transforms\":[1,0,0,0.265625,0,1,0,0.515625,0,0,1,0.265625,0,0,0,1],\"defaultTextureValue\":\"plchead\"}],\"defaultTransform\":{\"position\":[-0.529375,1.22625,-0.4425],\"rotation\":{\"x\":-0.08028514559173922,\"y\":0,\"z\":0},\"scale\":[2,1.9999999999999993,1.9999999999999993]}}]}]";
                placeholderCmd = placeholderCmd.replaceAll("plchead", response.get("plchead"));
                placeholderCmd = placeholderCmd.replaceAll("plcbody1", response.get("plcbody1"));
                placeholderCmd = placeholderCmd.replaceAll("plcbody2", response.get("plcbody2"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_arm1", response.get("plcleft_arm1"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_arm2", response.get("plcleft_arm2"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_arm1", response.get("plcright_arm1"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_arm2", response.get("plcright_arm2"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_leg1", response.get("plcleft_leg1"));
                placeholderCmd = placeholderCmd.replaceAll("plcleft_leg2", response.get("plcleft_leg2"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_leg1", response.get("plcright_leg1"));
                placeholderCmd = placeholderCmd.replaceAll("plcright_leg2", response.get("plcright_leg2"));
                placeholderModel = placeholderModel.replaceAll("plchead", response.get("plchead"));
                placeholderModel = placeholderModel.replaceAll("plcbody1", response.get("plcbody1"));
                placeholderModel = placeholderModel.replaceAll("plcbody2", response.get("plcbody2"));
                placeholderModel = placeholderModel.replaceAll("plcleft_arm1", response.get("plcleft_arm1"));
                placeholderModel = placeholderModel.replaceAll("plcleft_arm2", response.get("plcleft_arm2"));
                placeholderModel = placeholderModel.replaceAll("plcright_arm1", response.get("plcright_arm1"));
                placeholderModel = placeholderModel.replaceAll("plcright_arm2", response.get("plcright_arm2"));
                placeholderModel = placeholderModel.replaceAll("plcleft_leg1", response.get("plcleft_leg1"));
                placeholderModel = placeholderModel.replaceAll("plcleft_leg2", response.get("plcleft_leg2"));
                placeholderModel = placeholderModel.replaceAll("plcright_leg1", response.get("plcright_leg1"));
                placeholderModel = placeholderModel.replaceAll("plcright_leg2", response.get("plcright_leg2"));

                String timeStamp = new SimpleDateFormat("HH-mm-ss").format(new Date());

                sendLog("Command: " + placeholderCmd);
                sendLog("Model: " + placeholderModel);
                showDialog("mojang_cmd_"+timeStamp+".txt", placeholderCmd, "mojang_model_"+timeStamp+".bdengine", placeholderModel);
            });
        }

        static CompletableFuture<Map<String, String>> uploadSkins(BufferedImage baseskin, MineSkinClient client, String modelType) {
            HashMap<String, CompletableFuture> futureMap = new HashMap<String, CompletableFuture>();
            try {
                futureMap.put("plchead", SkinManager.uploadToMineSkin(baseskin, client));
                sendLog(modelType + " generated: 9%");
                Thread.sleep(5500L);
                futureMap.put("plcbody1", SkinManager.uploadToMineSkin(SkinManager.getBody1(baseskin), client));
                Thread.sleep(5500L);
                futureMap.put("plcbody2", SkinManager.uploadToMineSkin(SkinManager.getBody2(baseskin), client));
                Thread.sleep(5500L);
                futureMap.put("plcleft_arm1", SkinManager.uploadToMineSkin(SkinManager.getNewLeftArm1(baseskin), client));
                sendLog(modelType + " generated: 36%");
                Thread.sleep(5500L);
                futureMap.put("plcleft_arm2", SkinManager.uploadToMineSkin(SkinManager.getNewLeftArm2(baseskin), client));
                Thread.sleep(5500L);
                futureMap.put("plcright_arm1", SkinManager.uploadToMineSkin(SkinManager.getNewRightArm1(baseskin), client));
                Thread.sleep(5500L);
                futureMap.put("plcright_arm2", SkinManager.uploadToMineSkin(SkinManager.getNewRightArm2(baseskin), client));
                sendLog(modelType + " generated: 63%");
                Thread.sleep(5500L);
                futureMap.put("plcleft_leg1", SkinManager.uploadToMineSkin(SkinManager.getLeftLeg1(baseskin), client));
                Thread.sleep(5500L);
                futureMap.put("plcleft_leg2", SkinManager.uploadToMineSkin(SkinManager.getLeftLeg2(baseskin), client));
                Thread.sleep(5500L);
                futureMap.put("plcright_leg1", SkinManager.uploadToMineSkin(SkinManager.getRightLeg1(baseskin), client));
                sendLog(modelType + " generated: 90%");
                Thread.sleep(5500L);
                futureMap.put("plcright_leg2", SkinManager.uploadToMineSkin(SkinManager.getRightLeg2(baseskin), client));
                sendLog(modelType + " generated: 100%");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            for (String key : futureMap.keySet()) {
                CompletableFuture completable = futureMap.get(key);
                completable.thenApply(v -> "");
            }
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0]));
            CompletionStage resultFuture = allFutures.thenApply(v -> {
                HashMap<String, String> urlMap = new HashMap<String, String>();
                for (Map.Entry entry : futureMap.entrySet()) {
                    String url = (String) ((CompletableFuture) entry.getValue()).join();
                    urlMap.put((String) entry.getKey(), url);
                }
                return urlMap;
            });
            return (CompletableFuture) resultFuture;
        }

        static CompletableFuture<Map<String, String>> uploadCape(BufferedImage baseskin, MineSkinClient client) {
            HashMap<String, CompletableFuture> futureMap = new HashMap<String, CompletableFuture>();
            try {
                futureMap.put("cape1", SkinManager.uploadToMineSkin(SkinManager.getCape1(baseskin), client));
                sendLog("Cape generated: 25%");
                Thread.sleep(2500L);
                futureMap.put("cape2", SkinManager.uploadToMineSkin(SkinManager.getCape2(baseskin), client));
                sendLog("Cape generated: 50%");
                Thread.sleep(2500L);
                futureMap.put("cape3", SkinManager.uploadToMineSkin(SkinManager.getCape3(baseskin), client));
                sendLog("Cape generated: 75%");
                Thread.sleep(2500L);
                futureMap.put("cape4", SkinManager.uploadToMineSkin(SkinManager.getCape4(baseskin), client));
                sendLog("Cape generated: 100%");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            for (String key : futureMap.keySet()) {
                CompletableFuture completable = futureMap.get(key);
                completable.thenApply(v -> "");
            }
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0]));
            CompletionStage resultFuture = allFutures.thenApply(v -> {
                HashMap<String, String> urlMap = new HashMap<String, String>();
                for (Map.Entry entry : futureMap.entrySet()) {
                    String url = (String) ((CompletableFuture) entry.getValue()).join();
                    urlMap.put((String) entry.getKey(), url);
                }
                return urlMap;
            });
            return (CompletableFuture) resultFuture;
        }

    }

}
