package ru.promej.bdmodelgenerator;

import ru.promej.bdmodelgenerator.utils.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.promej.bdmodelgenerator.Generator.*;
import static ru.promej.bdmodelgenerator.utils.Utils.saveFiles;

public class Main {

    private static JTextArea logTextArea;
    private static JButton generateButton;

    public static void main(String[] args) {

        setDarkTheme();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BDModelGenerator v1.0");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 280);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            JLabel nicknameLabel = new JLabel("Nickname/Url:");
            JTextField nicknameField = new JTextField(20);

            JLabel apiKeyLabel = new JLabel("MineSkin ApiKey:");
            JTextField apiKeyField = new JTextField(20);

            JLabel modelLabel = new JLabel("Model:");
            String[] models = {"Plushe", "Mojang", "Cape"};
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
            sendLog("v1.0 by _PrometheuZ_");
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
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
            UIManager.put("control", new Color(50, 50, 50));
            UIManager.put("info", new Color(50, 50, 50));
            UIManager.put("menu", new Color(60, 60, 60));
            UIManager.put("text", Color.white);
            UIManager.put("textField", new Color(80, 80, 80));
            UIManager.put("nimbusBase", new Color(30, 30, 30));
            UIManager.put("nimbusBlueGrey", new Color(70, 70, 70));
            UIManager.put("nimbusLightBackground", new Color(50, 50, 50));
            UIManager.put("nimbusFocus", new Color(100, 100, 100));
            UIManager.put("menuText", Color.white);
            UIManager.put("Button.background", new Color(65, 65, 65));
            UIManager.put("Button.foreground", Color.white);
            UIManager.put("Button.border", BorderFactory.createEmptyBorder());
            UIManager.put("TabbedPane.selectedBackground", new Color(70, 70, 70));
            UIManager.put("TabbedPane.selectedForeground", Color.white);
            UIManager.put("Button.arc", 0);
            UIManager.put("TextField.border", BorderFactory.createEtchedBorder(1));
            UIManager.put("TextField.background", new Color(80, 80, 80));
            UIManager.put("TextField.foreground", Color.WHITE);

        } catch (UnsupportedLookAndFeelException exc) {
            System.err.println("Nimbus: Unsupported Look and feel!");
        }
    }


}
