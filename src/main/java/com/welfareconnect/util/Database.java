package com.welfareconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String DB_URL = System.getProperty("wc.db.url", "jdbc:sqlite:welfare_connect.db");

    private Database() {}

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(DB_URL);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA busy_timeout=5000");
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA foreign_keys=ON");
        } catch (SQLException ignored) {}
        return c;
    }

    public static void initialize() throws SQLException {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "role TEXT NOT NULL," +
                    "sub_role TEXT," +
                    "identifier TEXT UNIQUE NOT NULL," +
                    "name TEXT," +
                    "email TEXT," +
                    "phone TEXT," +
                    "password_hash TEXT NOT NULL," +
                    "security_question TEXT," +
                    "security_answer_hash TEXT," +
                    "region TEXT," +
                    "annualIncome TEXT," +
                    "active INTEGER NOT NULL DEFAULT 1" +
                    ")");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_identifier ON users(identifier)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS schemes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "category TEXT," +
                    "description TEXT," +
                    "eligibility TEXT," +
                    "active INTEGER NOT NULL DEFAULT 1" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS applications (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "scheme_id INTEGER NOT NULL," +
                    "status TEXT NOT NULL DEFAULT 'Pending'," +
                    "reason TEXT," +
                    "submitted_at TEXT NOT NULL DEFAULT (datetime('now'))," +
                    "updated_at TEXT NOT NULL DEFAULT (datetime('now'))," +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(scheme_id) REFERENCES schemes(id) ON DELETE CASCADE" +
                    ")");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_app_user ON applications(user_id)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_app_scheme ON applications(scheme_id)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS documents (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "application_id INTEGER NOT NULL," +
                    "file_name TEXT NOT NULL," +
                    "content_type TEXT," +
                    "file_path TEXT NOT NULL," +
                    "uploaded_at TEXT NOT NULL DEFAULT (datetime('now'))," +
                    "FOREIGN KEY(application_id) REFERENCES applications(id) ON DELETE CASCADE" +
                    ")");
        }
    }
}
