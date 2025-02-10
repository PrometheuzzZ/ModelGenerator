package ru.promej.bdmodelgenerator;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import ru.promej.bdmodelgenerator.utils.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.promej.bdmodelgenerator.Generator.*;
import static ru.promej.bdmodelgenerator.utils.Utils.saveFiles;

public class Main {

    private static JTextPane logTextArea;
    private static JButton generateButton;
    private static JTextField apiKeyField;
    private static JTextField newModelField;
    private static JTextField commandField;


    private static String apiKeyMineSkin = "";
    private static final String API_KEY_FILE = "apiKey.txt";
    private static boolean savedApiKey = false;

    public static void main(String[] args) {

        setDarkTheme();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BDModelGenerator v1.4");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 480);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            JLabel nicknameLabel = new JLabel("Nickname/Skin URL:");
            JTextField nicknameField = new JTextField(20);

            JLabel apiKeyLabel = new JLabel("MineSkin API Key:");
            apiKeyField = new JTextField(20);

            JLabel modelLabel = new JLabel("Model:");
            String[] models = {"Plushe", "Mojang", "Sleep Animation", "Fake Steve", "Cape"};
            JComboBox<String> modelComboBox = new JComboBox<>(models);

            JLabel commandLabel = new JLabel("Command:");
            commandField = new JTextField(20);
            JButton commandCopyButton = new JButton("Copy");
            commandField.setEnabled(false);

            JLabel newModelLabel = new JLabel("Model:");
            newModelField = new JTextField(20);
            JButton modelCopyButton = new JButton("Copy");
            newModelField.setEnabled(false);

            commandCopyButton.addActionListener(e -> {
                String commandText = commandField.getText();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(commandText), null);

                if (!commandField.getText().isEmpty()) {
                    copyToClipboard(commandText);
                    sendLog("Command copied to clipboard!");
                } else {
                    sendLog("Command is empty!");
                }

            });

            modelCopyButton.addActionListener(e -> {
                String newModelText = newModelField.getText();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(newModelText), null);

                if (!newModelField.getText().isEmpty()) {
                    copyToClipboard(newModelText);
                    sendLog("Model copied to clipboard!");
                } else {
                    sendLog("Model is empty!");
                }
            });

            generateButton = new JButton("Generate");
            generateButton.addActionListener(e -> {
                String nickname = nicknameField.getText();
                String apiKey = apiKeyField.getText();
                String selectedModel = (String) modelComboBox.getSelectedItem();
                generateButton.setEnabled(false);
                generate(nickname, apiKey, selectedModel);
            });
            generateButton.setBackground(new Color(74, 136, 220));
            generateButton.setForeground(Color.WHITE);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0;
            gbc.gridy = 0;
            frame.add(nicknameLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
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
            frame.add(commandLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 7;
            frame.add(commandField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 8;
            frame.add(commandCopyButton, gbc);

            gbc.gridx = 0;
            gbc.gridy = 9;
            frame.add(newModelLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 10;
            frame.add(newModelField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 11;
            frame.add(modelCopyButton, gbc);

            gbc.gridx = 0;
            gbc.gridy = 12;
            frame.add(generateButton, gbc);


            logTextArea = new JTextPane();
            logTextArea.setEditable(false);
            //logTextArea.setLineWrap(true);
            //logTextArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(logTextArea);
            scrollPane.setPreferredSize(new Dimension(250, 400));

            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridheight = 14;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            frame.add(scrollPane, gbc);

            ImageIcon icon = new ImageIcon(Main.class.getResource("/image/icon.png"));
            frame.setIconImage(icon.getImage());

            frame.setVisible(true);

            nicknameField.requestFocusInWindow();

            sendLogPurple("BDModelGenerator started");
            sendLogPurple("v1.4 by _PrometheuZ_");
            checkAndLoadApiKey();

            if (isSavedApiKey()) {
                apiKeyField.setText(apiKeyMineSkin);
            }
        });
    }

    private static void generate(String data, String apiKey, String selectedModel) {

        if (!data.isEmpty()) {

            boolean keyIsValide = Utils.checkMineSkinApiKey(apiKey);

            sendLog("Checking MineSkin ApiKey");

            if (keyIsValide) {

                sendLogGreen("MineSkin ApiKey is Valid.");

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

    public static void showDialog(String cmdName, String cmdData, String bdName, String bdData) {

        sendLog("Command:");
        sendLogGreen(cmdData);
        setCommandField(cmdData);
        sendLog("Model:");
        sendLogGreen(bdData);
        setNewModelFieldText(bdData);
        saveFiles(cmdName, cmdData, bdName, Utils.compressToGZIP(bdData));
        enableButton();

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
