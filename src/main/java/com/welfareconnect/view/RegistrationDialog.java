package com.welfareconnect.view;

import com.welfareconnect.controller.AuthController;

import javax.swing.*;
import java.awt.*;

public class RegistrationDialog extends JDialog {
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Citizen","Officer","Admin"});
    private final JComboBox<String> subRoleCombo = new JComboBox<>(new String[]{"Student","Farmer","Senior Citizen","General Citizen"});
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

    public RegistrationDialog(Window owner) {
        super(owner, "Register", ModalityType.APPLICATION_MODAL);
        setSize(520, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,1,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.add(labeled("Role", roleCombo));
        form.add(labeled("Sub-Role", subRoleCombo));
        form.add(labeled("Identifier", identifier));
        form.add(labeled("Name", name));
        form.add(labeled("Email", email));
        form.add(labeled("Phone", phone));
        form.add(labeled("Password", password));
        form.add(labeled("Confirm Password", confirm));
        form.add(labeled("Security Question", question));
        form.add(labeled("Security Answer", answer));

        JButton submit = new JButton("Register");
        submit.addActionListener(e -> onSubmit());

        add(form, BorderLayout.CENTER);
        add(submit, BorderLayout.SOUTH);
    }

    private JPanel labeled(String text, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void onSubmit() {
        if (!String.valueOf(password.getPassword()).equals(String.valueOf(confirm.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            AuthController ac = new AuthController();
            boolean ok = ac.register(
                    (String) roleCombo.getSelectedItem(),
                    (String) subRoleCombo.getSelectedItem(),
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
                JOptionPane.showMessageDialog(this, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
