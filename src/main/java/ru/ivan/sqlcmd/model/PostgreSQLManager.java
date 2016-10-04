package ru.ivan.sqlcmd.model;

import java.sql.*;
import java.util.*;

public class PostgreSQLManager implements DatabaseManager {

    private Connection connection;

    private static final PropertiesLoader propertiesLoader = new PropertiesLoader();
    private static final String HOST = propertiesLoader.getServerName();
    private static final String PORT = propertiesLoader.getDatabasePort();
    private static final String DRIVER = propertiesLoader.getDriver();
    private static final String DATABASE_URL = DRIVER + HOST + ":" + PORT + "/";
    private static final String USER_NAME = propertiesLoader.getUserName();
    private static final String PASSWORD = propertiesLoader.getPassword();

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            try {
                DriverManager.registerDriver(new org.postgresql.Driver());
            } catch (SQLException e1) {
                try {
                    throw new SQLException("Couldn't register driver in case -", e1);
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }


    @Override
    public Integer getTableSize(String tableName) {
        Integer result=0;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) as count from  " + tableName)) {
            while (rs.next()) {
                result=rs.getInt("count");
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseManagerException(String.format("Не возможно получить данные из таблицы %s",tableName), e);

        }
        return result;
    }

    @Override
    public void dropAllDatabases() {
        try (Statement stmt = connection.createStatement()) {
            Set<String> databases = getDatabasesNames();
            for (String databaseName : databases
                    ) {
                stmt.executeUpdate("DROP DATABASE IF EXISTS " + databaseName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dropAllTables() {
        try (Statement stmt = connection.createStatement()) {
            Set<String> tables = getTableNames();
            for (String tableName : tables
                    ) {
                stmt.executeUpdate("DROP TABLE IF EXISTS public." + tableName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void truncateAllTables() {
        try (Statement stmt = connection.createStatement()) {
            Set<String> tables = getTableNames();
            for (String tableName : tables
                    ) {
                stmt.executeUpdate("TRUNCATE TABLE public." + tableName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createDatabase(String databaseName) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + databaseName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dropDatabase(String databaseName) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP DATABASE IF EXISTS " + databaseName + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTable(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS public." + tableName + "();");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getDatabasesNames() {
        Set<String> databases = new LinkedHashSet<String>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT datname as database_name FROM pg_database WHERE datistemplate = false;")) {
            while (rs.next()) {
                databases.add(rs.getString("database_name"));
            }
            return databases;
        } catch (SQLException e) {
            e.printStackTrace();
            return databases;
        }
    }

    @Override
    public void disconnect() {
        connection = null;
    }

    @Override
    public List<Map<String, Object>> getTableRows(String tableName) {
        List<Map<String, Object>> result = new LinkedList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {
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
            //e.printStackTrace();
            return result;
        }
    }

    @Override
    public Set<String> getTableNames() {
        Set<String> tables = new LinkedHashSet<String>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'")) {
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
            throw new RuntimeException("Пожалуйста добавьте jdbc.jar к проекту.", e);
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
                    String.format("Невозможно подключиться к базе данных :%s, user:%s, password:%s",
                            database, userName, password),
                    e);
        }
    }

    @Override
    public void dropTable(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS public." + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void truncateTable(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE public." + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertRow(String tableName, Map<String, Object> newRow) {
        String rowNames = getFormatedName(newRow, "\"%s\",");
        String values = getFormatedValues(newRow, "'%s',");
        String sql = createString("INSERT INTO ", tableName, " (", rowNames, ") ", "VALUES (", values, ")");

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getValuesFormated(DataSet input, String format) {
        String values = "";
        for (Object value : input.getValues()) {
            values += String.format(format, value);
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

    @Override
    public void updateRow(String tableName, int id, Map<String, Object> newRow) {
        String tableNames = getFormatedName(newRow, "\"%s\" = ?,");
        String query = createString("UPDATE ", tableName, " SET ", tableNames, " WHERE id = ?");

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int index = 1;
            for (Object value : newRow.values()) {
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
    public void deleteRow(String tableName, int id) {
        String query = createString("DELETE  FROM ", tableName, " WHERE id = ",String.valueOf(id));
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getTableColumns(String tableName) {
        Set<String> tables = new LinkedHashSet<String>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '" + tableName + "'")) {
            while (rs.next()) {
                tables.add(rs.getString("column_name"));
            }
            return tables;
        } catch (SQLException e) {
            //e.printStackTrace();
            return tables;
        }
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    private String createString(String... args) {
        StringBuilder result = new StringBuilder();
        for (String arg : args) {
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
