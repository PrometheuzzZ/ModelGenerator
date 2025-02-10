package ru.promej.bdmodelgenerator;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import ru.promej.bdmodelgenerator.utils.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.promej.bdmodelgenerator.Generator.*;
import static ru.promej.bdmodelgenerator.utils.Utils.saveFiles;

public class Main {

    private static JTextArea logTextArea;
    private static JButton generateButton;
    private static JTextField apiKeyField;
    private static String apiKeyMineSkin = "";
    private static final String API_KEY_FILE = "apiKey.txt";
    private static boolean savedApiKey = false;

    public static void main(String[] args) {

        setDarkTheme();



        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BDModelGenerator v1.3");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 280);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            JLabel nicknameLabel = new JLabel("Nickname/Skin URL:");
            JTextField nicknameField = new JTextField(20);

            JLabel apiKeyLabel = new JLabel("MineSkin API Key:");
            apiKeyField = new JTextField(20);


            JLabel modelLabel = new JLabel("Model:");
            String[] models = {"Plushe", "Mojang", "Sleep Animation", "Fake Steve", "Cape"};
            JComboBox<String> modelComboBox = new JComboBox<>(models);

            generateButton = new JButton("Generate");
            generateButton.addActionListener(e -> {
                String nickname = nicknameField.getText();
                String apiKey = apiKeyField.getText();
                String selectedModel = (String) modelComboBox.getSelectedItem();
                generateButton.setEnabled(false);
                generate(nickname, apiKey, selectedModel);
            });

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0;
            gbc.gridy = 0;
            frame.add(nicknameLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            frame.add(nicknameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            frame.add(apiKeyLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            frame.add(apiKeyField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            frame.add(modelLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 5;
            frame.add(modelComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 6;
            frame.add(generateButton, gbc);

            logTextArea = new JTextArea();
            logTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(logTextArea);
            scrollPane.setPreferredSize(new Dimension(250, 200));

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridheight = 7;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            frame.add(scrollPane, gbc);

            ImageIcon icon = new ImageIcon(Main.class.getResource("/image/icon.png"));
            frame.setIconImage(icon.getImage());

            frame.setVisible(true);

            sendLog("BDModelGenerator started");
            sendLog("v1.3 by _PrometheuZ_");
            checkAndLoadApiKey();

            if(isSavedApiKey()){
                apiKeyField.setText(apiKeyMineSkin);
            }


        });


    }

    private static void generate(String data, String apiKey, String selectedModel) {
        boolean keyIsValide = Utils.checkMineSkinApiKey(apiKey);

        sendLog("Checking MineSkin ApiKey");

        if (keyIsValide) {

            sendLog("MineSkin ApiKey is Valid.");

            switch (selectedModel) {
                case "Plushe" -> {
                    boolean plusheIsValide = validPlushe(data, apiKey);
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
                    boolean sleepAnimationIsValide =  validSleep(data, apiKey);
                    if (!sleepAnimationIsValide) {
                        enableButton();
                    }
                }
                case "Fake Steve" -> {
                    boolean fakeSteveIsValide =  validFakeSteve(data, apiKey);
                    if (!fakeSteveIsValide) {
                        enableButton();
                    }
                }
            }


        } else {
            sendLog("Invalid MineSkin ApiKey!");
            enableButton();
        }

    }

    public static void showDialog(String cmdName, String cmdData, String bdName, String bdData) {

        JPanel panel = new JPanel();
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
        });
    }

    private static void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(null, "The model has been copied to the clipboard.");
        enableButton();
    }

    public static void sendLog(String msg) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logTextArea.append("[" + timeStamp + "] " + msg + "\n");
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
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
                    savedApiKey = true; // Устанавливаем значение, если ключ загружен
                    apiKeyMineSkin = apiKey;
                    sendLog("API Key loaded!");
                }
            } catch (IOException ignored) {

            }
        } else {

        }
    }

    public static void saveApiKey(String apiKey) {
        if(!savedApiKey) {
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
