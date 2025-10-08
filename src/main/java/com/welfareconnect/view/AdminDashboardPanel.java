package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox; // <--- THIS IS THE MISSING IMPORT
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.welfareconnect.model.ApplicationDAO;
import com.welfareconnect.model.Scheme;
import com.welfareconnect.model.SchemeDAO;

public class AdminDashboardPanel extends JPanel {
    // ... the rest of the file is exactly the same as the previous version ...
    private final DefaultTableModel schemeModel = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Active"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable schemeTable = new JTable(schemeModel);

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(5,5,5,5));

        // --- Schemes CRUD Tab ---
        JPanel schemes = new JPanel(new BorderLayout(0, 10));
        schemes.add(new JScrollPane(schemeTable), BorderLayout.CENTER);
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        JButton export = new JButton("Export to CSV");
        actions.add(export);
        actions.add(add);
        actions.add(edit);
        actions.add(del);
        schemes.add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        del.addActionListener(e -> onDelete());
        export.addActionListener(e -> onExport());

        // --- Analytics Tab ---
        JPanel analytics = new JPanel(new BorderLayout());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField start = new JTextField(LocalDate.now().minusMonths(3).toString(), 10);
        JTextField end = new JTextField(LocalDate.now().toString(), 10);
        JButton refresh = new JButton("Refresh");
        filters.add(new JLabel("Start Date (YYYY-MM-DD)")); filters.add(start);
        filters.add(new JLabel("End Date")); filters.add(end);
        filters.add(refresh);
        analytics.add(filters, BorderLayout.NORTH);

        DefaultPieDataset pieData = new DefaultPieDataset();
        var pieChart = ChartFactory.createPieChart("Applications by Scheme", pieData, true, true, false);
        ((PiePlot) pieChart.getPlot()).setSimpleLabels(true);
        ChartPanel piePanel = new ChartPanel(pieChart);

        DefaultCategoryDataset barData = new DefaultCategoryDataset();
        var barChart = ChartFactory.createBarChart("Approved vs Rejected", "Month", "Count", barData);
        ChartPanel barPanel = new ChartPanel(barChart);
        
        piePanel.setLayout(new GridBagLayout());
        barPanel.setLayout(new GridBagLayout());
        
        JPanel charts = new JPanel(new GridLayout(1, 2, 10, 0));
        charts.add(piePanel);
        charts.add(barPanel);
        analytics.add(charts, BorderLayout.CENTER);

        refresh.addActionListener(e -> loadAnalytics(pieData, barData, start.getText().trim(), end.getText().trim(), piePanel, barPanel));
        loadAnalytics(pieData, barData, start.getText().trim(), end.getText().trim(), piePanel, barPanel);

        tabs.addTab("Schemes", schemes);
        tabs.addTab("Users", new JPanel());
        tabs.addTab("Analytics", analytics);

        add(tabs, BorderLayout.CENTER);
        reloadSchemes();
    }
    
    private void loadAnalytics(DefaultPieDataset pie, DefaultCategoryDataset bar, String start, String end, ChartPanel... panels) {
        try {
            pie.clear();
            List<ApplicationDAO.SchemeCount> sc = new ApplicationDAO().applicationsByScheme(start, end);
            for (ApplicationDAO.SchemeCount s : sc) pie.setValue(s.schemeName, s.count);

            bar.clear();
            List<ApplicationDAO.MonthlyStatus> ms = new ApplicationDAO().monthlyApprovedRejected(start, end);
            for (ApplicationDAO.MonthlyStatus m : ms) {
                bar.addValue(m.approved, "Approved", m.yearMonth);
                bar.addValue(m.rejected, "Rejected", m.yearMonth);
            }

            for(ChartPanel p : panels) {
                p.removeAll();
                boolean isPieEmpty = p.getChart().getPlot() instanceof PiePlot && pie.getItemCount() == 0;
                boolean isBarEmpty = !(p.getChart().getPlot() instanceof PiePlot) && bar.getColumnCount() == 0;
                if (isPieEmpty || isBarEmpty) {
                     p.add(new JLabel("No data for the selected period."));
                }
                p.revalidate();
                p.repaint();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
    
    private void onExport() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("id,name,category,active\n");
            for (int i = 0; i < schemeModel.getRowCount(); i++) {
                fw.write(schemeModel.getValueAt(i,0) + "," + 
                         ((String)schemeModel.getValueAt(i,1)).replace(","," ") + "," + 
                         schemeModel.getValueAt(i,2) + "," + 
                         ((Boolean)schemeModel.getValueAt(i,3)?"1":"0") + "\n");
            }
            JOptionPane.showMessageDialog(this, "Exported");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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