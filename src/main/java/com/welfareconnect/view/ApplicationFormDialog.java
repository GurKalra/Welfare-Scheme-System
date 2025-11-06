package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.welfareconnect.model.ApplicationDAO;
import com.welfareconnect.model.DocumentDAO;
import com.welfareconnect.model.Scheme;
import com.welfareconnect.util.FileStorage;

public class ApplicationFormDialog extends JDialog {
    private final Scheme scheme;
    private final int userId;
    private final DefaultListModel<String> uploadedFilesModel = new DefaultListModel<>();
    private final JList<String> uploadedFilesList = new JList<>(uploadedFilesModel);
    private final List<File> selectedFiles = new ArrayList<>();
    private boolean submitted = false;

    public ApplicationFormDialog(Window parent, Scheme scheme, int userId) {
        super(parent, "Apply for Scheme", ModalityType.APPLICATION_MODAL);
        this.scheme = scheme;
        this.userId = userId;
        
        setLayout(new BorderLayout(10, 10));
        setSize(600, 500);
        setLocationRelativeTo(parent);
        
        initComponents();
    }

    private void initComponents() {
        // Main panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Scheme name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel schemeLabel = new JLabel("Scheme: " + scheme.getName());
        schemeLabel.setFont(schemeLabel.getFont().deriveFont(16f).deriveFont(java.awt.Font.BOLD));
        mainPanel.add(schemeLabel, gbc);

        // Category
        gbc.gridy = 1;
        JLabel categoryLabel = new JLabel("Category: " + scheme.getCategory());
        mainPanel.add(categoryLabel, gbc);

        // Separator
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 5, 10, 5);
        JLabel instructionsLabel = new JLabel("Required Documents:");
        instructionsLabel.setFont(instructionsLabel.getFont().deriveFont(java.awt.Font.BOLD));
        mainPanel.add(instructionsLabel, gbc);

        // Document requirements text area
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        JTextArea requirementsArea = new JTextArea();
        requirementsArea.setEditable(false);
        requirementsArea.setLineWrap(true);
        requirementsArea.setWrapStyleWord(true);
        requirementsArea.setText(getDocumentRequirements());
        requirementsArea.setBackground(getBackground());
        JScrollPane requirementsScroll = new JScrollPane(requirementsArea);
        requirementsScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        mainPanel.add(requirementsScroll, gbc);

        // Upload section label
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel uploadLabel = new JLabel("Upload Documents:");
        uploadLabel.setFont(uploadLabel.getFont().deriveFont(java.awt.Font.BOLD));
        mainPanel.add(uploadLabel, gbc);

        // File list
        gbc.gridy = 5;
        gbc.weighty = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        uploadedFilesList.setBorder(BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
        JScrollPane filesScroll = new JScrollPane(uploadedFilesList);
        mainPanel.add(filesScroll, gbc);

        // Upload button
        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addFilesBtn = new JButton("Add Files");
        addFilesBtn.addActionListener(e -> addFiles());
        mainPanel.add(addFilesBtn, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton submitBtn = new JButton("Submit Application");
        JButton cancelBtn = new JButton("Cancel");
        
        submitBtn.addActionListener(e -> submitApplication());
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(submitBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private int getRequiredDocumentCount() {
        // All categories require 4 documents
        return 4;
    }

    private String getDocumentRequirements() {
        // Generate document requirements based on scheme category
        StringBuilder requirements = new StringBuilder();
        requirements.append("Please upload the following documents:\n\n");
        
        switch (scheme.getCategory().toLowerCase()) {
            case "education":
                requirements.append("• Student ID Card or Enrollment Certificate\n");
                requirements.append("• Previous Year's Mark Sheet\n");
                requirements.append("• Income Certificate\n");
                requirements.append("• Passport Size Photograph\n");
                break;
            case "agriculture":
                requirements.append("• Land Ownership Documents\n");
                requirements.append("• Farmer ID Card\n");
                requirements.append("• Bank Account Details\n");
                requirements.append("• Passport Size Photograph\n");
                break;
            case "senior":
                requirements.append("• Age Proof (Birth Certificate/Aadhaar)\n");
                requirements.append("• Identity Proof\n");
                requirements.append("• Address Proof\n");
                requirements.append("• Passport Size Photograph\n");
                break;
            case "general":
                requirements.append("• Identity Proof (Aadhaar/PAN/Voter ID)\n");
                requirements.append("• Address Proof\n");
                requirements.append("• Income Certificate\n");
                requirements.append("• Passport Size Photograph\n");
                break;
            default:
                requirements.append("• Identity Proof\n");
                requirements.append("• Address Proof\n");
                requirements.append("• Supporting Documents\n");
                requirements.append("• Passport Size Photograph\n");
        }
        
        requirements.append("\nNote: All documents should be in PDF, JPG, or PNG format.");
        
        return requirements.toString();
    }

    private void addFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".pdf") || name.endsWith(".jpg") || 
                       name.endsWith(".jpeg") || name.endsWith(".png");
            }
            
            @Override
            public String getDescription() {
                return "Documents (*.pdf, *.jpg, *.jpeg, *.png)";
            }
        });
        
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File file : fc.getSelectedFiles()) {
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                    uploadedFilesModel.addElement(file.getName());
                }
            }
        }
    }

    private void submitApplication() {
        // Get required document count based on category
        int requiredDocCount = getRequiredDocumentCount();
        
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please upload at least one document before submitting.", 
                "No Documents", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate document count
        if (selectedFiles.size() < requiredDocCount) {
            JOptionPane.showMessageDialog(this, 
                "Please upload all required documents.\n\n" +
                "Required: " + requiredDocCount + " documents\n" +
                "Uploaded: " + selectedFiles.size() + " document(s)\n\n" +
                "Check the 'Required Documents' section above for the complete list.", 
                "Incomplete Documents", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Submit application for: " + scheme.getName() + "\nwith " + selectedFiles.size() + " document(s)?", 
            "Confirm Submission", 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (confirm != JOptionPane.OK_OPTION) return;

        try {
            // Create application
            int newAppId = new ApplicationDAO().create(userId, scheme.getId());
            if (newAppId == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to submit application", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Store files and create document records
            for (File f : selectedFiles) {
                String stored = FileStorage.store(f);
                String ct = Files.probeContentType(Path.of(stored));
                new DocumentDAO().add(newAppId, f.getName(), ct, stored);
            }

            submitted = true;
            JOptionPane.showMessageDialog(this, 
                "Your application has been submitted successfully! 🌱", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error during application: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean wasSubmitted() {
        return submitted;
    }
}
