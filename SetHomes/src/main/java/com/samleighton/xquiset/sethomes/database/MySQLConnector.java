package com.samleighton.xquiset.sethomes.database;

import com.samleighton.xquiset.sethomes.SetHomes;

import java.sql.*;
import java.util.logging.Level;

public class MySQLConnector {

    private Connection conn;
    private SetHomes pl;
    private Statement stmt;
    private ResultSet rs;
    private String host;
    private String db;
    private String username;
    private String password;
    private String prefix;
    private int port;

    public MySQLConnector(SetHomes plugin) {
        this.pl = plugin;
        setHost(pl.getDb().getConfig().getString("host"));
        setDb(pl.getDb().getConfig().getString("database"));
        setUsername(pl.getDb().getConfig().getString("username"));
        setPassword(pl.getDb().getConfig().getString("password"));
        setPort(pl.getDb().getConfig().getInt("port"));
        setPrefix(pl.getDb().getConfig().getString("db_prefix"));
        getConnection();
        makeTables();
    }

    private void getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
            setConnection(DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&autoReconnect=true&useSSL=false", getHost(), getPort(), getDb(), getUsername(), getPassword())));
        } catch (Exception e) {
            pl.getServer().getLogger().log(Level.SEVERE, pl.LOG_PREFIX + "DB Connection Error: " + e.getMessage());
        }
    }

    private String getHost() {
        return this.host;
    }

    private String getDb() {
        return this.db;
    }

    private String getUsername() {
        return this.username;
    }

    private String getPassword() {
        return this.password;
    }

    private int getPort() {
        return this.port;
    }

    public String getPrefix() {
        return this.prefix;
    }

    private void setConnection(Connection conn) {
        this.conn = conn;
    }

    private void setHost(String host) {
        this.host = host;
    }

    private void setDb(String db) {
        this.db = db;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    private void setPort(int port) {
        this.port = port;
    }

    private void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public ResultSet executeSQL(String sql) {
        try {
            stmt = conn.createStatement();

            if (stmt.execute(sql)) {
                rs = stmt.getResultSet();
            }
        } catch (SQLException e) {
            pl.getLogger().log(Level.SEVERE, "SQLException: " + e.getMessage());
            pl.getLogger().log(Level.SEVERE, "SQLState: " + e.getSQLState());
            pl.getLogger().log(Level.SEVERE, "VendorError: " + e.getErrorCode());
        }

        return rs;
    }

    public int getPlayerID(String uuid) {
        int userID = -1;
        try {
            String sql = String.format("SELECT id FROM %ssh_users WHERE uuid LIKE '%s'", getPrefix(), uuid);
            ResultSet resultSet = executeSQL(sql);

            if (resultSet != null) {
                while (resultSet.next()) {
                    userID = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            pl.printDBError(e);
        }

        return userID;
    }

    private void makeTables() {
        // MySQL code to create the users table
        String usersTable = String.format("CREATE TABLE IF NOT EXISTS `%s`.`%ssh_users` (" +
                "`id` INT(11) NOT NULL," +
                "`uuid` VARCHAR(255) NOT NULL ," +
                "PRIMARY KEY (`id`), " +
                "UNIQUE KEY `uuid` (`uuid`) " +
                ") ENGINE = InnoDB;", getDb(), getPrefix());

        // MySQL code to create the homes table
        String homesTable = String.format("CREATE TABLE IF NOT EXISTS `%s`.`%ssh_homes` ( " +
                "`id` INT(11) NOT NULL AUTO_INCREMENT , " +
                "`uuid` INT(11) NOT NULL , " +
                "`home_name` VARCHAR(255) NULL DEFAULT NULL , " +
                "`world` VARCHAR(255) NOT NULL , " +
                "`x` DOUBLE NOT NULL , " +
                "`y` DOUBLE NOT NULL , " +
                "`z` DOUBLE NOT NULL , " +
                "`pitch` FLOAT NOT NULL , " +
                "`yaw` FLOAT NOT NULL , " +
                "`home_desc` TEXT NULL DEFAULT NULL , " +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`uuid`) REFERENCES %ssh_users(`id`) ON UPDATE CASCADE ON DELETE CASCADE" +
                ") ENGINE = InnoDB;", getDb(), getPrefix(), getPrefix());

        // Execute queries and close statement
        executeSQL(usersTable);
        executeSQL(homesTable);
        close();
    }

    public void close() {
        if (this.rs != null) {
            try {
                this.rs.close();
            } catch (SQLException e) {
                pl.printDBError(e);
            }
        }

        if (this.stmt != null) {
            try {
                this.stmt.close();
            } catch (SQLException e) {
                pl.printDBError(e);
            }
        }

        this.rs = null;
        this.stmt = null;
    }

    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }

            conn = null;
        } catch (SQLException e) {
            // ignore
        }
    }
}
