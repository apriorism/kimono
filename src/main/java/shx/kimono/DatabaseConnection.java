package shx.kimono;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConnection {
    private HikariDataSource dataSource;

    public DatabaseConnection(FileConfiguration config) {
        // TODO: use config
    }
}
