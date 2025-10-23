package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot; // Import
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.welfareconnect.model.ApplicationDAO;
import com.welfareconnect.model.Scheme;
import com.welfareconnect.model.SchemeDAO;
import com.welfareconnect.model.User;
import com.welfareconnect.model.UserDAO;

public class AdminDashboardPanel extends JPanel {
    // Fields for JTabbedPane
    private final JTabbedPane tabs = new JTabbedPane();

    // Fields for Schemes Tab
    private final DefaultTableModel schemeModel = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Active"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) {
                return Boolean.class; // This will render as a JCheckBox
            }
            return super.getColumnClass(columnIndex);
        }
    };
    private final JTable schemeTable = new JTable(schemeModel);
    
    // Fields for Users Tab
    private final DefaultTableModel userModel = new DefaultTableModel(new String[]{"ID", "Name", "Identifier", "Role", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable userTable = new JTable(userModel);
    private final JButton changeRoleButton = new JButton("Change Role");
    private final JButton toggleStatusButton = new JButton("Disable/Enable Account");
    private final JPanel userContentPanel = new JPanel(new CardLayout());

    // Fields for Analytics Tab
    private final JPanel analyticsContentPanel = new JPanel(new CardLayout());
    private final JPanel pieCardPanel = new JPanel(new CardLayout()); // Panel for pie chart
    private final DefaultPieDataset pieData = new DefaultPieDataset();
    private final DefaultCategoryDataset barData = new DefaultCategoryDataset();

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        tabs.setBorder(new EmptyBorder(5, 5, 5, 5));

        // --- Schemes Tab ---
        JPanel schemesPanel = new JPanel(new BorderLayout(0, 10));
        schemesPanel.add(new JScrollPane(schemeTable), BorderLayout.CENTER);
        
        JToolBar schemeActions = new JToolBar();
        schemeActions.setFloatable(false);
        JButton addScheme = new JButton("Add");
        JButton editScheme = new JButton("Edit");
        JButton delScheme = new JButton("Delete");
        schemeActions.add(addScheme);
        schemeActions.add(editScheme);
        schemeActions.add(delScheme);
        schemesPanel.add(schemeActions, BorderLayout.NORTH); 
        
        addScheme.addActionListener(e -> onAdd());
        editScheme.addActionListener(e -> onEdit());
        delScheme.addActionListener(e -> onDelete());

        editScheme.setEnabled(false);
        delScheme.setEnabled(false);
        schemeTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = schemeTable.getSelectedRow() != -1;
            editScheme.setEnabled(isSelected);
            delScheme.setEnabled(isSelected);
        });

        // --- Users Tab ---
        JPanel usersPanel = new JPanel(new BorderLayout(0, 10));
        JPanel userLoadingPanel = new JPanel(new GridBagLayout());
        userLoadingPanel.add(new JLabel("Loading users..."));
        userContentPanel.add(new JScrollPane(userTable), "main");
        userContentPanel.add(userLoadingPanel, "loading");
        usersPanel.add(userContentPanel, BorderLayout.CENTER);

        JToolBar userActions = new JToolBar();
        userActions.setFloatable(false);
        userActions.add(changeRoleButton);
        userActions.add(toggleStatusButton);
        usersPanel.add(userActions, BorderLayout.NORTH); 
        
        changeRoleButton.addActionListener(e -> onChangeRole());
        toggleStatusButton.addActionListener(e -> onToggleStatus());
        
        userTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            boolean isSelected = selectedRow != -1;
            changeRoleButton.setEnabled(isSelected);
            toggleStatusButton.setEnabled(isSelected);

            if (isSelected) {
                String currentStatus = (String) userModel.getValueAt(selectedRow, 4);
                if (currentStatus.equals("Active")) {
                    toggleStatusButton.setText("Disable Account");
                } else {
                    toggleStatusButton.setText("Enable Account");
                }
            } else {
                toggleStatusButton.setText("Disable/Enable Account"); // Reset text
            }
        });

        // --- Analytics Tab ---
        JPanel analyticsPanel = new JPanel(new BorderLayout());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField start = new JTextField(LocalDate.now().minusMonths(3).toString(), 10);
        JTextField end = new JTextField(LocalDate.now().toString(), 10);
        JButton refresh = new JButton("Refresh");
        filters.add(new JLabel("Start Date (YYYY-MM-DD)"));
        filters.add(start);
        filters.add(new JLabel("End Date"));
        filters.add(end);
        filters.add(refresh);
        analyticsPanel.add(filters, BorderLayout.NORTH);
        
        JPanel analyticsLoadingPanel = new JPanel(new GridBagLayout());
        analyticsLoadingPanel.add(new JLabel("Generating reports, please wait..."));
        
        // Pie Chart in a CardLayout
        var pieChart = ChartFactory.createPieChart("Applications by Scheme", pieData, true, true, false);
        ((PiePlot) pieChart.getPlot()).setSimpleLabels(true);
        ChartPanel piePanel = new ChartPanel(pieChart);
        pieCardPanel.add(piePanel, "chart");
        pieCardPanel.add(createAnalyticsEmptyPanel("pie"), "empty"); // Add empty panel
        
        // Bar Chart Y-Axis set to integers
        var barChart = ChartFactory.createBarChart("Approved vs Rejected", "Month", "Count", barData);
        CategoryPlot plot = barChart.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // Force integer ticks
        
        // --- THIS IS THE CORRECTED LINE ---
        rangeAxis.setLowerBound(0.0); // Start Y-axis at 0
        
        ChartPanel barPanel = new ChartPanel(barChart);
        
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        chartsPanel.add(pieCardPanel); // Add pieCardPanel
        chartsPanel.add(barPanel);
        
        analyticsContentPanel.add(chartsPanel, "main");
        analyticsContentPanel.add(analyticsLoadingPanel, "loading");
        analyticsPanel.add(analyticsContentPanel, BorderLayout.CENTER);
        refresh.addActionListener(e -> loadAnalytics(start.getText().trim(), end.getText().trim()));
        
        tabs.addTab("Schemes", schemesPanel);
        tabs.addTab("Users", usersPanel);
        tabs.addTab("Analytics", analyticsPanel);

        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 0) { 
                reloadSchemes();
            } else if (selectedIndex == 1) {
                reloadUsers();
            } else if (selectedIndex == 2) {
                loadAnalytics(start.getText().trim(), end.getText().trim());
            }
        });
        
        add(tabs, BorderLayout.CENTER);
        
        changeRoleButton.setEnabled(false);
        toggleStatusButton.setEnabled(false);

        reloadSchemes();
        reloadUsers();
        loadAnalytics(start.getText().trim(), end.getText().trim());
    }
    
    // Helper method to create an empty panel for charts
    private JPanel createAnalyticsEmptyPanel(String type) {
        JPanel p = new JPanel(new GridBagLayout());
        String message;
        if (type.equals("pie")) {
            message = "Not enough data to display a pie chart.";
        } else {
            message = "No data for this date range.";
        }
        JLabel label = new JLabel("<html><div style='text-align: center;'>" + message + "<br>Select a different date range or wait for new data.</div></html>");
        label.setForeground(Color.GRAY);
        p.add(label);
        return p;
    }

    private void reloadUsers() {
        CardLayout cl = (CardLayout) userContentPanel.getLayout();
        cl.show(userContentPanel, "loading");

        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return new UserDAO().getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    userModel.setRowCount(0);
                    for (User user : get()) {
                        userModel.addRow(new Object[]{
                            user.getId(),
                            user.getName(),
                            user.getIdentifier(),
                            user.getRole(),
                            user.isActive() ? "Active" : "Disabled"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this, "Failed to load users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    cl.show(userContentPanel, "main");
                }
            }
        };
        worker.execute();
    }

    private void onChangeRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;

        int userId = (int) userModel.getValueAt(selectedRow, 0);
        String currentRole = (String) userModel.getValueAt(selectedRow, 3);
        
        String[] roles = {"Citizen", "Officer", "Admin"};
        String newRole = (String) JOptionPane.showInputDialog(this, "Select new role:", "Change User Role",
                JOptionPane.PLAIN_MESSAGE, null, roles, currentRole);

        if (newRole != null && !newRole.equals(currentRole)) {
            try {
                if (new UserDAO().updateUserRole(userId, newRole)) {
                    JOptionPane.showMessageDialog(this, "User role updated successfully.");
                    reloadUsers();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update role: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onToggleStatus() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;

        int userId = (int) userModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) userModel.getValueAt(selectedRow, 4);
        boolean newStatus = !currentStatus.equals("Active");
        String action = newStatus ? "Enable" : "Disable";

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to " + action + " this account?", "Confirm Status Change", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (new UserDAO().updateUserStatus(userId, newStatus)) {
                JOptionPane.showMessageDialog(this, "User account has been " + (newStatus ? "enabled." : "disabled."));
                reloadUsers();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAnalytics(String start, String end) {
        CardLayout cl = (CardLayout) (analyticsContentPanel.getLayout());
        cl.show(analyticsContentPanel, "loading");
        SwingWorker<Map<String, List<?>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, List<?>> doInBackground() throws Exception {
                List<ApplicationDAO.SchemeCount> schemeCounts = new ApplicationDAO().applicationsByScheme(start, end);
                List<ApplicationDAO.MonthlyStatus> monthlyStatuses = new ApplicationDAO().monthlyApprovedRejected(start, end);
                return Map.of("pieData", schemeCounts, "barData", monthlyStatuses);
            }
            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Map<String, List<?>> results = get();
                    List<ApplicationDAO.SchemeCount> schemeCounts = (List<ApplicationDAO.SchemeCount>) results.get("pieData");
                    List<ApplicationDAO.MonthlyStatus> monthlyStatuses = (List<ApplicationDAO.MonthlyStatus>) results.get("barData");
                    
                    // Logic to show pie chart or empty panel
                    CardLayout pieCL = (CardLayout) pieCardPanel.getLayout();
                    pieData.clear();
                    if (schemeCounts.size() <= 1) {
                        pieCL.show(pieCardPanel, "empty");
                    } else {
                        for (ApplicationDAO.SchemeCount s : schemeCounts) pieData.setValue(s.schemeName, s.count);
                        pieCL.show(pieCardPanel, "chart");
                    }

                    barData.clear();
                    for (ApplicationDAO.MonthlyStatus m : monthlyStatuses) {
                        barData.addValue(m.approved, "Approved", m.yearMonth);
                        barData.addValue(m.rejected, "Rejected", m.yearMonth);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this, "Failed to load analytics: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    cl.show(analyticsContentPanel, "main");
                }
            }
        };
        worker.execute();
    }

    private void reloadSchemes() {
        schemeModel.setRowCount(0);
        try {
            List<Scheme> items = new SchemeDAO().listAll();
            for (Scheme s : items) {
                schemeModel.addRow(new Object[]{s.getId(), s.getName(), s.getCategory(), s.isActive()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedSchemeId() {
        int selectedRow = schemeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a scheme from the table first.");
            return -1;
        }
        return (int) schemeModel.getValueAt(selectedRow, 0);
    }
    
    private void onAdd() {
        SchemeForm form = new SchemeForm();
        int res = JOptionPane.showConfirmDialog(this, form.panel, "Add Scheme", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            new SchemeDAO().create(form.name.getText().trim(), form.category.getText().trim(), form.description.getText(), form.eligibility.getText(), form.active.isSelected());
            reloadSchemes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        int schemeId = getSelectedSchemeId();
        if (schemeId == -1) return;
        try {
            Scheme selected = new SchemeDAO().findById(schemeId);
            if(selected == null) {
                JOptionPane.showMessageDialog(this, "Scheme not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            SchemeForm form = new SchemeForm();
            form.name.setText(selected.getName());
            form.category.setText(selected.getCategory());
            form.description.setText(selected.getDescription());
            form.eligibility.setText(selected.getEligibility());
            form.active.setSelected(selected.isActive());
            int res = JOptionPane.showConfirmDialog(this, form.panel, "Edit Scheme", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            new SchemeDAO().update(selected.getId(), form.name.getText().trim(), form.category.getText().trim(), form.description.getText(), form.eligibility.getText(), form.active.isSelected());
            reloadSchemes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int schemeId = getSelectedSchemeId();
        if (schemeId == -1) return;
        String schemeName = (String) schemeModel.getValueAt(schemeTable.getSelectedRow(), 1);
        int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete \"" + schemeName + "\"?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        try {
            new SchemeDAO().delete(schemeId);
            reloadSchemes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not delete scheme: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // No changes needed to the inner SchemeForm class
    static class SchemeForm {
        final JPanel panel = new JPanel(new GridBagLayout());
        final JTextField name = new JTextField(30);
        final JTextField category = new JTextField(30);
        final JTextArea description = new JTextArea(5, 30);
        final JTextArea eligibility = new JTextArea(4, 30);
        final JCheckBox active = new JCheckBox("Active", true);

        SchemeForm() {
            panel.setBorder(new EmptyBorder(10,10,10,10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0; panel.add(new JLabel("Name:"), gbc);
            gbc.gridy = 1; panel.add(name, gbc);
            gbc.gridy = 2; panel.add(new JLabel("Category:"), gbc);
            gbc.gridy = 3; panel.add(category, gbc);
            gbc.gridy = 4; panel.add(new JLabel("Description:"), gbc);
            gbc.gridy = 5; panel.add(new JScrollPane(description), gbc);
            gbc.gridy = 6; panel.add(new JLabel("Eligibility:"), gbc);
            gbc.gridy = 7; panel.add(new JScrollPane(eligibility), gbc);
            gbc.gridy = 8; panel.add(active, gbc);
        }
    }
}