package com.welfareconnect.model;

import com.welfareconnect.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    public boolean add(int applicationId, String fileName, String contentType, String filePath) throws SQLException {
        String sql = "INSERT INTO documents(application_id,file_name,content_type,file_path) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setString(2, fileName);
            ps.setString(3, contentType);
            ps.setString(4, filePath);
            return ps.executeUpdate() == 1;
        }
    }

    public List<String> listPathsByApplication(int applicationId) throws SQLException {
        String sql = "SELECT file_path FROM documents WHERE application_id=? ORDER BY id";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> list = new ArrayList<>();
                while (rs.next()) list.add(rs.getString(1));
                return list;
            }
        }
    }
}
