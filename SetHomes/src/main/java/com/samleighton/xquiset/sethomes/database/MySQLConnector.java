package com.samleighton.xquiset.sethomes.database;

import com.samleighton.xquiset.sethomes.SetHomes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;

public class MySQLConnector {

    private Connection conn;
    private SetHomes pl;
    private String host;
    private String db;
    private String username;
    private String password;
    private int port;

    public MySQLConnector(SetHomes plugin) {
        this.pl = plugin;
        this.host = pl.getDb().getConfig().getString("host");
        this.db = pl.getDb().getConfig().getString("database");
        this.username = pl.getDb().getConfig().getString("username");
        this.password = pl.getDb().getConfig().getString("password");
        this.port = pl.getDb().getConfig().getInt("port");
    }

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
            setConnection(DriverManager.getConnection("jdbc:mysql://" + getHost() + ":" + getPort(), getUsername(), getPassword()));
        } catch (Exception e) {
            pl.getServer().getLogger().log(Level.SEVERE, pl.LOG_PREFIX + "DATABASE ERROR: " + e.getMessage());
        }

        return conn;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public String getHost() {
        return this.host;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getPort() {
        return this.port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
