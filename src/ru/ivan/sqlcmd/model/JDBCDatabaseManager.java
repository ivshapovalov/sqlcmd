package ru.ivan.sqlcmd.model;

import java.sql.*;
import java.util.*;

/**
 * Created by Ivan on 16.09.2016.
 */
public class JDBCDatabaseManager implements DatabaseManager {

    private Connection connection;

    @Override
    public List<Map<String, Object>> getTableData(String tableName) {
        List<Map<String, Object>> result = new LinkedList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName))
        {
            ResultSetMetaData rsmd = rs.getMetaData();


            while (rs.next()) {
                Map<String, Object> data = new LinkedHashMap<>();
                for (int index = 1; index <= rsmd.getColumnCount(); index++) {
                    data.put(rsmd.getColumnName(index), rs.getObject(index));
                }
                result.add(data);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }

    @Override
    public Set<String> getTableNames() {
        Set<String> tables = new LinkedHashSet<String>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'"))
        {
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
            return tables;
        } catch (SQLException e) {
            e.printStackTrace();
            return tables;
        }
    }

    @Override
    public void connect(String database, String userName, String password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Please add jdbc jar to project.", e);
        }
        try {
            if (connection != null) {
                connection.close();
            }
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/" + database, userName,
                    password);
        } catch (SQLException e) {
            connection = null;
            throw new RuntimeException(
                    String.format("Cant get connection for model:%s user:%s",
                            database, userName),
                    e);
        }
    }

    @Override
    public void clear(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM public." + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert(String tableName, Map<String, Object> input) {
        String rowNames = getFormatedName(input, "\"%s\",");
        String values = getFormatedValues(input, "'%s',");
        String sql = createString("INSERT INTO ", tableName, " (", rowNames, ") ", "VALUES (", values, ")");

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getValuesFormated(DataSet input, String format) {
        String values = "";
        for (Object value: input.getValues()) {
            values += String.format(format, value);
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

    @Override
    public void update(String tableName, int id, Map<String, Object> newValue) {
        String tableNames = getFormatedName(newValue, "\"%s\" = ?,");
        String sql = createString("UPDATE ", tableName, " SET ", tableNames, " WHERE id = ?");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object value : newValue.values()) {
                ps.setObject(index, value);
                index++;
            }
            ps.setObject(index, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getTableColumns(String tableName) {
        Set<String> tables = new LinkedHashSet<String>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '" + tableName + "'"))
        {
            while (rs.next()) {
                tables.add(rs.getString("column_name"));
            }
            return tables;
        } catch (SQLException e) {
            e.printStackTrace();
            return tables;
        }
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    private String createString(String... args) {
        StringBuilder result = new StringBuilder();
        for (String arg: args) {
            result.append(arg);
        }
        return result.toString();
    }

    private String getFormatedName(Map<String, Object> newValue, String format) {
        String string = "";
        for (String name : newValue.keySet()) {
            string += String.format(format, name);
        }
        string = string.substring(0, string.length() - 1);
        return string;
    }

    private String getFormatedValues(Map<String, Object> input, String format) {
        String values = "";
        for (Object value : input.values()) {
            values += String.format(format, value);
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

}
