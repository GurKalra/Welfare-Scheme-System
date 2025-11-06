package com.welfareconnect.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.welfareconnect.util.Database;

public class NotificationDAO {
    
    static {
        createTableIfNotExists();
    }
    
    private static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS Notifications (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER NOT NULL, " +
                     "message TEXT NOT NULL, " +
                     "is_read BOOLEAN DEFAULT FALSE, " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "application_id INTEGER, " +
                     "FOREIGN KEY(user_id) REFERENCES users(id))";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void createNotification(int userId, String message, int applicationId) throws SQLException {
        String sql = "INSERT INTO Notifications(user_id, message, application_id) VALUES(?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.setInt(3, applicationId);
            ps.executeUpdate();
        }
    }
    
    public List<Notification> getUnreadForUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT id, user_id, message, is_read, created_at, application_id " +
                     "FROM Notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getInt("is_read") == 1,
                        rs.getString("created_at"),
                        rs.getInt("application_id")
                    ));
                }
            }
        }
        return notifications;
    }
    
    public void markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE Notifications SET is_read = 1 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        }
    }
    
    public int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Notifications WHERE user_id = ? AND is_read = 0";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
