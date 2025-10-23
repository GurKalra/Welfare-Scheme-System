package com.welfareconnect.view.customcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor; // Import
import java.awt.Font;
import java.awt.event.MouseAdapter; // Import
import java.awt.event.MouseEvent; // Import

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager; // Import
import javax.swing.border.Border; // Import
import javax.swing.border.EmptyBorder;

import com.welfareconnect.model.Scheme;

public class SchemeCardPanel extends JPanel {
    private JLabel schemeTitleLabel;
    private JTextArea schemeDescriptionArea;
    private JLabel categoryLabel;

    // Store original and hover borders
    private final Border originalBorder;
    private final Border hoverBorder;

    public SchemeCardPanel() {
        setLayout(new BorderLayout(10, 5));
        
        // --- UPDATED BORDERS ---
        EmptyBorder padding = new EmptyBorder(10, 15, 10, 15);
        originalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                padding
        );
        // Use a theme-aware color for the hover
        hoverBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.focusColor"), 1, true),
                padding
        );

        setBorder(originalBorder);
        setBackground(Color.WHITE);
        
        // --- ADDED HOVER LISTENER ---
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(hoverBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(originalBorder);
            }
        });
        // --- END OF UPDATES ---


        schemeTitleLabel = new JLabel("Scheme Title Here");
        schemeTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        schemeDescriptionArea = new JTextArea("A brief description of the scheme's benefits will appear here.");
        schemeDescriptionArea.setWrapStyleWord(true);
        schemeDescriptionArea.setLineWrap(true);
        schemeDescriptionArea.setOpaque(false);
        schemeDescriptionArea.setEditable(false);
        schemeDescriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        schemeDescriptionArea.setForeground(Color.DARK_GRAY);

        categoryLabel = new JLabel("Category");
        categoryLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        categoryLabel.setForeground(Color.GRAY);

        add(schemeTitleLabel, BorderLayout.NORTH);
        add(schemeDescriptionArea, BorderLayout.CENTER);
        add(categoryLabel, BorderLayout.SOUTH);
    }

    public void setSchemeData(Scheme scheme) {
        schemeTitleLabel.setText(scheme.getName());
        String description = scheme.getDescription();
        if (description.length() > 120) {
            description = description.substring(0, 120) + "...";
        }
        schemeDescriptionArea.setText(description);
        categoryLabel.setText("Category: " + scheme.getCategory());
    }
}