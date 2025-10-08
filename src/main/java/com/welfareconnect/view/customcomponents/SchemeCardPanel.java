package com.welfareconnect.view.customcomponents;

// IMPORT STATEMENTS ADDED HERE
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.welfareconnect.model.Scheme;

public class SchemeCardPanel extends JPanel {
    private JLabel schemeTitleLabel;
    private JTextArea schemeDescriptionArea;
    private JLabel categoryLabel;

    public SchemeCardPanel() {
        setLayout(new BorderLayout(10, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));
        setBackground(Color.WHITE);

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