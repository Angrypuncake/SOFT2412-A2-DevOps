package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static Connection connection;

    private Database() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:vsas.db");
                System.out.println("Database connection established");
                initializeDatabase();
            } catch (SQLException e) {
                System.err.println("Failed to connect to the database: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    private static void initializeDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createUsersDB = "CREATE TABLE IF NOT EXISTS users ("
                    + "id TEXT PRIMARY KEY, "
                    + "username TEXT NOT NULL, "
                    + "password TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "phone TEXT NOT NULL, "
                    + "userType TEXT NOT NULL, ";
            statement.execute(createUsersDB);
            System.out.println("Database initialized with 'users' table.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize the database schema: " + e.getMessage());
            throw e;
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


}
