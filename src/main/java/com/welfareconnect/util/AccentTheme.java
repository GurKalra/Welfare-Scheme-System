package com.welfareconnect.util;

import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;

import javax.swing.UIManager;

public class AccentTheme {

    public static final Color BLUE = new Color(0, 120, 215);
    public static final Color GREEN = new Color(16, 124, 16);
    public static final Color RED = new Color(196, 43, 43);

    private final Color accentColor;

    public AccentTheme(Color accentColor) {
        this.accentColor = accentColor;
    }

    public void apply() {
        // These are special keys used by the FlatLaf library to style components.
        // We are overriding the default values with our chosen accent color.
        FlatLaf.setGlobalExtraDefaults(java.util.Map.of(
            "@accentColor", toHex(accentColor),
            "@accentColor.hover", toHex(accentColor.brighter()),
            "@accentColor.pressed", toHex(accentColor.darker())
        ));
        
        // You can add more specific overrides here if needed, for example:
        // UIManager.put("Button.background", accentColor);
        // UIManager.put("ProgressBar.foreground", accentColor);
        // UIManager.put("Component.focusColor", accentColor);
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}