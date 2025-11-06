package com.welfareconnect.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.welfareconnect.util.Database;

public class ApplicationDAO {
    public int create(int userId, int schemeId) throws SQLException {
        String sql = "INSERT INTO applications(user_id, scheme_id, status) VALUES(?,?,'Pending')";
        int newAppId = -1;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, schemeId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newAppId = rs.getInt(1);
                }
            }
        }
        
        // Notify all officers about new application
        if (newAppId != -1) {
            try {
                Application app = findById(newAppId);
                if (app != null) {
                    String message = "New application for '" + app.getSchemeName() + "' has been submitted by " + app.getApplicantName();
                    List<User> officers = new UserDAO().getAllUsers();
                    NotificationDAO notificationDAO = new NotificationDAO();
                    for (User officer : officers) {
                        if ("Officer".equalsIgnoreCase(officer.getRole())) {
                            notificationDAO.createNotification(officer.getId(), message, newAppId);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return newAppId;
    }

    public Application findById(int appId) throws SQLException {
        String sql = "SELECT a.id, a.user_id, a.scheme_id, s.name, a.status, a.updated_at, u.name, u.identifier " +
                     "FROM applications a " +
                     "JOIN schemes s ON a.scheme_id = s.id " +
                     "JOIN users u ON a.user_id = u.id " +
                     "WHERE a.id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Application(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4),
                                           rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8));
                }
            }
        }
        return null;
    }

    public List<Application> listByUser(int userId) throws SQLException {
        String sql = "SELECT a.id,a.user_id,a.scheme_id,s.name,a.status,a.updated_at, u.name, u.identifier " +
                "FROM applications a " +
                "JOIN schemes s ON a.scheme_id=s.id " +
                "JOIN users u ON a.user_id=u.id " +
                "WHERE a.user_id=? ORDER BY a.updated_at DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Application> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Application(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4),
                                           rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)));
                }
                return list;
            }
        }
    }

    public List<Application> listPending() throws SQLException {
        String sql = "SELECT a.id,a.user_id,a.scheme_id,s.name,a.status,a.updated_at, u.name, u.identifier " +
                "FROM applications a " +
                "JOIN schemes s ON a.scheme_id=s.id " +
                "JOIN users u ON a.user_id=u.id " +
                "WHERE a.status IN ('Pending','More Info Required') ORDER BY a.submitted_at ASC";
        try (Connection c = Database.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Application> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Application(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4),
                                       rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)));
            }
            return list;
        }
    }

    public boolean updateStatus(int appId, String status, String reason) throws SQLException {
        String sql = "UPDATE applications SET status=?, reason=?, updated_at=datetime('now') WHERE id=?";
        boolean success = false;
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, reason);
            ps.setInt(3, appId);
            success = ps.executeUpdate() == 1;
        }
        
        // Trigger notification for citizen
        if (success) {
            try {
                Application app = findById(appId);
                if (app != null) {
                    String message = "Your application for '" + app.getSchemeName() + "' has been " + status;
                    if (reason != null && !reason.isEmpty()) {
                        message += ". Reason: " + reason;
                    }
                    new NotificationDAO().createNotification(app.getUserId(), message, appId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return success;
    }
    
    public boolean requestMoreInfo(int appId, int officerId, String message) throws SQLException {
        // Update status to "More Info Required"
        return updateStatus(appId, "More Info Required", message);
    }

    public List<SchemeCount> applicationsByScheme(String startDate, String endDate) throws SQLException {
        String sql = "SELECT s.name, COUNT(*) FROM applications a JOIN schemes s ON a.scheme_id=s.id " +
                "WHERE date(a.submitted_at) BETWEEN date(?) AND date(?) GROUP BY s.name ORDER BY 2 DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                List<SchemeCount> list = new ArrayList<>();
                while (rs.next()) list.add(new SchemeCount(rs.getString(1), rs.getInt(2)));
                return list;
            }
        }
    }

    public List<MonthlyStatus> monthlyApprovedRejected(String startDate, String endDate) throws SQLException {
        String sql = "SELECT strftime('%Y-%m', a.updated_at) ym, " +
                "SUM(CASE WHEN a.status='Approved' THEN 1 ELSE 0 END) approved, " +
                "SUM(CASE WHEN a.status='Rejected' THEN 1 ELSE 0 END) rejected " +
                "FROM applications a WHERE date(a.updated_at) BETWEEN date(?) AND date(?) GROUP BY ym ORDER BY ym";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                List<MonthlyStatus> list = new ArrayList<>();
                while (rs.next()) list.add(new MonthlyStatus(rs.getString(1), rs.getInt(2), rs.getInt(3)));
                return list;
            }
        }
    }

    public static class SchemeCount {
        public final String schemeName;
        public final int count;
        public SchemeCount(String schemeName, int count) { this.schemeName = schemeName; this.count = count; }
    }

    public static class MonthlyStatus {
        public final String yearMonth;
        public final int approved;
        public final int rejected;
        public MonthlyStatus(String yearMonth, int approved, int rejected) {
            this.yearMonth = yearMonth; this.approved = approved; this.rejected = rejected;
        }
    }
}