package com.welfareconnect.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.welfareconnect.util.Database;
import com.welfareconnect.util.PasswordUtil;

public class AuthController {
    public AuthController() {}

    public boolean register(String role, String subRole, String identifier, String name,
                            String email, String phone, String password,
                            String securityQuestion, String securityAnswer) throws SQLException {
        String sql = "INSERT INTO users(role, sub_role, identifier, name, email, phone, password_hash, security_question, security_answer_hash, active) " +
                "VALUES(?,?,?,?,?,?,?,?,?,1)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, subRole);
            ps.setString(3, identifier);
            ps.setString(4, name);
            ps.setString(5, email);
            ps.setString(6, phone);
            ps.setString(7, PasswordUtil.hash(password));
            ps.setString(8, securityQuestion);
            ps.setString(9, PasswordUtil.hash(securityAnswer));
            return ps.executeUpdate() == 1;
        }
    }

    public boolean login(String role, String identifier, String password) throws SQLException {
        String sql = "SELECT password_hash, active FROM users WHERE role=? AND identifier=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                if (rs.getInt("active") != 1) return false;
                String hash = rs.getString("password_hash");
                return PasswordUtil.matches(password, hash);
            }
        }
    }

    public String fetchSecurityQuestion(String identifier) throws SQLException {
        String sql = "SELECT security_question FROM users WHERE identifier=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
                return null;
            }
        }
    }

    public boolean resetPassword(String identifier, String securityAnswer, String newPassword) throws SQLException {
        String check = "SELECT security_answer_hash FROM users WHERE identifier=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(check)) {
            ps.setString(1, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String expected = rs.getString(1);
                if (!PasswordUtil.matches(securityAnswer, expected)) return false;
            }
        }
        String upd = "UPDATE users SET password_hash=? WHERE identifier=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(upd)) {
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, identifier);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean changePassword(String identifier, String currentPassword, String newPassword) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE identifier=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String hash = rs.getString(1);
                if (!PasswordUtil.matches(currentPassword, hash)) return false;
            }
        }
        String upd = "UPDATE users SET password_hash=? WHERE identifier=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(upd)) {
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, identifier);
            return ps.executeUpdate() == 1;
        }
    }
}
