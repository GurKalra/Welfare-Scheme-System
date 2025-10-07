package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.welfareconnect.model.Application;
import com.welfareconnect.model.ApplicationDAO;

public class OfficerDashboardPanel extends JPanel {
    private final DefaultListModel<Application> queueModel = new DefaultListModel<>();
    private final JList<Application> queueList = new JList<>(queueModel);

    public OfficerDashboardPanel() {
        setLayout(new BorderLayout());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        split.setLeftComponent(new JScrollPane(queueList));

        JPanel review = new JPanel(new BorderLayout());
        review.add(new JLabel("Review Panel"), BorderLayout.NORTH);
        JTextArea details = new JTextArea(12, 40);
        review.add(new JScrollPane(details), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton moreInfo = new JButton("Request Info");
        JButton openDocs = new JButton("Open Documents");
        actions.add(openDocs);
        actions.add(approve);
        actions.add(reject);
        actions.add(moreInfo);
        review.add(actions, BorderLayout.SOUTH);

        split.setRightComponent(review);
        split.setDividerLocation(380);
        add(split, BorderLayout.CENTER);

        // Load queue
        reloadQueue();

        queueList.addListSelectionListener(e -> {
            Application a = queueList.getSelectedValue();
            if (a == null) return;
            details.setText("Application #" + a.getId() + "\nScheme: " + a.getSchemeName() + "\nStatus: " + a.getStatus() + "\nUpdated: " + a.getUpdatedAt());
        });

        approve.addActionListener(e -> updateSelected(details, "Approved", null));
        reject.addActionListener(e -> {
            String reason = JOptionPane.showInputDialog(this, "Reason for rejection:");
            if (reason != null) updateSelected(details, "Rejected", reason);
        });
        moreInfo.addActionListener(e -> {
            String msg = JOptionPane.showInputDialog(this, "Request more info message:");
            if (msg != null) updateSelected(details, "More Info Required", msg);
        });
        openDocs.addActionListener(e -> openSelectedDocs());
    }

    private void reloadQueue() {
        queueModel.clear();
        try {
            List<Application> items = new ApplicationDAO().listPending();
            for (Application a : items) queueModel.addElement(a);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelected(JTextArea details, String status, String reason) {
        Application a = queueList.getSelectedValue();
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Select an application first");
            return;
        }
        try {
            boolean ok = new ApplicationDAO().updateStatus(a.getId(), status, reason);
            if (ok) {
                details.setText("Updated status to: " + status + (reason != null ? ("\nReason: " + reason) : ""));
                reloadQueue();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedDocs() {
        Application a = queueList.getSelectedValue();
        if (a == null) { JOptionPane.showMessageDialog(this, "Select an application first"); return; }
        try {
            java.util.List<String> paths = new com.welfareconnect.model.DocumentDAO().listPathsByApplication(a.getId());
            if (paths.isEmpty()) { JOptionPane.showMessageDialog(this, "No documents uploaded"); return; }
            for (String p : paths) {
                try {
                    Desktop.getDesktop().open(new File(p));
                } catch (Exception ex) {
                    // continue opening others
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
