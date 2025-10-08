package com.welfareconnect.view;

import com.welfareconnect.model.Application;
import com.welfareconnect.model.ApplicationDAO;
import com.welfareconnect.model.DocumentDAO;
import com.welfareconnect.model.Scheme;
import com.welfareconnect.model.SchemeDAO;
import com.welfareconnect.model.UserDAO;
import com.welfareconnect.util.FileStorage;
import com.welfareconnect.view.customcomponents.SchemeCellRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CitizenDashboardPanel extends JPanel {
    private final DefaultListModel<Scheme> schemeListModel = new DefaultListModel<>();
    private final JList<Scheme> schemeList = new JList<>(schemeListModel);
    private final JTextField searchField = new JTextField(24);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"All", "Education", "Agriculture", "Senior", "General"});

    private final String[] cols = {"ID", "Scheme", "Status", "Updated"};
    private final DefaultTableModel appsModel = new DefaultTableModel(cols, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable appsTable = new JTable(appsModel);

    private final JButton detailsBtn = new JButton("View Details");
    private final JButton applyBtn = new JButton("Apply Now");

    public CitizenDashboardPanel(String userIdentifier) {
        setLayout(new BorderLayout());
        putClientProperty("userIdentifier", userIdentifier);
        JTabbedPane tabs = new JTabbedPane();

        // --- Discover Tab ---
        JPanel discoverTab = new JPanel(new BorderLayout(0, 10));
        discoverTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel topActionPanel = new JPanel(new BorderLayout(10, 5));
        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchField.putClientProperty("JTextField.placeholderText", "Search schemes by name...");
        JButton searchBtn = new JButton("Search");
        searchFilterPanel.add(searchField);
        searchFilterPanel.add(searchBtn);
        searchFilterPanel.add(new JLabel("Category:"));
        searchFilterPanel.add(categoryBox);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.add(detailsBtn);
        buttonsPanel.add(applyBtn);
        topActionPanel.add(searchFilterPanel, BorderLayout.WEST);
        topActionPanel.add(buttonsPanel, BorderLayout.EAST);
        discoverTab.add(topActionPanel, BorderLayout.NORTH);
        schemeList.setCellRenderer(new SchemeCellRenderer());
        schemeList.setFixedCellHeight(120);
        detailsBtn.setEnabled(false);
        applyBtn.setEnabled(false);
        schemeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = !schemeList.isSelectionEmpty();
                detailsBtn.setEnabled(isSelected);
                applyBtn.setEnabled(isSelected);
            }
        });
        JScrollPane scrollPane = new JScrollPane(schemeList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        discoverTab.add(scrollPane, BorderLayout.CENTER);
        tabs.addTab("Discover", discoverTab);

        // --- My Applications Tab ---
        appsTable.setDefaultRenderer(Object.class, new StatusColorRenderer());
        JScrollPane sp = new JScrollPane(appsTable);
        tabs.addTab("My Applications", sp);

        // --- My Profile Tab (Rebuilt with GridBagLayout) ---
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Identifier
        gbc.gridx = 0;
        gbc.gridy = 0;
        profilePanel.add(new JLabel("Identifier:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        profilePanel.add(new JLabel(userIdentifier), gbc);

        // Row 1: Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField emailField = new JTextField();
        profilePanel.add(emailField, gbc);

        // Row 2: Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField phoneField = new JTextField();
        profilePanel.add(phoneField, gbc);

        // Row 3: Buttons
        JPanel profileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        JButton changePwButton = new JButton("Change Password");
        profileButtonsPanel.add(saveButton);
        profileButtonsPanel.add(changePwButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        profilePanel.add(profileButtonsPanel, gbc);
        changePwButton.addActionListener(e -> new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this), getUserIdentifier()).setVisible(true));
        
        JPanel profileContainer = new JPanel(new BorderLayout());
        profileContainer.add(profilePanel, BorderLayout.NORTH);
        tabs.addTab("My Profile", profileContainer);

        add(tabs, BorderLayout.CENTER);

        // Load initial data and wire actions
        reloadSchemes();
        reloadApplications();
        searchBtn.addActionListener(e -> reloadSchemes());
        categoryBox.addActionListener(e -> reloadSchemes());
        detailsBtn.addActionListener(e -> showSelectedDetails());
        applyBtn.addActionListener(e -> onApply());
    }

    private void reloadSchemes() {
        schemeListModel.clear();
        try {
            List<Scheme> items = new SchemeDAO().listActive(searchField.getText().trim(), (String) categoryBox.getSelectedItem());
            schemeListModel.addAll(items);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedDetails() {
        Scheme s = schemeList.getSelectedValue();
        if (s == null) return;
        
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setText("Name: " + s.getName() + "\n\nCategory: " + s.getCategory() + "\n\nBenefits: " + s.getDescription() + "\n\nEligibility: " + s.getEligibility());
        JScrollPane sp = new JScrollPane(text);
        sp.setPreferredSize(new Dimension(520, 320));
        JOptionPane.showMessageDialog(this, sp, "Scheme Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void onApply() {
        Scheme s = schemeList.getSelectedValue();
        if (s == null) return;

        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Apply to: " + s.getName() + " with " + fc.getSelectedFiles().length + " attachment(s)?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) return;

        try {
            Integer userId = new UserDAO().findIdByIdentifier(getUserIdentifier());
            if (userId == null) {
                JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // OPTIMIZED: Assumes create() returns the new application ID.
            int newAppId = new ApplicationDAO().create(userId, s.getId());
            if (newAppId == -1) {
                JOptionPane.showMessageDialog(this, "Failed to submit application", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (File f : fc.getSelectedFiles()) {
                String stored = FileStorage.store(f);
                String ct = Files.probeContentType(Path.of(stored));
                new DocumentDAO().add(newAppId, f.getName(), ct, stored);
            }

            JOptionPane.showMessageDialog(this, "Your application has been submitted with care! 🌱");
            reloadApplications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error during application: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getUserIdentifier() {
        Object id = getClientProperty("userIdentifier");
        return id == null ? "" : id.toString();
    }

    private void reloadApplications() {
        appsModel.setRowCount(0);
        try {
            Integer userId = new UserDAO().findIdByIdentifier(getUserIdentifier());
            if (userId == null) return;

            List<Application> items = new ApplicationDAO().listByUser(userId);
            for (Application a : items) {
                appsModel.addRow(new Object[]{a.getId(), a.getSchemeName(), a.getStatus(), a.getUpdatedAt()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(table.getValueAt(row, 2));
            if (!isSelected) {
                switch (status.toLowerCase()) {
                    case "approved":
                        c.setBackground(new Color(200, 230, 201)); // Green
                        break;
                    case "rejected":
                        c.setBackground(new Color(255, 205, 210)); // Red
                        break;
                    case "more info required":
                        c.setBackground(new Color(187, 222, 251)); // Blue
                        break;
                    default: // Pending
                        c.setBackground(new Color(255, 249, 196)); // Yellow
                        break;
                }
            }
            return c;
        }
    }
}