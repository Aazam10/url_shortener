package com.example.url_shortner.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UrlSchemaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UrlSchemaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void createSchema() {
        // Create Users Table
        String urlTableSql = """
                CREATE TABLE IF NOT EXISTS url_shortener (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                original_url TEXT NOT NULL ,
                short_code TEXT NOT NULL UNIQUE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP)""";

        // Execute table creation
        jdbcTemplate.execute(urlTableSql);
    }
}



