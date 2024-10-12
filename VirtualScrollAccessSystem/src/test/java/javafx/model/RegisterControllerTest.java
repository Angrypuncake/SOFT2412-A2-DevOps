package javafx.model;

import database.Database;
import javafx.controller.RegisterController;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;


import java.awt.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegisterControllerTest {
    private RegisterController controller;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField emailField;
    private TextField phoneField;
    private Label errorMessageLabel;
    private Connection connection;

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @BeforeEach
    public void setUp() throws Exception {
        controller = new RegisterController();
        usernameField = mock(TextField.class);
        passwordField = mock(PasswordField.class);
        emailField = mock(TextField.class);
        phoneField = mock(TextField.class);
        errorMessageLabel = mock(Label.class);

        setField(controller, "usernameField", usernameField);
        setField(controller, "passwordField", passwordField);
        setField(controller, "emailField", emailField);
        setField(controller, "phoneField", phoneField);
        setField(controller, "errorMessageLabel", errorMessageLabel);
    }

    @Test
    public void testRegisterEmptyField() {
        when(usernameField.getText()).thenReturn("");
        when(passwordField.getText()).thenReturn("");
        when(emailField.getText()).thenReturn("");
        when(phoneField.getText()).thenReturn("");

        controller.handleRegister();

        assertEquals("All fields are required!", errorMessageLabel.getText());
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        when(usernameField.getText()).thenReturn("testuser");
        when(passwordField.getText()).thenReturn("testpass");
        when(emailField.getText()).thenReturn("testemail@example.com");
        when(phoneField.getText()).thenReturn("0123456789");

        connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        mockStatic(Database.class);
        when(Database.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        controller.handleRegister();

        verify(usernameField).clear();
        verify(passwordField).clear();
        verify(emailField).clear();
        verify(phoneField).clear();
        assertEquals("User successfully registered!", errorMessageLabel.getText());
    }

    @Test
    public void testRegisterFailure() throws Exception {
        when(usernameField.getText()).thenReturn("testuser");
        when(passwordField.getText()).thenReturn("testpass");
        when(emailField.getText()).thenReturn("testemail@example.com");
        when(phoneField.getText()).thenReturn("0123456789");

        connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        mockStatic(Database.class);
        when(Database.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        controller.handleRegister();

        verify(errorMessageLabel).setText("Registration failed!");
    }

    @Test
    public void testDatabaseError() throws Exception {
        when(usernameField.getText()).thenReturn("testuser");
        when(passwordField.getText()).thenReturn("testpass");
        when(emailField.getText()).thenReturn("testemail@example.com");
        when(phoneField.getText()).thenReturn("0123456789");

        connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        mockStatic(Database.class);
        when(Database.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("Database Error"));

        controller.handleRegister();

        verify(errorMessageLabel).setText(contains("Database Error!"));
    }
}
