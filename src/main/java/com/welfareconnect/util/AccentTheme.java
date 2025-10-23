package com.welfareconnect.util;

import java.awt.Color;

import com.formdev.flatlaf.FlatLaf;

public class AccentTheme {

    public static final Color BLUE = new Color(0, 120, 215);
    public static final Color GREEN = new Color(16, 124, 16);
    public static final Color RED = new Color(196, 43, 43);

    private final Color accentColor;

    public AccentTheme(Color accentColor) {
        this.accentColor = accentColor;
    }

    public void apply() {
        FlatLaf.setGlobalExtraDefaults(java.util.Map.of(
            "@accentColor", toHex(accentColor),
            "@accentColor.hover", toHex(accentColor.brighter()),
            "@accentColor.pressed", toHex(accentColor.darker())
        ));
        
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}