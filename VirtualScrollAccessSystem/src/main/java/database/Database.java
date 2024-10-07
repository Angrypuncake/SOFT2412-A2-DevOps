package database;

import javafx.utils.HashUtils;


import java.sql.*;
import java.util.UUID;


public class Database {
    private static Connection connection;

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
                    phone TEXT NOT NULL,
                    userType TEXT NOT NULL
                );
            """;

            // Step 3: Execute the SQL statement to create the table
            statement.execute(createUserTableSQL);

            // You can add other tables here similarly if needed
            System.out.println("Database setup complete. Tables created if they didn't exist.");
        } catch (SQLException e) {
            throw new SQLException("Error during database setup: " + e.getMessage(), e);
        }

        // Insert a pre existing admin user
        insertDefaultAdmin();
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

    private static void insertDefaultAdmin(){
        String username = "Admin";
        String password = "Admin";
        String email = "Admin@Admin.com";
        String phone = "1234";
        String id = UUID.randomUUID().toString();

        String hashedPassword = HashUtils.hashPassword(password);
        // Inserts the default AdminUser
        try (Connection connection = Database.getConnection()) {
            String insertUser = "INSERT INTO users (id, username, password, email, phone, userType) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(insertUser)) {
                statement.setString(1, id);
                statement.setString(2, username);
                statement.setString(3, hashedPassword);  // Store the hashed password
                statement.setString(4, email);
                statement.setString(5, phone);
                statement.setString(6, "Normal");  // Default user type is "Normal"
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
