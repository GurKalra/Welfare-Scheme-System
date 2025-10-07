package com.welfareconnect;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            try { com.welfareconnect.util.Database.initialize(); } catch (Exception ex) { ex.printStackTrace(); }
            try { new com.welfareconnect.model.SchemeDAO().seedIfEmpty(); } catch (Exception ex) { ex.printStackTrace(); }
            new com.welfareconnect.view.LoginFrame().setVisible(true);
        });
    }
}
