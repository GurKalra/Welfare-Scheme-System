package com.welfareconnect.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.welfareconnect.util.Database;

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

    public User findByIdentifier(String identifier) throws SQLException {
        String sql = "SELECT id, name, identifier, email, phone, role, active, sub_role, region, annualIncome FROM users WHERE identifier=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("identifier"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getInt("active") == 1
                    );
                    user.setSubRole(rs.getString("sub_role"));
                    user.setRegion(rs.getString("region"));
                    user.setAnnualIncome(rs.getString("annualIncome"));
                    return user;
                }
                return null;
            }
        }
    }

    public boolean updateProfile(String identifier, String email, String phone, String region, String annualIncome) throws SQLException {
        String sql = "UPDATE users SET email = ?, phone = ?, region = ?, annualIncome = ? WHERE identifier = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setString(3, region);
            ps.setString(4, annualIncome);
            ps.setString(5, identifier);
            return ps.executeUpdate() == 1;
        }
    }
    
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, identifier, email, phone, role, active FROM users ORDER BY name";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("identifier"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("role"),
                    rs.getInt("active") == 1
                ));
            }
        }
        return users;
    }

    public boolean updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateUserStatus(int userId, boolean isActive) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, isActive ? 1 : 0);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }
}