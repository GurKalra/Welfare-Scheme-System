package com.welfareconnect.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.welfareconnect.util.AccentTheme; // We will create this new class
import com.welfareconnect.util.I18n;

import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private static AccentTheme currentTheme = new AccentTheme(AccentTheme.BLUE); // Default theme

    public AppFrame(String role, String subRole, String displayName) {
        // Apply the theme first, before any components are created
        currentTheme.apply();

        setTitle(I18n.t("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setJMenuBar(buildMenuBar());

        if ("Citizen".equalsIgnoreCase(role)) {
            tabs.addTab("Citizen", new CitizenDashboardPanel(displayName));
        } else if ("Officer".equalsIgnoreCase(role)) {
            tabs.addTab("Officer", new OfficerDashboardPanel());
        } else if ("Admin".equalsIgnoreCase(role)) {
            tabs.addTab("Admin", new AdminDashboardPanel());
        }

        // --- UI Modernization: Wrapper Panel ---
        // This panel adds padding and a nice border around the main content area.
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Use a client property to give the tabs a modern card look with a subtle outline
        tabs.putClientProperty("JComponent.outline", "accent");
        tabs.putClientProperty("JComponent.roundRect", true);

        mainPanel.add(tabs, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu settings = new JMenu("Settings");
        JMenu theme = new JMenu("Theme");
        JMenu accent = new JMenu("Accent Color");
        JMenu account = new JMenu("Account");

        // --- Light/Dark Theme ---
        ButtonGroup themeGroup = new ButtonGroup();
        JRadioButtonMenuItem light = new JRadioButtonMenuItem("Light", UIManager.getLookAndFeel() instanceof FlatLightLaf);
        JRadioButtonMenuItem dark = new JRadioButtonMenuItem("Dark", UIManager.getLookAndFeel() instanceof FlatDarkLaf);

        themeGroup.add(light);
        themeGroup.add(dark);

        light.addActionListener(e -> switchLaf(new FlatLightLaf()));
        dark.addActionListener(e -> switchLaf(new FlatDarkLaf()));

        theme.add(light);
        theme.add(dark);

        // --- Accent Color Theme ---
        ButtonGroup accentGroup = new ButtonGroup();
        JRadioButtonMenuItem blue = new JRadioButtonMenuItem("Blue", true);
        JRadioButtonMenuItem green = new JRadioButtonMenuItem("Green");
        JRadioButtonMenuItem red = new JRadioButtonMenuItem("Red");

        accentGroup.add(blue);
        accentGroup.add(green);
        accentGroup.add(red);

        blue.addActionListener(e -> switchAccent(AccentTheme.BLUE, "Blue"));
        green.addActionListener(e -> switchAccent(AccentTheme.GREEN, "Green"));
        red.addActionListener(e -> switchAccent(AccentTheme.RED, "Red"));

        accent.add(blue);
        accent.add(green);
        accent.add(red);

        settings.add(theme);
        settings.add(accent);

        // --- Account Menu ---
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> doLogout());
        account.add(logout);

        bar.add(settings);
        bar.add(account);
        return bar;
    }

    private void switchLaf(LookAndFeel laf) {
        try {
            UIManager.setLookAndFeel(laf);
            // Re-apply the accent theme after changing light/dark mode
            currentTheme.apply();
            FlatLaf.updateUI();
        } catch (UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchAccent(Color accentColor, String name) {
        currentTheme = new AccentTheme(accentColor);
        currentTheme.apply();
        // This forces the UI to repaint with the new accent color
        FlatLaf.updateUI();
        System.out.println("Switched to " + name + " accent theme.");
    }

    private void doLogout() {
        // Use invokeLater to ensure UI updates happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
    }
}