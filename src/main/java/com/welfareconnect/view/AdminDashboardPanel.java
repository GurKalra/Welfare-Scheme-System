package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.welfareconnect.model.ApplicationDAO;
import com.welfareconnect.model.Scheme;
import com.welfareconnect.model.SchemeDAO;

public class AdminDashboardPanel extends JPanel {
    private final DefaultListModel<Scheme> schemeModel = new DefaultListModel<>();
    private final JList<Scheme> schemeList = new JList<>(schemeModel);

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        // Schemes CRUD
        JPanel schemes = new JPanel(new BorderLayout());
        schemes.add(new JScrollPane(schemeList), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        JButton export = new JButton("Export to CSV");
        actions.add(export);
        actions.add(add); actions.add(edit); actions.add(del);
        schemes.add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        del.addActionListener(e -> onDelete());
        export.addActionListener(e -> onExport());

        // Analytics tab with date filters
        JPanel analytics = new JPanel(new BorderLayout());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField start = new JTextField(LocalDate.now().minusMonths(3).toString(), 10);
        JTextField end = new JTextField(LocalDate.now().toString(), 10);
        JButton refresh = new JButton("Refresh");
        JButton exportAnalytics = new JButton("Export Analytics CSV");
        filters.add(new JLabel("Start Date (YYYY-MM-DD)")); filters.add(start);
        filters.add(new JLabel("End Date")); filters.add(end);
        filters.add(refresh); filters.add(exportAnalytics);
        analytics.add(filters, BorderLayout.NORTH);

        DefaultPieDataset pie = new DefaultPieDataset();
        var pieChart = ChartFactory.createPieChart("Applications by Scheme", pie, true, true, false);
        ChartPanel piePanel = new ChartPanel(pieChart);

        DefaultCategoryDataset bar = new DefaultCategoryDataset();
        var barChart = ChartFactory.createBarChart("Approved vs Rejected per Month", "Month", "Count", bar);
        ChartPanel barPanel = new ChartPanel(barChart);

        JPanel charts = new JPanel(new java.awt.GridLayout(1,2));
        charts.add(piePanel);
        charts.add(barPanel);
        analytics.add(charts, BorderLayout.CENTER);

        refresh.addActionListener(e -> {
            loadAnalytics(pie, bar, start.getText().trim(), end.getText().trim());
        });
        exportAnalytics.addActionListener(e -> exportAnalyticsCsv(start.getText().trim(), end.getText().trim()));

        // initial load
        loadAnalytics(pie, bar, start.getText().trim(), end.getText().trim());

        tabs.addTab("Schemes", schemes);
        tabs.addTab("Users", new JPanel());
        tabs.addTab("Analytics", analytics);

        add(tabs, BorderLayout.CENTER);

        reloadSchemes();
    }

    private void loadAnalytics(DefaultPieDataset pie, DefaultCategoryDataset bar, String start, String end) {
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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAnalyticsCsv(String start, String end) {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        var file = fc.getSelectedFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Applications by Scheme (" + start + " to " + end + ")\n");
            fw.write("scheme,count\n");
            List<ApplicationDAO.SchemeCount> sc = new ApplicationDAO().applicationsByScheme(start, end);
            for (ApplicationDAO.SchemeCount s : sc) fw.write(s.schemeName.replace(","," ") + "," + s.count + "\n");
            fw.write("\nApproved vs Rejected per Month\n");
            fw.write("month,approved,rejected\n");
            List<ApplicationDAO.MonthlyStatus> ms = new ApplicationDAO().monthlyApprovedRejected(start, end);
            for (ApplicationDAO.MonthlyStatus m : ms) fw.write(m.yearMonth + "," + m.approved + "," + m.rejected + "\n");
            JOptionPane.showMessageDialog(this, "Exported");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadSchemes() {
        schemeModel.clear();
        try {
            List<Scheme> items = new SchemeDAO().listAll();
            for (Scheme s : items) schemeModel.addElement(s);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAdd() {
        SchemeForm form = new SchemeForm();
        int res = JOptionPane.showConfirmDialog(this, form.panel, "Add Scheme", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            int id = new SchemeDAO().create(form.name.getText().trim(), form.category.getText().trim(), form.description.getText(), form.eligibility.getText(), form.active.isSelected());
            if (id > 0) reloadSchemes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        Scheme selected = schemeList.getSelectedValue();
        if (selected == null) { JOptionPane.showMessageDialog(this, "Select a scheme"); return; }
        SchemeForm form = new SchemeForm();
        form.name.setText(selected.getName());
        form.category.setText(selected.getCategory());
        form.description.setText(selected.getDescription());
        form.eligibility.setText(selected.getEligibility());
        form.active.setSelected(selected.isActive());
        int res = JOptionPane.showConfirmDialog(this, form.panel, "Edit Scheme", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            boolean ok = new SchemeDAO().update(selected.getId(), form.name.getText().trim(), form.category.getText().trim(), form.description.getText(), form.eligibility.getText(), form.active.isSelected());
            if (ok) reloadSchemes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        Scheme selected = schemeList.getSelectedValue();
        if (selected == null) { JOptionPane.showMessageDialog(this, "Select a scheme"); return; }
        int res = JOptionPane.showConfirmDialog(this, "Delete \"" + selected.getName() + "\"?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            boolean ok = new SchemeDAO().delete(selected.getId());
            if (ok) reloadSchemes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExport() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        var file = fc.getSelectedFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("id,name,category,active\n");
            for (int i = 0; i < schemeModel.size(); i++) {
                Scheme s = schemeModel.get(i);
                fw.write(s.getId() + "," + s.getName().replace(","," ") + "," + s.getCategory() + "," + (s.isActive()?"1":"0") + "\n");
            }
            JOptionPane.showMessageDialog(this, "Exported");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class SchemeForm {
        final JPanel panel = new JPanel(new BorderLayout());
        final JTextField name = new JTextField();
        final JTextField category = new JTextField();
        final JTextArea description = new JTextArea(5, 30);
        final JTextArea eligibility = new JTextArea(4, 30);
        final JCheckBox active = new JCheckBox("Active", true);

        SchemeForm() {
            JPanel fields = new JPanel(new java.awt.GridLayout(0,1,6,6));
            fields.add(labeled("Name", name));
            fields.add(labeled("Category", category));
            fields.add(labeled("Description", new JScrollPane(description)));
            fields.add(labeled("Eligibility", new JScrollPane(eligibility)));
            fields.add(active);
            panel.add(fields, BorderLayout.CENTER);
        }

        private JPanel labeled(String label, java.awt.Component c) {
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JLabel(label), BorderLayout.NORTH);
            p.add(c, BorderLayout.CENTER);
            return p;
        }
    }
}
