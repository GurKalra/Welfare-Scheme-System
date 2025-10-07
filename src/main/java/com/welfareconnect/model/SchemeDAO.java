package com.welfareconnect.model;

import com.welfareconnect.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SchemeDAO {
    public List<Scheme> listActive(String search, String category) throws SQLException {
        List<Scheme> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id,name,category,description,eligibility,active FROM schemes WHERE active=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + search + "%");
        }
        if (category != null && !"All".equalsIgnoreCase(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY name ASC");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Scheme(
                            rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getInt(6) == 1
                    ));
                }
            }
        }
        return list;
    }

    public List<Scheme> listAll() throws SQLException {
        List<Scheme> list = new ArrayList<>();
        String sql = "SELECT id,name,category,description,eligibility,active FROM schemes ORDER BY name";
        try (Connection c = Database.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Scheme(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6) == 1));
            }
        }
        return list;
    }

    public Scheme findById(int id) throws SQLException {
        String sql = "SELECT id,name,category,description,eligibility,active FROM schemes WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Scheme(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6) == 1);
                }
            }
        }
        return null;
    }

    public int create(String name, String category, String description, String eligibility, boolean active) throws SQLException {
        String sql = "INSERT INTO schemes(name,category,description,eligibility,active) VALUES(?,?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setString(4, eligibility);
            ps.setInt(5, active ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public boolean update(int id, String name, String category, String description, String eligibility, boolean active) throws SQLException {
        String sql = "UPDATE schemes SET name=?, category=?, description=?, eligibility=?, active=? WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setString(4, eligibility);
            ps.setInt(5, active ? 1 : 0);
            ps.setInt(6, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM schemes WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public void seedIfEmpty() throws SQLException {
        try (Connection c = Database.getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM schemes")) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }
        }
        String sql = "INSERT INTO schemes(name,category,description,eligibility,active) VALUES(?,?,?,?,1)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            insert(ps, "Student Scholarship", "Education", "Covers tuition and books for eligible students.", "Student ID required; income limits apply.");
            insert(ps, "Farmer Support Grant", "Agriculture", "Financial assistance for seeds and equipment.", "Valid Farmer Permit; landholding limits apply.");
            insert(ps, "Senior Wellness Program", "Senior", "Healthcare subsidies and wellness programs.", "Age 60+ with valid ID.");
            insert(ps, "Housing Assistance", "General", "Subsidized housing scheme for low-income citizens.", "Aadhaar and income proof required.");
        }
    }

    private void insert(PreparedStatement ps, String name, String category, String desc, String elig) throws SQLException {
        ps.setString(1, name);
        ps.setString(2, category);
        ps.setString(3, desc);
        ps.setString(4, elig);
        ps.executeUpdate();
    }
}
