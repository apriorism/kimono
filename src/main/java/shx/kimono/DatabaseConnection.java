package shx.kimono;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConnection {
    private HikariDataSource dataSource;

    public DatabaseConnection() {
        HikariConfig hikariConfig = new HikariConfig();
        FileConfiguration config = Kimono.getPlugin().getConfig();

        if ( config.getString("database.host").isBlank() ) {
            Kimono.getPlugin().getLogger().warning("database connection not configured, skipping");
            return;
        }

        hikariConfig.setUsername(config.getString("database.username"));
        hikariConfig.setPassword(config.getString("database.password"));
        hikariConfig.setMaximumPoolSize(config.getInt("database.maxPoolSize"));
        hikariConfig.setConnectionTimeout(config.getInt("database.connectionTimeout"));
        hikariConfig.setIdleTimeout(config.getInt("database.idleTimeout"));
        hikariConfig.setMaxLifetime(config.getInt("database.maxLifetime"));
        hikariConfig.setJdbcUrl(getMySQLUrl(
            config.getString("database.host"),
            config.getInt("database.port"),
            config.getString("database.database")
        ));

        dataSource = new HikariDataSource(hikariConfig);
    }

    private String getMySQLUrl(String hostname, int port, String database) {
        return String.format("jdbc:mysql://%s:%d/%s", hostname, port, database);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        dataSource.close();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
