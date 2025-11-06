package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints; // Import
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import com.welfareconnect.model.Notification;
import com.welfareconnect.model.NotificationDAO;
import com.welfareconnect.model.Scheme;
import com.welfareconnect.model.SchemeDAO;
import com.welfareconnect.model.User;
import com.welfareconnect.model.UserDAO;
import com.welfareconnect.view.customcomponents.SchemeCellRenderer;

public class CitizenDashboardPanel extends JPanel {
    // Fields for Discover Tab
    private final DefaultListModel<Scheme> schemeListModel = new DefaultListModel<>();
    private final JList<Scheme> schemeList = new JList<>(schemeListModel);
    private final JTextField searchField = new JTextField(24);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"All", "Education", "Agriculture", "Senior", "General"});
    private final JButton detailsBtn = new JButton("View Details");
    private final JButton applyBtn = new JButton("Apply Now");

    // Fields for My Applications Tab
    private final String[] cols = {"ID", "Scheme", "Status", "Updated"};
    private final DefaultTableModel appsModel = new DefaultTableModel(cols, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable appsTable = new JTable(appsModel);

    // Fields for Loading Indicators
    private final JPanel discoverContentPanel = new JPanel(new CardLayout());
    private final JPanel appsContentPanel = new JPanel(new CardLayout());

    // Fields for My Profile Tab
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JLabel subRoleLabel = new JLabel("...");
    private final JTextField regionField = new JTextField();
    private final JComboBox<String> incomeField;
    
    // Fields for Recommended for You
    private final DefaultListModel<Scheme> recommendedListModel = new DefaultListModel<>();
    private final JList<Scheme> recommendedList = new JList<>(recommendedListModel);
    private JPanel recommendationPanel;
    
    // Fields for Notifications Tab
    private final DefaultListModel<Notification> notificationListModel = new DefaultListModel<>();
    private final JList<Notification> notificationList = new JList<>(notificationListModel);

    public CitizenDashboardPanel(String userIdentifier) {
        setLayout(new BorderLayout());
        putClientProperty("userIdentifier", userIdentifier);
        
        // Initialize profile combo boxes
        incomeField = new JComboBox<>(new String[]{"Not Specified", "Under 50,000", "50,000 - 1,50,000", "1,50,001 - 5,00,000", "Above 5,00,001"});
        
        JTabbedPane tabs = new JTabbedPane();

        // --- Discover Tab ---
        JPanel discoverTab = new JPanel(new BorderLayout());
        discoverTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Recommended for You section
        recommendationPanel = new JPanel(new BorderLayout(5, 5));
        recommendationPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JLabel recommendedLabel = new JLabel("✨ Recommended for You (Based on Your Profile)");
        recommendedLabel.setFont(recommendedLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        recommendedLabel.setForeground(new Color(76, 175, 80));
        recommendationPanel.add(recommendedLabel, BorderLayout.NORTH);
        recommendedList.setCellRenderer(new SchemeCellRenderer());
        recommendedList.setFixedCellHeight(120);
        recommendedList.setVisibleRowCount(1);
        recommendedList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        JScrollPane recommendedScrollPane = new JScrollPane(recommendedList);
        recommendedScrollPane.setPreferredSize(new Dimension(0, 140));
        recommendationPanel.add(recommendedScrollPane, BorderLayout.CENTER);
        recommendationPanel.setVisible(true); // Always visible
        
        // Create a container for both recommendation panel and search panel
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new javax.swing.BoxLayout(northContainer, javax.swing.BoxLayout.Y_AXIS));
        northContainer.add(recommendationPanel);
        
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
        northContainer.add(topActionPanel);
        
        discoverTab.add(northContainer, BorderLayout.NORTH);
        schemeList.setCellRenderer(new SchemeCellRenderer());
        schemeList.setFixedCellHeight(120);
        detailsBtn.setEnabled(false);
        applyBtn.setEnabled(false);
        
        // Add listener to main scheme list
        schemeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = !schemeList.isSelectionEmpty();
                if (isSelected) {
                    // Clear recommended list selection when main list is selected
                    recommendedList.clearSelection();
                }
                detailsBtn.setEnabled(isSelected);
                applyBtn.setEnabled(isSelected);
            }
        });
        
        // Add listener to recommended list
        recommendedList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = !recommendedList.isSelectionEmpty();
                if (isSelected) {
                    // Clear main list selection when recommendation is selected
                    schemeList.clearSelection();
                }
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
        
        // --- NEW: EMPTY STATE PANEL ---
        JPanel appsEmptyPanel = new JPanel(new GridBagLayout());
        JLabel emptyLabel = new JLabel("You haven't applied for any schemes yet.");
        emptyLabel.setFont(emptyLabel.getFont().deriveFont(14f));
        emptyLabel.setForeground(Color.GRAY);
        appsEmptyPanel.add(emptyLabel);
        // --- END OF NEW PANEL ---
        
        appsContentPanel.add(appsScrollPane, "main");
        appsContentPanel.add(appsLoadingPanel, "loading");
        appsContentPanel.add(appsEmptyPanel, "empty"); // Added the "empty" card
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
        profilePanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        profilePanel.add(phoneField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        profilePanel.add(subRoleLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Region:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        profilePanel.add(regionField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Income:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        profilePanel.add(incomeField, gbc);
        JPanel profileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        JButton changePwButton = new JButton("Change Password");
        profileButtonsPanel.add(saveButton);
        profileButtonsPanel.add(changePwButton);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        profilePanel.add(profileButtonsPanel, gbc);
        changePwButton.addActionListener(e -> new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this), getUserIdentifier()).setVisible(true));
        saveButton.addActionListener(e -> onSaveProfile());
        JPanel profileContainer = new JPanel(new BorderLayout());
        profileContainer.add(profilePanel, BorderLayout.NORTH);
        tabs.addTab("My Profile", profileContainer);
        
        // --- Notifications Tab ---
        JPanel notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel notifTopPanel = new JPanel(new BorderLayout());
        JLabel notifLabel = new JLabel("Your Notifications");
        notifLabel.setFont(notifLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        notifTopPanel.add(notifLabel, BorderLayout.WEST);
        notificationsPanel.add(notifTopPanel, BorderLayout.NORTH);
        
        notificationList.setFixedCellHeight(60);
        JScrollPane notifScrollPane = new JScrollPane(notificationList);
        notificationsPanel.add(notifScrollPane, BorderLayout.CENTER);
        
        tabs.addTab("Notifications", notificationsPanel);

        add(tabs, BorderLayout.CENTER);

        // --- Actions and Listeners ---
        searchBtn.addActionListener(e -> reloadSchemes());
        categoryBox.addActionListener(e -> reloadSchemes());
        detailsBtn.addActionListener(e -> showSelectedDetails());
        applyBtn.addActionListener(e -> onApply());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                reloadApplications();
            } else if (tabs.getSelectedIndex() == 2) {
                loadProfileData();
            } else if (tabs.getSelectedIndex() == 3) {
                reloadNotifications();
            }
        });
        
        notificationList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onNotificationDoubleClick();
                }
            }
        });

        // Initial data load
        reloadSchemes();
        reloadApplications();
        loadProfileData();
        reloadRecommendations();
        
        // Check if profile is incomplete and prompt user
        checkProfileCompletion();
    }

    private void checkProfileCompletion() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                User user = new UserDAO().findByIdentifier(getUserIdentifier());
                if (user == null) return true; // Profile doesn't exist, can't check
                
                // Check if profile is incomplete (sub_role is set at registration, so only check region and income)
                boolean isIncomplete = (user.getRegion() == null || user.getRegion().isEmpty()) &&
                                      (user.getAnnualIncome() == null || user.getAnnualIncome().equals("Not Specified") || user.getAnnualIncome().isEmpty());
                
                return isIncomplete;
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        // Profile is incomplete, show notification
                        SwingUtilities.invokeLater(() -> {
                            int result = JOptionPane.showConfirmDialog(
                                CitizenDashboardPanel.this,
                                "Welcome! Your profile is incomplete.\n\n" +
                                "Please complete your profile (region and income) to:\n" +
                                "• Get personalized scheme recommendations\n" +
                                "• Improve your application experience\n\n" +
                                "Would you like to complete your profile now?",
                                "Complete Your Profile",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            
                            if (result == JOptionPane.YES_OPTION) {
                                // Switch to My Profile tab (index 2)
                                JTabbedPane tabs = (JTabbedPane) getParent();
                                if (tabs != null) {
                                    tabs.setSelectedIndex(2);
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    // Fail silently
                }
            }
        };
        worker.execute();
    }
    
    private void loadProfileData() {
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return new UserDAO().findByIdentifier(getUserIdentifier());
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        emailField.setText(user.getEmail());
                        phoneField.setText(user.getPhone());
                        if (user.getSubRole() != null) {
                            subRoleLabel.setText(user.getSubRole());
                        }
                        if (user.getRegion() != null) {
                            regionField.setText(user.getRegion());
                        }
                        if (user.getAnnualIncome() != null) {
                            incomeField.setSelectedItem(user.getAnnualIncome());
                        }
                    }
                } catch (Exception ex) {
                    // Fail silently, fields will just remain empty
                }
            }
        };
        worker.execute();
    }

    private void onSaveProfile() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String region = regionField.getText().trim();
        String income = (String) incomeField.getSelectedItem();

        if (email.isEmpty() || !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return new UserDAO().updateProfile(getUserIdentifier(), email, phone, region, income);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(CitizenDashboardPanel.this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        reloadRecommendations();
                    } else {
                        JOptionPane.showMessageDialog(CitizenDashboardPanel.this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CitizenDashboardPanel.this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
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
                // --- UPDATED LOGIC TO SHOW EMPTY CARD ---
                try {
                    appsModel.setRowCount(0);
                    List<Application> items = get();
                    
                    if (items.isEmpty()) {
                        cl.show(appsContentPanel, "empty"); // Show empty
                    } else {
                        for (Application a : items) {
                            appsModel.addRow(new Object[]{a.getId(), a.getSchemeName(), a.getStatus(), a.getUpdatedAt()});
                        }
                        cl.show(appsContentPanel, "main"); // Show main table
                    }
                } catch (Exception ex) {
                    appsModel.setRowCount(0);
                    cl.show(appsContentPanel, "empty"); // Show empty on error too
                }
                // 'finally' block removed as logic is handled in try/catch
            }
        };
        worker.execute();
    }

    private void showSelectedDetails() {
        // Check both main list and recommended list
        Scheme s = schemeList.getSelectedValue();
        if (s == null) {
            s = recommendedList.getSelectedValue();
        }
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
        // Check both main list and recommended list
        Scheme s = schemeList.getSelectedValue();
        if (s == null) {
            s = recommendedList.getSelectedValue();
        }
        if (s == null) return;
        
        try {
            Integer userId = new UserDAO().findIdByIdentifier(getUserIdentifier());
            if (userId == null) {
                JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ApplicationFormDialog dialog = new ApplicationFormDialog(SwingUtilities.getWindowAncestor(this), s, userId);
            dialog.setVisible(true);
            
            if (dialog.wasSubmitted()) {
                reloadApplications();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadRecommendations() {
        SwingWorker<List<Scheme>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Scheme> doInBackground() throws Exception {
                User user = new UserDAO().findByIdentifier(getUserIdentifier());
                if (user == null) {
                    return List.of();
                }
                
                List<Scheme> allSchemes = new SchemeDAO().listActive("", "All");
                List<Scheme> recommendations = new ArrayList<>();
                
                String subRole = user.getSubRole();
                String region = user.getRegion();
                String income = user.getAnnualIncome();
                
                // Check if profile is complete
                boolean hasSubRole = subRole != null && !subRole.isEmpty();
                boolean hasRegion = region != null && !region.isEmpty();
                boolean hasIncome = income != null && !income.equals("Not Specified") && !income.isEmpty();
                
                if (!hasSubRole && !hasRegion && !hasIncome) {
                    // Profile incomplete, return empty
                    return List.of();
                }
                
                for (Scheme s : allSchemes) {
                    String eligibility = s.getEligibility().toLowerCase();
                    String category = s.getCategory().toLowerCase();
                    String description = s.getDescription().toLowerCase();
                    String name = s.getName().toLowerCase();
                    
                    // Combined text for better matching
                    String combinedText = eligibility + " " + category + " " + description + " " + name;
                    
                    boolean matches = false;
                    
                    // Match sub_role (Student, Farmer, Senior Citizen, General Citizen)
                    if (hasSubRole) {
                        String subRoleLower = subRole.toLowerCase();
                        if (combinedText.contains(subRoleLower)) {
                            matches = true;
                        }
                        // Special mappings
                        if (subRoleLower.contains("farmer") && (combinedText.contains("agriculture") || combinedText.contains("farming"))) {
                            matches = true;
                        }
                        if (subRoleLower.contains("student") && (combinedText.contains("education") || combinedText.contains("scholarship"))) {
                            matches = true;
                        }
                        if (subRoleLower.contains("senior") && (combinedText.contains("elderly") || combinedText.contains("pension") || combinedText.contains("senior citizen"))) {
                            matches = true;
                        }
                        if (subRoleLower.contains("general") && (combinedText.contains("general") || combinedText.contains("citizen"))) {
                            matches = true;
                        }
                    }
                    
                    // Match region
                    if (hasRegion && combinedText.contains(region.toLowerCase())) {
                        matches = true;
                    }
                    
                    // Match income
                    if (hasIncome) {
                        if (income.contains("Under") && (combinedText.contains("low income") || combinedText.contains("below poverty"))) {
                            matches = true;
                        }
                        // Check for income-related keywords
                        String[] incomeWords = income.toLowerCase().split("[\\s,-]+");
                        for (String word : incomeWords) {
                            if (word.length() > 3 && combinedText.contains(word)) {
                                matches = true;
                                break;
                            }
                        }
                    }
                    
                    if (matches) {
                        recommendations.add(s);
                    }
                }
                
                return recommendations;
            }
            
            @Override
            protected void done() {
                try {
                    recommendedListModel.clear();
                    List<Scheme> recommendations = get();
                    
                    if (recommendations.isEmpty()) {
                        // Show message that profile is set but no matches
                        User user = new UserDAO().findByIdentifier(getUserIdentifier());
                        if (user != null) {
                            boolean hasProfile = (user.getSubRole() != null && !user.getSubRole().isEmpty()) ||
                                               (user.getRegion() != null && !user.getRegion().isEmpty()) ||
                                               (user.getAnnualIncome() != null && !user.getAnnualIncome().equals("Not Specified"));
                            
                            if (hasProfile) {
                                // Create a dummy scheme to show message
                                Scheme dummyScheme = new Scheme(0, "No personalized recommendations found", 
                                    "Info", 
                                    "Based on your profile (" + 
                                    (user.getSubRole() != null && !user.getSubRole().isEmpty() ? user.getSubRole() : "") + 
                                    (user.getRegion() != null && !user.getRegion().isEmpty() ? ", " + user.getRegion() : "") +
                                    (user.getAnnualIncome() != null && !user.getAnnualIncome().equals("Not Specified") ? ", " + user.getAnnualIncome() : "") +
                                    "), we couldn't find matching schemes. Browse all schemes below!", 
                                    "Complete your profile for better recommendations", true);
                                recommendedListModel.addElement(dummyScheme);
                            } else {
                                // Profile not complete
                                Scheme dummyScheme = new Scheme(0, "Complete your profile for recommendations", 
                                    "Info", 
                                    "Go to 'My Profile' tab and fill in your Region and Income to get personalized scheme recommendations!", 
                                    "Click 'My Profile' tab to get started", true);
                                recommendedListModel.addElement(dummyScheme);
                            }
                        }
                    } else {
                        recommendedListModel.addAll(recommendations);
                    }
                    
                    recommendationPanel.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    recommendationPanel.setVisible(false);
                }
            }
        };
        worker.execute();
    }

    private void reloadNotifications() {
        SwingWorker<List<Notification>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Notification> doInBackground() throws Exception {
                Integer userId = new UserDAO().findIdByIdentifier(getUserIdentifier());
                if (userId == null) {
                    return List.of();
                }
                return new NotificationDAO().getUnreadForUser(userId);
            }
            
            @Override
            protected void done() {
                try {
                    notificationListModel.clear();
                    notificationListModel.addAll(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CitizenDashboardPanel.this,
                        "Error loading notifications: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void onNotificationDoubleClick() {
        Notification selectedNotif = notificationList.getSelectedValue();
        if (selectedNotif == null) return;
        
        try {
            // Mark as read
            new NotificationDAO().markAsRead(selectedNotif.getId());
            
            // Reload notifications
            reloadNotifications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getUserIdentifier() {
        Object id = getClientProperty("userIdentifier");
        return id == null ? "" : id.toString();
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