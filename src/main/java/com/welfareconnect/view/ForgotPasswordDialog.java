package com.welfareconnect.view;

import com.welfareconnect.controller.AuthController;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordDialog extends JDialog {
    private final JTextField identifier = new JTextField();
    private final JLabel questionLabel = new JLabel(" ");
    private final JTextField answer = new JTextField();
    private final JPasswordField newPass = new JPasswordField();
    private final JPasswordField confirm = new JPasswordField();

    public ForgotPasswordDialog(Window owner) {
        super(owner, "Forgot Password", ModalityType.APPLICATION_MODAL);
        setSize(480, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,1,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.add(labeled("Identifier", identifier));
        JButton fetch = new JButton("Fetch Security Question");
        fetch.addActionListener(e -> fetchQuestion());
        form.add(fetch);
        form.add(labeled("Security Question", questionLabel));
        form.add(labeled("Your Answer", answer));
        form.add(labeled("New Password", newPass));
        form.add(labeled("Confirm Password", confirm));

        JButton reset = new JButton("Reset Password");
        reset.addActionListener(e -> onReset());

        add(form, BorderLayout.CENTER);
        add(reset, BorderLayout.SOUTH);
    }

    private JPanel labeled(String text, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void fetchQuestion() {
        try {
            AuthController ac = new AuthController();
            String q = ac.fetchSecurityQuestion(identifier.getText().trim());
            if (q == null) {
                JOptionPane.showMessageDialog(this, "Identifier not found", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                questionLabel.setText(q);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReset() {
        if (!String.valueOf(newPass.getPassword()).equals(String.valueOf(confirm.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            AuthController ac = new AuthController();
            boolean ok = ac.resetPassword(
                    identifier.getText().trim(),
                    answer.getText().trim(),
                    String.valueOf(newPass.getPassword())
            );
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password updated");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
