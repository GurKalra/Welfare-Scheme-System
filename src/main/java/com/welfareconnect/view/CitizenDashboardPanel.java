package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.welfareconnect.model.Application;
import com.welfareconnect.model.ApplicationDAO;
import com.welfareconnect.model.DocumentDAO;
import com.welfareconnect.model.Scheme;
import com.welfareconnect.model.SchemeDAO;
import com.welfareconnect.model.UserDAO;
import com.welfareconnect.util.FileStorage;
import com.welfareconnect.view.customcomponents.SchemeCellRenderer;

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

    private final JPanel discoverContentPanel = new JPanel(new CardLayout());
    private final JPanel appsContentPanel = new JPanel(new CardLayout());

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
        JScrollPane schemeScrollPane = new JScrollPane(schemeList);
        schemeScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel discoverLoadingPanel = new JPanel(new GridBagLayout());
        discoverLoadingPanel.add(new JLabel("Loading available schemes..."));
        discoverContentPanel.add(schemeScrollPane, "main");
        discoverContentPanel.add(discoverLoadingPanel, "loading");
        discoverTab.add(discoverContentPanel, BorderLayout.CENTER);
        tabs.addTab("Discover", discoverTab);

        // --- My Applications Tab ---
        appsTable.setDefaultRenderer(Object.class, new StatusColorRenderer());
        JScrollPane appsScrollPane = new JScrollPane(appsTable);
        JPanel appsLoadingPanel = new JPanel(new GridBagLayout());
        appsLoadingPanel.add(new JLabel("Loading your applications..."));
        appsContentPanel.add(appsScrollPane, "main");
        appsContentPanel.add(appsLoadingPanel, "loading");
        tabs.addTab("My Applications", appsContentPanel);

        // --- My Profile Tab ---
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; profilePanel.add(new JLabel("Identifier:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        profilePanel.add(new JLabel(userIdentifier), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField emailField = new JTextField(); profilePanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField phoneField = new JTextField(); profilePanel.add(phoneField, gbc);
        JPanel profileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        JButton changePwButton = new JButton("Change Password");
        profileButtonsPanel.add(saveButton); profileButtonsPanel.add(changePwButton);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        profilePanel.add(profileButtonsPanel, gbc);
        changePwButton.addActionListener(e -> new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this), getUserIdentifier()).setVisible(true));
        JPanel profileContainer = new JPanel(new BorderLayout());
        profileContainer.add(profilePanel, BorderLayout.NORTH);
        tabs.addTab("My Profile", profileContainer);

        add(tabs, BorderLayout.CENTER);

        // --- Actions and Listeners ---
        searchBtn.addActionListener(e -> reloadSchemes());
        categoryBox.addActionListener(e -> reloadSchemes());
        detailsBtn.addActionListener(e -> showSelectedDetails());
        applyBtn.addActionListener(e -> onApply());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                reloadApplications();
            }
        });

        reloadSchemes();
        reloadApplications();
    }

    private void reloadSchemes() {
        CardLayout cl = (CardLayout) discoverContentPanel.getLayout();
        cl.show(discoverContentPanel, "loading");
        SwingWorker<List<Scheme>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Scheme> doInBackground() throws Exception {
                return new SchemeDAO().listActive(searchField.getText().trim(), (String) categoryBox.getSelectedItem());
            }
            @Override
            protected void done() {
                try {
                    schemeListModel.clear();
                    schemeListModel.addAll(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CitizenDashboardPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    cl.show(discoverContentPanel, "main");
                }
            }
        };
        worker.execute();
    }

    private void reloadApplications() {
        CardLayout cl = (CardLayout) appsContentPanel.getLayout();
        cl.show(appsContentPanel, "loading");
        SwingWorker<List<Application>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Application> doInBackground() throws Exception {
                Integer userId = new UserDAO().findIdByIdentifier(getUserIdentifier());
                if (userId == null) {
                    throw new Exception("Current user not found.");
                }
                return new ApplicationDAO().listByUser(userId);
            }
            @Override
            protected void done() {
                try {
                    appsModel.setRowCount(0);
                    List<Application> items = get();
                    for (Application a : items) {
                        appsModel.addRow(new Object[]{a.getId(), a.getSchemeName(), a.getStatus(), a.getUpdatedAt()});
                    }
                } catch (Exception ex) {
                    appsModel.setRowCount(0);
                } finally {
                    cl.show(appsContentPanel, "main");
                }
            }
        };
        worker.execute();
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

    static class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // THIS LINE IS FIXED
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