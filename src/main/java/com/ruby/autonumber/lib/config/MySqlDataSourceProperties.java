package com.ruby.autonumber.lib.config;

import com.ruby.autonumber.lib.exception.AutoNumberException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class MySqlDataSourceProperties {

    private static MySqlDataSourceProperties instance = new MySqlDataSourceProperties();

    public static MySqlDataSourceProperties getInstance() {
        return instance;
    }

    private final Properties properties;

    private MySqlDataSourceProperties() {
        properties = loadProperties();
        if(System.getenv("DB_HOST") != null) {
            properties.setProperty("jdbc.db_host", System.getenv("DB_HOST"));
        }
        if(System.getenv("DB_PORT") != null) {
            properties.setProperty("jdbc.db_port", System.getenv("DB_PORT"));
        }
        if(System.getenv("DB_NAME") != null) {
            properties.setProperty("jdbc.db_name", System.getenv("DB_NAME"));
        }

    }

    private Properties loadProperties() {
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("application.properties")){
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } catch (IOException e) {
            throw new AutoNumberException(e);
        }
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + properties.getProperty("jdbc.db_host") + ":" +
                properties.getProperty("jdbc.db_port") + "/" + properties.getProperty("jdbc.db_name") +
                "?zeroDateTimeBehavior=convertToNull&autoReconnect=true";
    }

    public String getJdbcUsername() {
        return properties.getProperty("jdbc.username");
    }

    public String getJdbcPassword() {
        return properties.getProperty("jdbc.password");
    }


}
