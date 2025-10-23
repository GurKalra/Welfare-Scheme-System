package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.util.regex.Pattern;

import javax.swing.BorderFactory; // Needed for email validation
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.welfareconnect.controller.AuthController;

public class RegistrationDialog extends JDialog {
    // --- Components ---
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Citizen", "Officer", "Admin"});
    private final JComboBox<String> subRoleCombo = new JComboBox<>(new String[]{"Student", "Farmer", "Senior Citizen", "General Citizen"});
    private final JTextField identifier = new JTextField();
    private final JTextField name = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField phone = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirm = new JPasswordField();
    private final JComboBox<String> question = new JComboBox<>(new String[]{
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What city were you born in?",
            "What is your favorite teacher's name?",
            "What is the title of your favorite book?"
    });
    private final JTextField answer = new JTextField();

    // --- NEW: Panels and Error Labels for inline validation ---
    private final JPanel subRolePanel;
    private final JLabel emailError = new JLabel(" ");
    private final JLabel phoneError = new JLabel(" ");
    private final JLabel confirmError = new JLabel(" ");
    
    // --- NEW: Validation patterns ---
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");


    public RegistrationDialog(Window owner) {
        super(owner, "Register", ModalityType.APPLICATION_MODAL);
        setSize(520, 700); // Increased height for error messages
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Use BoxLayout for a form that can grow with error messages
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Set up error labels
        Font errorFont = emailError.getFont().deriveFont(10f);
        for (JLabel err : new JLabel[]{emailError, phoneError, confirmError}) {
            err.setForeground(Color.RED);
            err.setFont(errorFont);
            err.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0)); // Padding
            err.setAlignmentX(Component.LEFT_ALIGNMENT); // --- FIX --- Align label left
        }
        
        // --- Add components and error labels to the form ---
        form.add(labeled("Role", roleCombo));

        // Create a panel for the sub-role so it can be hidden
        subRolePanel = new JPanel(new BorderLayout());
        subRolePanel.add(labeled("Sub-Role", subRoleCombo), BorderLayout.CENTER);
        subRolePanel.setAlignmentX(Component.LEFT_ALIGNMENT); // --- FIX --- Align panel left
        form.add(subRolePanel);

        form.add(labeled("Identifier", identifier));
        form.add(labeled("Name", name));
        
        form.add(labeled("Email", email));
        form.add(emailError); // Add error label

        form.add(labeled("Phone", phone));
        form.add(phoneError); // Add error label

        form.add(labeled("Password", password));
        
        form.add(labeled("Confirm Password", confirm));
        form.add(confirmError); // Add error label
        
        form.add(labeled("Security Question", question));
        form.add(labeled("Security Answer", answer));
        
        // --- Add Listeners for Real-time Validation ---
        
        // Listener to hide/show sub-role combo
        roleCombo.addItemListener(e -> updateSubRoleVisibility());
        updateSubRoleVisibility(); // Call once at start
        
        // Create a reusable listener for text fields
        DocumentListener validator = new SimpleDocumentListener() {
            @Override
            public void update() {
                validateEmail();
                validatePhone();
                validatePasswords();
            }
        };

        email.getDocument().addDocumentListener(validator);
        phone.getDocument().addDocumentListener(validator);
        password.getDocument().addDocumentListener(validator);
        confirm.getDocument().addDocumentListener(validator);

        // --- Buttons ---
        JButton submit = new JButton("Register");
        submit.addActionListener(e -> onSubmit());
        
        // --- FIX: WRAPPER PANEL ---
        // Create a wrapper panel to hold the form at the TOP
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(form, BorderLayout.NORTH);

        // Add the wrapper to the scroll pane, not the form directly
        add(new JScrollPane(wrapperPanel), BorderLayout.CENTER);
        // --- END OF FIX ---
        
        add(submit, BorderLayout.SOUTH);
    }
    
    // --- NEW: Validation Methods ---

    private void updateSubRoleVisibility() {
        boolean citizen = roleCombo.getSelectedIndex() == 0;
        subRolePanel.setVisible(citizen);
    }
    
    private boolean validateEmail() {
        String emailText = email.getText().trim();
        if (emailText.isEmpty() || EMAIL_PATTERN.matcher(emailText).matches()) {
            emailError.setText(" "); // Clear error
            return true;
        } else {
            emailError.setText("Please enter a valid email address.");
            return false;
        }
    }
    
    private boolean validatePhone() {
        String phoneText = phone.getText().trim();
        if (phoneText.isEmpty() || PHONE_PATTERN.matcher(phoneText).matches()) {
            phoneError.setText(" "); // Clear error
            return true;
        } else {
            phoneError.setText("Phone number must be exactly 10 digits.");
            return false;
        }
    }
    
    private boolean validatePasswords() {
        String passText = String.valueOf(password.getPassword());
        String confirmText = String.valueOf(confirm.getPassword());
        
        if (passText.equals(confirmText)) {
            confirmError.setText(" "); // Clear error
            return true;
        } else {
            confirmError.setText("Passwords do not match.");
            return false;
        }
    }
    
    // --- Helper component method (Updated) ---
    private JPanel labeled(String text, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);

        // --- FIX ---
        // Set alignment so all 'labeled' panels line up left
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        // --- END OF FIX ---
        return p;
    }

    // --- UPDATED: Submit Method ---
    private void onSubmit() {
        // Run all validations one last time
        boolean emailOK = validateEmail();
        boolean phoneOK = validatePhone();
        boolean passOK = validatePasswords();
        
        // Check for other empty fields
        if (identifier.getText().trim().isEmpty() ||
            name.getText().trim().isEmpty() ||
            answer.getText().trim().isEmpty()) {
                
            JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if all inline validations passed
        if (!emailOK || !phoneOK || !passOK) {
            JOptionPane.showMessageDialog(this, "Please fix the errors shown in red.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // All checks passed, proceed with registration
        try {
            AuthController ac = new AuthController();
            boolean ok = ac.register(
                    (String) roleCombo.getSelectedItem(),
                    roleCombo.getSelectedIndex() == 0 ? (String) subRoleCombo.getSelectedItem() : "N/A", // Pass N/A if not citizen
                    identifier.getText().trim(),
                    name.getText().trim(),
                    email.getText().trim(),
                    phone.getText().trim(),
                    String.valueOf(password.getPassword()),
                    (String) question.getSelectedItem(),
                    answer.getText().trim()
            );
            if (ok) {
                JOptionPane.showMessageDialog(this, "Registered successfully");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. This identifier may already be in use.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- NEW: Helper interface for clean DocumentListeners ---
    @FunctionalInterface
    interface SimpleDocumentListener extends DocumentListener {
        void update();
        
        @Override
        default void insertUpdate(DocumentEvent e) { update(); }
        @Override
        default void removeUpdate(DocumentEvent e) { update(); }
        @Override
        default void changedUpdate(DocumentEvent e) { update(); }
    }
}