package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.welfareconnect.model.Application;
import com.welfareconnect.model.ApplicationDAO;
import com.welfareconnect.model.Document;
import com.welfareconnect.model.DocumentDAO;

public class OfficerDashboardPanel extends JPanel {
    // ... (rest of the file is unchanged) ...
    private final DefaultTableModel queueModel = new DefaultTableModel(new String[]{"App ID", "Applicant", "Scheme", "Submitted"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable queueTable = new JTable(queueModel);

    private final JLabel applicantNameLabel = new JLabel("...");
    private final JLabel applicantIdLabel = new JLabel("...");
    private final JLabel schemeDetailsLabel = new JLabel("...");
    private final DefaultListModel<Document> documentListModel = new DefaultListModel<>();
    private final JList<Document> documentList = new JList<>(documentListModel);
    
    private final JButton approveButton = new JButton("Approve");
    private final JButton rejectButton = new JButton("Reject");
    private final JButton moreInfoButton = new JButton("Request Info");
    private final JButton openDocButton = new JButton("View Document");

    public OfficerDashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.add(new JScrollPane(queueTable), BorderLayout.CENTER);
        split.setLeftComponent(queuePanel);

        JPanel reviewPanel = new JPanel(new BorderLayout(10, 10));
        reviewPanel.setBorder(new TitledBorder("Review Panel"));

        JPanel detailsGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; detailsGrid.add(new JLabel("Applicant Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; detailsGrid.add(applicantNameLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 1; detailsGrid.add(new JLabel("Applicant ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; detailsGrid.add(applicantIdLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 2; detailsGrid.add(new JLabel("Scheme:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; detailsGrid.add(schemeDetailsLabel, gbc);

        JPanel docPanel = new JPanel(new BorderLayout(0, 5));
        docPanel.setBorder(new TitledBorder("Submitted Documents"));
        docPanel.add(new JScrollPane(documentList), BorderLayout.CENTER);
        docPanel.add(openDocButton, BorderLayout.SOUTH);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        detailsGrid.add(docPanel, gbc);
        
        reviewPanel.add(detailsGrid, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(approveButton);
        actions.add(rejectButton);
        actions.add(moreInfoButton);
        reviewPanel.add(actions, BorderLayout.SOUTH);

        split.setRightComponent(reviewPanel);
        split.setDividerLocation(380);
        add(split, BorderLayout.CENTER);

        setActionsEnabled(false);
        reloadQueue();

        queueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateReviewPanel();
            }
        });

        approveButton.addActionListener(e -> updateSelectedStatus("Approved", null));
        rejectButton.addActionListener(e -> {
            String reason = JOptionPane.showInputDialog(this, "Reason for rejection:");
            if (reason != null && !reason.trim().isEmpty()) updateSelectedStatus("Rejected", reason);
        });
        moreInfoButton.addActionListener(e -> {
            String msg = JOptionPane.showInputDialog(this, "Message to citizen requesting more info:");
            if (msg != null && !msg.trim().isEmpty()) updateSelectedStatus("More Info Required", msg);
        });
        openDocButton.addActionListener(e -> openSelectedDoc());
    }
    
    private void setActionsEnabled(boolean enabled) {
        approveButton.setEnabled(enabled);
        rejectButton.setEnabled(enabled);
        moreInfoButton.setEnabled(enabled);
        openDocButton.setEnabled(enabled);
    }

    private void populateReviewPanel() {
        int selectedRow = queueTable.getSelectedRow();
        if (selectedRow == -1) {
            clearReviewPanel();
            setActionsEnabled(false);
            return;
        }

        int appId = (int) queueModel.getValueAt(selectedRow, 0);
        try {
            ApplicationDAO appDAO = new ApplicationDAO();
            Application app = appDAO.findById(appId);
            if (app == null) return;

            applicantNameLabel.setText(app.getApplicantName());
            applicantIdLabel.setText(app.getApplicantIdentifier());
            schemeDetailsLabel.setText(app.getSchemeName());

            documentListModel.clear();
            List<Document> docs = new DocumentDAO().listByApplication(appId);
            for(Document doc : docs) documentListModel.addElement(doc);
            
            setActionsEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearReviewPanel() {
        applicantNameLabel.setText("...");
        applicantIdLabel.setText("...");
        schemeDetailsLabel.setText("...");
        documentListModel.clear();
    }

    private void reloadQueue() {
        queueModel.setRowCount(0);
        clearReviewPanel();
        try {
            List<Application> items = new ApplicationDAO().listPending();
            for (Application a : items) {
                queueModel.addRow(new Object[]{a.getId(), a.getApplicantName(), a.getSchemeName(), a.getUpdatedAt()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedStatus(String status, String reason) {
        int selectedRow = queueTable.getSelectedRow();
        if (selectedRow == -1) return;
        int appId = (int) queueModel.getValueAt(selectedRow, 0);

        try {
            boolean ok = new ApplicationDAO().updateStatus(appId, status, reason);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Application status updated to: " + status);
                reloadQueue();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedDoc() {
        Document selectedDoc = documentList.getSelectedValue();
        if (selectedDoc == null) {
            JOptionPane.showMessageDialog(this, "Select a document from the list first.");
            return;
        }
        try {
            // Updated to use getFilePath()
            Desktop.getDesktop().open(new File(selectedDoc.getFilePath()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}