package com.example.url_shortner.config;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseConfiguration {
//        @Bean
//        @Primary
//        public DataSource dataSource() {
//            SQLiteDataSource dataSource = new SQLiteDataSource();
//            dataSource.setUrl("jdbc:sqlite:mydatabase.db?foreign_keys=on");
//
//            SQLiteConfig config = new SQLiteConfig();
//            config.enforceForeignKeys(true);
//
//            try {
//                config.setJournalMode(SQLiteConfig.JournalMode.WAL);
//                dataSource.setConfig(config);
//
//                // Enable foreign keys using a direct connection
//                try (Connection conn = dataSource.getConnection();
//                     Statement stmt = conn.createStatement()) {
//                    stmt.execute("PRAGMA foreign_keys = ON");
//                }
//            } catch (SQLException e) {
//                throw new RuntimeException("Failed to configure SQLite", e);
//            }
//
//            return dataSource;
//        }
}
