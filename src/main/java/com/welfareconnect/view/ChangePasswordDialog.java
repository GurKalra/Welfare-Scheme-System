package com.welfareconnect.view;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.welfareconnect.controller.AuthController;

public class ChangePasswordDialog extends JDialog {
    private final JTextField identifierDisplay = new JTextField();
    private final JPasswordField current = new JPasswordField();
    private final JPasswordField next = new JPasswordField();
    private final JPasswordField confirm = new JPasswordField();

    public ChangePasswordDialog(Window owner, String identifier) {
        super(owner, "Change Password", ModalityType.APPLICATION_MODAL);
        setSize(420, 260);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        identifierDisplay.setText(identifier);
        identifierDisplay.setEditable(false);

        JPanel form = new JPanel(new java.awt.GridLayout(0,1,6,6));
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(12,12,12,12));
        form.add(labeled("Identifier", identifierDisplay));
        form.add(labeled("Current Password", current));
        form.add(labeled("New Password", next));
        form.add(labeled("Confirm New Password", confirm));

        JButton save = new JButton("Update Password");
        save.addActionListener(e -> onSave());

        add(form, BorderLayout.CENTER);
        add(save, BorderLayout.SOUTH);
    }

    private JPanel labeled(String text, java.awt.Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void onSave() {
        String cur = new String(current.getPassword());
        String n1 = new String(next.getPassword());
        String n2 = new String(confirm.getPassword());
        if (!n1.equals(n2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            boolean ok = new AuthController().changePassword(identifierDisplay.getText(), cur, n1);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password updated");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Current password incorrect", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
