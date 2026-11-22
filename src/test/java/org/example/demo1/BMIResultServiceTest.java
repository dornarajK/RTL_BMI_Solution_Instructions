package org.example.demo1;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BMIResultServiceTest {
    @Test
    void publicSaveResultHandlesSuccessfulConnection() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        BMIResultService.saveResult(() -> connection, 70, 1.75, 22.86, "en");

        verify(statement).executeUpdate();
    }

    @Test
    void publicSaveResultHandlesUnavailableDatabaseWithoutThrowing() {
        assertDoesNotThrow(() -> BMIResultService.saveResult(70, 1.75, 22.86, "en"));
    }

    @Test
    void handlesConnectionFailureWithoutThrowing() {
        assertDoesNotThrow(() -> BMIResultService.saveResult(
                () -> { throw new SQLException("Simulated database outage"); },
                70, 1.75, 22.86, "en"));
    }

    @Test
    void bindsResultFieldsAndExecutesInsert() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        BMIResultService.saveResult(connection, 70.0, 1.75, 22.86, "en");

        verify(statement).setDouble(1, 70.0);
        verify(statement).setDouble(2, 1.75);
        verify(statement).setDouble(3, 22.86);
        verify(statement).setString(4, "en");
        verify(statement).executeUpdate();
    }
}
