package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.welfareconnect.model.Scheme;
import com.welfareconnect.model.SchemeDAO;

public class CitizenDashboardPanel extends JPanel {
    private final DefaultListModel<Scheme> schemeListModel = new DefaultListModel<>();
    private final JList<Scheme> schemeList = new JList<>(schemeListModel);
    private final JTextField searchField = new JTextField(24);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"All","Education","Agriculture","Senior","General"});

    private final String[] cols = {"ID","Scheme","Status","Updated"};
    private final DefaultTableModel appsModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;}};
    private final JTable appsTable = new JTable(appsModel);

    public CitizenDashboardPanel(String userIdentifier) {
        setLayout(new BorderLayout());
        putClientProperty("userIdentifier", userIdentifier);
        JTabbedPane tabs = new JTabbedPane();

        // Discover tab
        JPanel discover = new JPanel(new BorderLayout());
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchBtn = new JButton("Search");
        JButton detailsBtn = new JButton("View Details");
        JButton applyBtn = new JButton("Apply Now");
        search.add(searchField);
        search.add(searchBtn);
        search.add(categoryBox);
        search.add(detailsBtn);
        search.add(applyBtn);
        discover.add(search, BorderLayout.NORTH);
        discover.add(new JScrollPane(schemeList), BorderLayout.CENTER);
        tabs.addTab("Discover", discover);

        // My Applications tab
        appsTable.setDefaultRenderer(Object.class, new StatusColorRenderer());
        JScrollPane sp = new JScrollPane(appsTable);
        tabs.addTab("My Applications", sp);

        // My Profile tab
        JPanel profile = new JPanel(new GridLayout(0,2,8,8));
        profile.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        profile.add(new JLabel("Identifier")); profile.add(new JLabel(userIdentifier));
        profile.add(new JLabel("Email")); profile.add(new JTextField());
        profile.add(new JLabel("Phone")); profile.add(new JTextField());
        JButton save = new JButton("Save Changes");
        profile.add(save);
        JButton changePw = new JButton("Change Password");
        profile.add(changePw);
        changePw.addActionListener(e -> new ChangePasswordDialog(javax.swing.SwingUtilities.getWindowAncestor(this), getUserIdentifier()).setVisible(true));
        tabs.addTab("My Profile", profile);

        add(tabs, BorderLayout.CENTER);

        // Load initial data and wire actions
        reloadSchemes();
        reloadApplications();
        searchBtn.addActionListener(e -> reloadSchemes());
        detailsBtn.addActionListener(e -> showSelectedDetails());
        applyBtn.addActionListener(e -> onApply());
    }

    private void reloadSchemes() {
        schemeListModel.clear();
        try {
            List<Scheme> items = new SchemeDAO().listActive(searchField.getText().trim(), (String) categoryBox.getSelectedItem());
            for (Scheme s : items) schemeListModel.addElement(s);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedDetails() {
        Scheme s = schemeList.getSelectedValue();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a scheme first");
            return;
        }
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
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a scheme first");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        int ch = fc.showOpenDialog(this);
        if (ch != JFileChooser.APPROVE_OPTION) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Apply to: " + s.getName() + " with " + fc.getSelectedFiles().length + " attachment(s)?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) return;
        try {
            Integer userId = new com.welfareconnect.model.UserDAO().findIdByIdentifier(getUserIdentifier());
            if (userId == null) {
                JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = new com.welfareconnect.model.ApplicationDAO().create(userId, s.getId());
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Failed to submit application", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Fetch latest application id for this user+scheme
            java.util.List<com.welfareconnect.model.Application> apps = new com.welfareconnect.model.ApplicationDAO().listByUser(userId);
            int appId = apps.isEmpty() ? -1 : apps.get(0).getId();
            for (java.io.File f : fc.getSelectedFiles()) {
                String stored = com.welfareconnect.util.FileStorage.store(f);
                String ct = java.nio.file.Files.probeContentType(java.nio.file.Path.of(stored));
                new com.welfareconnect.model.DocumentDAO().add(appId, f.getName(), ct, stored);
            }
            JOptionPane.showMessageDialog(this, "Your application has been submitted with care! 🌱");
            reloadApplications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getUserIdentifier() {
        Object id = getClientProperty("userIdentifier");
        return id == null ? "" : id.toString();
    }

    private void reloadApplications() {
        appsModel.setRowCount(0);
        try {
            Integer userId = new com.welfareconnect.model.UserDAO().findIdByIdentifier(getUserIdentifier());
            if (userId == null) return;
            java.util.List<com.welfareconnect.model.Application> items = new com.welfareconnect.model.ApplicationDAO().listByUser(userId);
            for (com.welfareconnect.model.Application a : items) {
                appsModel.addRow(new Object[]{a.getId(), a.getSchemeName(), a.getStatus(), a.getUpdatedAt()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(table.getValueAt(row, 2));
            if (!isSelected) {
                if ("Approved".equalsIgnoreCase(status)) c.setBackground(new Color(200, 230, 201));
                else if ("Rejected".equalsIgnoreCase(status)) c.setBackground(new Color(255, 205, 210));
                else if ("More Info Required".equalsIgnoreCase(status)) c.setBackground(new Color(187, 222, 251));
                else c.setBackground(new Color(255, 249, 196)); // Pending
            }
            return c;
        }
    }
}
