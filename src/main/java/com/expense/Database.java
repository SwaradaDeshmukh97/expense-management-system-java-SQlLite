package com.expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Connection conn;

    public Database(String dbName) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS expense_record " +
                    "(item_name TEXT, item_price REAL, purchase_date TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS admin " +
                    "(username TEXT, password TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS budget " +
                    "(amount REAL)");

            // Insert default admin if not exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admin");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO admin VALUES ('admin', 'admin123')");
            }

            // Insert default budget if not exists
            rs = stmt.executeQuery("SELECT COUNT(*) FROM budget");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO budget VALUES (5000)");
            }

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }

    public List<Object[]> fetchRecords() {
        List<Object[]> rows = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT rowid, * FROM expense_record");
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getDouble(3),
                    rs.getString(4)
                });
            }
        } catch (SQLException e) {
            System.out.println("Fetch Error: " + e.getMessage());
        }
        return rows;
    }

    public void insertRecord(String name, double price, String date) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO expense_record VALUES (?, ?, ?)");
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setString(3, date);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Insert Error: " + e.getMessage());
        }
    }

    public void updateRecord(String name, double price, String date, int rowid) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE expense_record SET item_name=?, item_price=?, " +
                    "purchase_date=? WHERE rowid=?");
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setString(3, date);
            ps.setInt(4, rowid);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update Error: " + e.getMessage());
        }
    }
    public void updateAdminCredentials(String newUsername, String newPassword) {
    try {
        conn.createStatement().execute("DELETE FROM admin");
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO admin VALUES (?, ?)");
        ps.setString(1, newUsername);
        ps.setString(2, newPassword);
        ps.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Update Credentials Error: " + e.getMessage());
    }
}
    public void deleteRecord(int rowid) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM expense_record WHERE rowid=?");
            ps.setInt(1, rowid);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete Error: " + e.getMessage());
        }
    }

    public double getTotalSpent() {
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT SUM(item_price) FROM expense_record");
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("Total Error: " + e.getMessage());
        }
        return 0;
    }

    public boolean validateAdmin(String username, String password) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM admin WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Auth Error: " + e.getMessage());
        }
        return false;
    }

    public double getBudget() {
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT amount FROM budget LIMIT 1");
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("Budget Error: " + e.getMessage());
        }
        return 5000;
    }

    public void setBudget(double amount) {
        try {
            conn.createStatement().execute("DELETE FROM budget");
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO budget VALUES (?)");
            ps.setDouble(1, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Set Budget Error: " + e.getMessage());
        }
    }

    public void close() {
        try { if (conn != null) conn.close(); }
        catch (SQLException e) { System.out.println(e.getMessage()); }
    }
}