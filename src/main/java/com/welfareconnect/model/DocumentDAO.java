package com.welfareconnect.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.welfareconnect.util.Database;

public class DocumentDAO {

    public void add(int applicationId, String fileName, String contentType, String storedPath) throws SQLException {
        String sql = "INSERT INTO documents(application_id, file_name, content_type, file_path) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setString(2, fileName);
            ps.setString(3, contentType);
            ps.setString(4, storedPath);
            ps.executeUpdate();
        }
    }
    
    public List<Document> listByApplication(int applicationId) throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT id, application_id, file_name, content_type, file_path FROM documents WHERE application_id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Document(
                        rs.getInt("id"),
                        rs.getInt("application_id"),
                        rs.getString("file_name"),
                        rs.getString("content_type"),
                        rs.getString("file_path") 
                    ));
                }
            }
        }
        return list;
    }
}