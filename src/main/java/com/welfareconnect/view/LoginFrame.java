package com.welfareconnect.view;

import com.formdev.flatlaf.ui.FlatRoundBorder;
import com.welfareconnect.controller.AuthController;
import com.welfareconnect.util.I18n;
import net.miginfocom.swing.MigLayout; // You will need to add the MigLayout library

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{I18n.t("login.citizen"), I18n.t("login.officer"), I18n.t("login.admin")});
    private final JComboBox<String> subRoleCombo = new JComboBox<>(new String[]{I18n.t("login.student"), I18n.t("login.farmer"), I18n.t("login.senior"), I18n.t("login.general")});
    private final JTextField identifierField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle(I18n.t("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // The animated gradient panel remains as the background
        JPanel bg = new AnimatedGradientPanel();
        bg.setLayout(new GridBagLayout()); // Use GridBagLayout to center the card
        add(bg, BorderLayout.CENTER);

        // --- The Login Card ---
        // Using MigLayout for a cleaner and more flexible layout
        JPanel card = new JPanel(new MigLayout(
                "wrap 1, fillx, insets 25 35 25 35", // 1 column, fill horizontally, with padding
                "[center]", // Center alignment for the column
                "[]15[]10[]10[]20[]15[]" // Row constraints with specified gaps
        ));

        // Use a theme-aware background color instead of hardcoded white
        card.putClientProperty("FlatLaf.style", "background: @panel.background");
        card.setBorder(new FlatRoundBorder());
        card.setPreferredSize(new Dimension(420, 480)); // Adjusted size

        // --- Components ---
        JLabel title = new JLabel(I18n.t("login.title"), SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        card.add(title, "growx");

        JPanel roleRow = new JPanel(new GridLayout(1, 2, 12, 0));
        roleRow.setOpaque(false);
        roleRow.add(labeled(I18n.t("login.role"), roleCombo));
        roleRow.add(labeled(I18n.t("login.subrole"), subRoleCombo));
        card.add(roleRow, "growx");

        card.add(labeled(I18n.t("login.identifier"), identifierField), "growx");
        card.add(labeled(I18n.t("login.password"), passwordField), "growx");

        JButton signIn = new JButton(I18n.t("login.signin"));
        signIn.putClientProperty("JButton.buttonType", "roundRect"); // A nice FlatLaf style
        card.add(signIn, "growx, h 40!"); // Make button taller

        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        links.setOpaque(false);
        JLabel register = linkLabel(I18n.t("login.register"));
        JLabel forgot = linkLabel(I18n.t("login.forgot"));
        links.add(register);
        links.add(forgot);
        card.add(links, "growx");

        bg.add(card, new GridBagConstraints()); // Add card to the centered background

        // --- Action Listeners ---
        roleCombo.addActionListener(e -> updateSubroleVisibility());
        subRoleCombo.addActionListener(e -> updateIdentifierPlaceholder());
        updateSubroleVisibility();
        updateIdentifierPlaceholder();

        signIn.addActionListener(e -> onLogin());
        register.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onRegister();
            }
        });
        forgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onForgot();
            }
        });
    }

    private JPanel labeled(String text, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4)); // Reduced gap
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        p.add(l, BorderLayout.NORTH);
        comp.putClientProperty("JComponent.roundRect", true);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JLabel linkLabel(String text) {
        JLabel l = new JLabel("<html><u>" + text + "</u></html>");
        l.setForeground(new Color(25, 118, 210));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    private void updateSubroleVisibility() {
        boolean citizen = roleCombo.getSelectedIndex() == 0;
        subRoleCombo.setEnabled(citizen);
    }

    private void updateIdentifierPlaceholder() {
        String hint;
        int idx = subRoleCombo.getSelectedIndex();
        if (idx == 0) hint = "Student ID";
        else if (idx == 1) hint = "Farmer Permit Number";
        else if (idx == 2) hint = "Senior Citizen ID";
        else hint = "Aadhaar Number";
        identifierField.putClientProperty("JTextField.placeholderText", hint);
    }

    private void onLogin() {
        try {
            AuthController ac = new AuthController();
            String role = mapRole(roleCombo.getSelectedIndex());
            String identifier = identifierField.getText().trim();
            String password = String.valueOf(passwordField.getPassword());
            boolean ok = ac.login(role, identifier, password);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String subRole = subRoleCombo.isEnabled() ? (String) subRoleCombo.getSelectedItem() : null;
            new AppFrame(role, subRole, identifier).setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String mapRole(int idx) {
        if (idx == 0) return "Citizen";
        if (idx == 1) return "Officer";
        return "Admin";
    }

    private void onRegister() {
        new RegistrationDialog(this).setVisible(true);
    }

    private void onForgot() {
        new ForgotPasswordDialog(this).setVisible(true);
    }

    static class AnimatedGradientPanel extends JPanel {
        private float t = 0f;
        private final Timer timer;

        AnimatedGradientPanel() {
            timer = new Timer(30, e -> {
                t += 0.002f; // Slow down the animation
                if (t > 1.0f) {
                    t = 0f;
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            // More subtle color choices
            Color color1 = new Color(225, 245, 254); // Light Blue
            Color color2 = new Color(232, 234, 246); // Light Indigo
            Color color3 = new Color(237, 231, 246); // Light Purple
            Color color4 = new Color(225, 245, 254); // Back to Light Blue

            Color c1, c2;
            float p = t * 2;
            if (p < 1) {
                c1 = blend(color1, color2, p);
                c2 = blend(color2, color3, p);
            } else {
                p -= 1;
                c1 = blend(color2, color3, p);
                c2 = blend(color3, color4, p);
            }

            GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }

        private Color blend(Color a, Color b, float ratio) {
            ratio = Math.max(0f, Math.min(1f, ratio));
            int r = (int) (a.getRed() * (1 - ratio) + b.getRed() * ratio);
            int g = (int) (a.getGreen() * (1 - ratio) + b.getGreen() * ratio);
            int bl = (int) (a.getBlue() * (1 - ratio) + b.getBlue() * ratio);
            return new Color(r, g, bl);
        }
    }
}