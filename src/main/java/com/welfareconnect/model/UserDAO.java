package com.welfareconnect.model;

import com.welfareconnect.util.Database;

import java.sql.*;

public class UserDAO {
    public Integer findIdByIdentifier(String identifier) throws SQLException {
        String sql = "SELECT id FROM users WHERE identifier=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return null;
            }
        }
    }
}
