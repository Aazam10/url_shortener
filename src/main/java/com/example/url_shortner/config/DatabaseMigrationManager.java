package com.example.url_shortner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@Order(1)  // Ensure this runs early
public class DatabaseMigrationManager implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationManager.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        logger.info("Starting database migration process...");
        try {
            createSchemaVersionTable();
            applyMigrations();
        } catch (Exception e) {
            logger.error("Database migration failed", e);
            throw new RuntimeException("Database migration failed", e);
        }
    }

    private void createSchemaVersionTable() {
        logger.info("Creating or verifying schema_version table");
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version TEXT PRIMARY KEY,
                    applied_on TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))
                )
            """);
            logger.info("Schema version table check completed");
        } catch (Exception e) {
            logger.error("Failed to create schema_version table", e);
            throw new RuntimeException("Failed to create schema_version table", e);
        }
    }

    private void applyMigrations() {
        logger.info("Starting to apply migrations");
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:db/migrations/V*__*.sql");

            logger.info("Found {} migration files: {}",
                    resources.length,
                    Arrays.toString(resources)
            );

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) {
                    logger.warn("Skipping migration file with null filename");
                    continue;
                }

                logger.info("Processing migration file: {}", filename);
                processMigrationFile(resource, filename);
            }

            logger.info("Migration process completed successfully");
        } catch (Exception e) {
            logger.error("Failed to read migration files", e);
            throw new RuntimeException("Failed to read migration files", e);
        }
    }

    private void processMigrationFile(Resource resource, String filename) {
        try {
            if (isMigrationApplied(filename)) {
                logger.info("Migration {} already applied, skipping", filename);
                return;
            }

            logger.info("Applying migration: {}", filename);
            String sql = readMigrationFile(resource);
            logger.debug("Migration SQL content: {}", sql);

            executeMigration(sql);
            markMigrationAsApplied(filename);

            logger.info("Successfully applied migration: {}", filename);
        } catch (Exception e) {
            logger.error("Failed to process migration file: " + filename, e);
            throw new RuntimeException("Failed to process migration: " + filename, e);
        }
    }

    private String readMigrationFile(Resource resource) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private void executeMigration(String sql) {
        logger.info("Executing migration SQL");
        jdbcTemplate.execute("PRAGMA foreign_keys = OFF");
        try {
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    logger.debug("Executing SQL statement: {}", statement);
                    jdbcTemplate.execute(statement.trim());
                }
            }
        } finally {
            jdbcTemplate.execute("PRAGMA foreign_keys = ON");
        }
    }

    private boolean isMigrationApplied(String version) {
        logger.debug("Checking if migration is applied: {}", version);
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM schema_version WHERE version = ?",
                    Integer.class,
                    version
            );
            boolean isApplied = count != null && count > 0;
            logger.debug("Migration {} is {}applied", version, isApplied ? "" : "not ");
            return isApplied;
        } catch (Exception e) {
            logger.error("Error checking migration status: " + version, e);
            return false;
        }
    }

    private void markMigrationAsApplied(String version) {
        logger.debug("Marking migration as applied: {}", version);
        try {
            jdbcTemplate.update("INSERT INTO schema_version (version) VALUES (?)", version);
            logger.debug("Successfully marked migration as applied: {}", version);
        } catch (Exception e) {
            logger.error("Failed to mark migration as applied: " + version, e);
            throw new RuntimeException("Failed to mark migration as applied: " + version, e);
        }
    }
}