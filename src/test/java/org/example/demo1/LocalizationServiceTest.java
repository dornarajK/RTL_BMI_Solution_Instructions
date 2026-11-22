package org.example.demo1;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalizationServiceTest {
    @Test
    void readsLocalizedStringsForLocale() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet results = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(results);
        when(results.next()).thenReturn(true, true, false);
        when(results.getString("key")).thenReturn("weight", "height");
        when(results.getString("value")).thenReturn("Weight", "Height");

        Map<String, String> strings = LocalizationService.getLocalizedStrings(connection, Locale.US);

        assertEquals("Weight", strings.get("weight"));
        assertEquals("Height", strings.get("height"));
        org.mockito.Mockito.verify(statement).setString(1, "en");
    }

    @Test
    void returnsEmptyMapWhenTheDatabaseConnectionFails() throws Exception {
        assertEquals(Map.of(), LocalizationService.getLocalizedStrings(
                Locale.GERMAN,
                () -> { throw new SQLException("Simulated database outage"); }));
    }

    @Test
    void returnsEmptyMapWhenThereAreNoRows() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet results = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(results);
        when(results.next()).thenReturn(false);

        assertEquals(Map.of(), LocalizationService.getLocalizedStrings(connection, Locale.FRENCH));
    }
}
