package com.welfareconnect.view.customcomponents;

// IMPORT STATEMENTS ADDED HERE
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import com.welfareconnect.model.Scheme;

public class SchemeCellRenderer implements ListCellRenderer<Scheme> {
    private final SchemeCardPanel cardPanel = new SchemeCardPanel();

    @Override
    public Component getListCellRendererComponent(JList<? extends Scheme> list, Scheme scheme, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        cardPanel.setSchemeData(scheme);
        if (isSelected) {
            cardPanel.setBackground(new Color(220, 235, 255));
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 120, 215), 2, true),
                    new EmptyBorder(9, 14, 9, 14)
            ));
        } else {
            cardPanel.setBackground(Color.WHITE);
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                    new EmptyBorder(10, 15, 10, 15)
            ));
        }
        return cardPanel;
    }
}