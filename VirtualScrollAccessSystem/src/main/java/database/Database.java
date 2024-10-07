package database;

import javafx.utils.HashUtils;


import java.sql.*;
import java.util.UUID;


public class Database {
    private static Connection connection;

    // Hardcoded admin credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_EMAIL = "admin@admin.com";
    private static final String ADMIN_ROLE = "Admin";

    private Database() {}

    private static final String URL = "jdbc:sqlite:src/main/resources/db/app_database.db";

    // Method to establish a connection to the SQLite database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void setup() throws SQLException {
        // Step 1: Establish a connection
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Step 2: Create the users table if it doesn't exist
            String createUserTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    phone TEXT,
                    userType TEXT NOT NULL
                );
            """;

            // Step 3: Execute the SQL statement to create the table
            statement.execute(createUserTableSQL);

            // You can add other tables here similarly if needed
            System.out.println("Database setup complete. Tables created if they didn't exist.");


            // Make sure we have an admin user
            Database.ensureAdminUserExists();


        } catch (SQLException e) {
            throw new SQLException("Error during database setup: " + e.getMessage(), e);
        }


    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close the database connection: " + e.getMessage());
        }
    }


    // Method to check if the admin exists in the database
    public static void ensureAdminUserExists() {
        try (Connection connection = getConnection()) {
            // Check if admin user exists
            String checkQuery = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, ADMIN_USERNAME);
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        // Admin doesn't exist, create the admin user
                        insertAdminUser(connection);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert the hardcoded admin user
    private static void insertAdminUser(Connection connection) throws SQLException {
        String insertQuery = "INSERT INTO users (username, password, email, userType) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, ADMIN_USERNAME);
            insertStatement.setString(2, HashUtils.hashPassword(ADMIN_PASSWORD));  // Make sure to hash the password
            insertStatement.setString(3, ADMIN_EMAIL);
            insertStatement.setString(4, ADMIN_ROLE);
            insertStatement.executeUpdate();
            System.out.println("Master admin user created.");
        }
    }


}
