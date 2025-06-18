package ru.promej.bdmodelgenerator;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import ru.promej.bdmodelgenerator.utils.Utils;



import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import static ru.promej.bdmodelgenerator.Generator.*;
import static ru.promej.bdmodelgenerator.utils.Utils.saveFiles;

public class Main {

    public static final String TEST_SKIN = "https://i.ibb.co/JWVGGZCn/steve.png";
    public static final String TEST_CAPE = "https://i.ibb.co/Kp4g6d1d/Skin-MC-Cape-89948.png";

    private static JTextPane logTextArea;
    private static JButton generateButton;
    private static JTextField apiKeyField;
    private static JTextField newModelField;
    private static JTextField commandField;
    private static JLabel previewLabel;


    private static String apiKeyMineSkin = "";
    private static final String API_KEY_FILE = "apiKey.txt";
    private static boolean savedApiKey = false;
    private static final String VERSION = "1.7";

    public static JCheckBox saveModelsCheckbox;

    public static void main(String[] args) {

        setDarkTheme();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BDModelGenerator v"+VERSION);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 545);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            JLabel nicknameLabel = new JLabel("Nickname/Skin URL:");
            JTextField nicknameField = new JTextField();
            nicknameField.setPreferredSize(new Dimension(300, 30));

            JLabel apiKeyLabel = new JLabel("MineSkin API Key:");
            apiKeyField = new JTextField();
            apiKeyField.setPreferredSize(new Dimension(300, 30));

            JLabel modelLabel = new JLabel("Model:");
            String[] models = {
                    "Plushe // Sit", "Plushe // Stand", "Mini // Sit", "Mini // Stand",
                    "Mojang", "Sleep Animation", "Fake Steve", "Cape"
            };
            JComboBox<String> modelComboBox = new JComboBox<>(models);
            modelComboBox.setPreferredSize(new Dimension(300, 30));

            JLabel commandLabel = new JLabel("Command:");
            commandField = new JTextField();
            commandField.setPreferredSize(new Dimension(300, 30));
            commandField.setEnabled(false);
            JButton commandCopyButton = new JButton("Copy");
            commandCopyButton.setPreferredSize(new Dimension(300, 30));

            JLabel newModelLabel = new JLabel("Model:");
            newModelField = new JTextField();
            newModelField.setPreferredSize(new Dimension(300, 30));
            newModelField.setEnabled(false);
            JButton modelCopyButton = new JButton("Copy");
            modelCopyButton.setPreferredSize(new Dimension(300, 30));

            generateButton = new JButton("Generate");
            generateButton.setPreferredSize(new Dimension(300, 40));
            generateButton.setBackground(new Color(74, 136, 220));
            generateButton.setForeground(Color.WHITE);

            previewLabel = new JLabel();
            previewLabel.setSize(125, 125);
            previewLabel.setHorizontalAlignment(SwingConstants.CENTER);

            modelComboBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    previewLabel.setVisible(true);
                    updatePreviewImage(previewLabel, (String) e.getItem());
                }
            });


            commandCopyButton.addActionListener(e -> {
                String commandText = commandField.getText();
                if (!commandText.isEmpty()) {
                    copyToClipboard(commandText);
                    sendLog("Command copied to clipboard!");
                } else {
                    sendLog("Command is empty!");
                }
            });

            modelCopyButton.addActionListener(e -> {
                String newModelText = newModelField.getText();
                if (!newModelText.isEmpty()) {
                    copyToClipboard(newModelText);
                    sendLog("Model copied to clipboard!");
                } else {
                    sendLog("Model is empty!");
                }
            });

            generateButton.addActionListener(e -> {
                String nickname = nicknameField.getText();
                String apiKey = apiKeyField.getText();
                String selectedModel = (String) modelComboBox.getSelectedItem();
                generateButton.setEnabled(false);
                previewLabel.setVisible(false);
                generate(nickname, apiKey, selectedModel);
            });

            saveModelsCheckbox = new JCheckBox("Save generated models");
            saveModelsCheckbox.setForeground(Color.WHITE);
            saveModelsCheckbox.setOpaque(false); // Чтобы он не имел фона (если dark theme)

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0; frame.add(nicknameLabel, gbc);
            gbc.gridy = 1; frame.add(nicknameField, gbc);
            gbc.gridy = 2; frame.add(apiKeyLabel, gbc);
            gbc.gridy = 3; frame.add(apiKeyField, gbc);
            gbc.gridy = 4; frame.add(modelLabel, gbc);
            gbc.gridy = 5; frame.add(modelComboBox, gbc);
            gbc.gridy = 6; frame.add(commandLabel, gbc);
            gbc.gridy = 7; frame.add(commandField, gbc);
            gbc.gridy = 8; frame.add(commandCopyButton, gbc);
            gbc.gridy = 9; frame.add(newModelLabel, gbc);
            gbc.gridy = 10; frame.add(newModelField, gbc);
            gbc.gridy = 11; frame.add(modelCopyButton, gbc);
            gbc.gridy = 12; frame.add(saveModelsCheckbox, gbc);
            gbc.gridy = 13; frame.add(generateButton, gbc);

            // === Правый блок с наложением превью ===
            logTextArea = new JTextPane();
            logTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(logTextArea);
            scrollPane.setBounds(0, 0, 450, 480); // Размер панели логов

            previewLabel.setBounds(300, 315, 150, 150);

            JLayeredPane layeredPane = new JLayeredPane();
            layeredPane.setPreferredSize(new Dimension(450, 460));
            layeredPane.add(scrollPane, Integer.valueOf(1));
            layeredPane.add(previewLabel, Integer.valueOf(2));

            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridheight = 14;
            gbc.fill = GridBagConstraints.BOTH;
            frame.add(layeredPane, gbc);

            // === Иконка ===
            ImageIcon icon = new ImageIcon(Main.class.getResource("/image/icon.png"));
            frame.setIconImage(icon.getImage());

            frame.setVisible(true);
            nicknameField.requestFocusInWindow();

            sendLogPurple("BDModelGenerator started");
            sendLogPurple("v"+VERSION+" by _PrometheuZ_");
            checkAndLoadApiKey();

            if (isSavedApiKey()) {
                apiKeyField.setText(apiKeyMineSkin);
            }

            // Превью по умолчанию
            updatePreviewImage(previewLabel, (String) modelComboBox.getSelectedItem());



        });
    }

    private static void updatePreviewImage(JLabel label, String model) {
        String path = switch (model) {
            case "Plushe // Sit" -> "/image/previews/plushe_sit.png";
            case "Plushe // Stand" -> "/image/previews/plushe_stand.png";
            case "Mini // Sit" -> "/image/previews/mini_sit.png";
            case "Mini // Stand" -> "/image/previews/mini_stand.png";
            case "Mojang" -> "/image/previews/mojang.png";
            case "Sleep Animation" -> "/image/previews/sleep.png";
            case "Fake Steve" -> "/image/previews/fakesteve.png";
            case "Cape" -> "/image/previews/cape.png";
            default -> null;
        };

        if (path != null) {
            java.net.URL imageUrl = Main.class.getResource(path);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            } else {
                label.setIcon(null);
                sendLog("Image not found: " + path);
            }
        } else {
            label.setIcon(null);
        }
    }

    private static void generate(String data, String apiKey, String selectedModel) {

        if (!data.isEmpty()) {

            boolean keyIsValide = Utils.checkMineSkinApiKey(apiKey);

            sendLog("Checking MineSkin ApiKey");

            if (keyIsValide) {

                sendLogGreen("MineSkin ApiKey is Valid.");

                switch (selectedModel) {

                    case "Mini // Sit" -> {
                        boolean plusheIsValide = validMiniSit(data, apiKey);
                        if (!plusheIsValide) {
                            enableButton();
                        }
                    }

                    case "Mini // Stand" -> {
                        boolean plusheIsValide = validMiniStand(data, apiKey);
                        if (!plusheIsValide) {
                            enableButton();
                        }
                    }


                    case "Plushe // Sit" -> {
                        boolean plusheIsValide = validPlusheSit(data, apiKey);
                        if (!plusheIsValide) {
                            enableButton();
                        }
                    }

                    case "Plushe // Stand" -> {
                        boolean plusheIsValide = validPlusheStand(data, apiKey);
                        if (!plusheIsValide) {
                            enableButton();
                        }
                    }

                    case "Mojang" -> {
                        boolean mojangIsValide = validMojang(data, apiKey);
                        if (!mojangIsValide) {
                            enableButton();
                        }
                    }
                    case "Cape" -> {
                        boolean capeIsValide = validCape(data, apiKey);
                        if (!capeIsValide) {
                            enableButton();
                        }
                    }

                    case "Sleep Animation" -> {
                        boolean sleepAnimationIsValide = validSleep(data, apiKey);
                        if (!sleepAnimationIsValide) {
                            enableButton();
                        }
                    }
                    case "Fake Steve" -> {
                        boolean fakeSteveIsValide = validFakeSteve(data, apiKey);
                        if (!fakeSteveIsValide) {
                            enableButton();
                        }
                    }
                }


            } else {
                sendLogRed("Invalid MineSkin ApiKey!");
                enableButton();
            }
        } else {
            sendLogRed("Please provide a nickname or a link to the texture!");
            enableButton();
        }
    }

    public static void endGeneration(String cmdName, String cmdData, String bdName, String bdData) {

        sendLog("Command:");
        sendLogGreen(cmdData);
        setCommandField(cmdData);
        sendLog("Model:");
        sendLogGreen(bdData);
        setNewModelFieldText(bdData);
        saveFiles(cmdName, cmdData, bdName, Utils.compressToGZIP(bdData));
        enableButton();
       try {
           sendLog(generatePreviewLink(bdData));
       } catch (Exception ignored){

       }
        sendLog("Telegram: https://t.me/promej");
        sendLog("Source: https://github.com/PrometheuzzZ/ModelGenerator");


       /* JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Save to files or copy model to clipboard?");
        panel.add(label, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save");
        JButton copyButton = new JButton("Copy");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(copyButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog();
        dialog.setTitle("Select an action");
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        saveButton.addActionListener(e -> {
            saveFiles(cmdName, cmdData, bdName, Utils.compressToGZIP(bdData));
            dialog.dispose();
            enableButton();
        });

        copyButton.addActionListener(e -> {
            copyToClipboard(bdData);
            dialog.dispose();
            enableButton();
        }); */
    }

    public static String generatePreviewLink(String modelCode) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(byteStream)) {
            gzip.write(modelCode.getBytes(StandardCharsets.UTF_8));
        }
        byte[] compressed = byteStream.toByteArray();
        String encoded = URLEncoder.encode(Base64.getEncoder().encodeToString(compressed), StandardCharsets.UTF_8);
        return "https://pjst.ru/bdengine/index.php?model=" + encoded;
    }

    private static void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(null, "The model has been copied to the clipboard.");
        enableButton();
    }

    public static void sendLogGreen(String msg) {
        sendLog(msg, new Color(160, 209, 108));
    }

    public static void sendLogRed(String msg) {
        sendLog(msg, new Color(232, 86, 62));
    }

    public static void sendLogOrange(String msg) {
        sendLog(msg, new Color(230, 126, 34));
    }

    public static void sendLogBlue(String msg) {
        sendLog(msg, new Color(80, 192, 204));
    }

    public static void sendLogPurple(String msg) {
        sendLog(msg, new Color(150, 121, 220));
    }

    public static void sendLog(String msg) {
        sendLog(msg, Color.WHITE);
    }

    public static void sendLog(String msg, Color color) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

        StyledDocument doc = logTextArea.getStyledDocument();

        SimpleAttributeSet timeStampAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(timeStampAttributes, Color.WHITE);

        SimpleAttributeSet msgAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(msgAttributes, color);

        try {
            doc.insertString(doc.getLength(), "[" + timeStamp + "] ", timeStampAttributes);
            doc.insertString(doc.getLength(), msg + "\n", msgAttributes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logTextArea.setCaretPosition(doc.getLength());

    }

    public static void setNewModelFieldText(String text) {
        newModelField.setText(text);
    }

    public static void setCommandField(String text) {
        commandField.setText(text);
    }

    public static void enableButton() {
        generateButton.setEnabled(true);

    }

    private static void setDarkTheme() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (UnsupportedLookAndFeelException exc) {
            System.err.println("Flatlaf: Unsupported FlatMacDarkLaf!");
        }
    }

    public static void checkAndLoadApiKey() {
        File file = new File(API_KEY_FILE);
        if (file.exists() && file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String apiKey = reader.readLine();
                if (apiKey != null) {
                    savedApiKey = true;
                    apiKeyMineSkin = apiKey;
                    sendLogGreen("API Key loaded!");
                }
            } catch (IOException ignored) {

            }
        } else {
            sendLogOrange("File apiKey.txt not found. It will be saved with your API key after the first successful generation.");
        }
    }

    public static void saveApiKey(String apiKey) {
        if (!savedApiKey) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(API_KEY_FILE))) {
                writer.write(apiKey);
                savedApiKey = true;
                System.out.println("API Key saved!");
            } catch (IOException e) {

            }
        }
    }

    public static boolean isSavedApiKey() {
        return savedApiKey;
    }

}
