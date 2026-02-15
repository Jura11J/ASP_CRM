package com.aspcrm.shop.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class JpaConfig {
    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", buildJdbcUrl());
        props.put("jakarta.persistence.jdbc.user", env("DB_USER", "postgres"));
        props.put("jakarta.persistence.jdbc.password", env("DB_PASSWORD", "postgres"));
        props.put("jakarta.persistence.jdbc.driver", env("DB_DRIVER", "org.postgresql.Driver"));
        props.put("hibernate.hbm2ddl.auto", env("HIBERNATE_DDL_AUTO", "validate"));
        props.put("hibernate.dialect", env("HIBERNATE_DIALECT", "org.hibernate.dialect.PostgreSQLDialect"));
        props.put("hibernate.show_sql", env("HIBERNATE_SHOW_SQL", "false"));
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.jdbc.time_zone", "UTC");
        // keep EF Core-created table/column casing (quoted identifiers)
        props.put("hibernate.globally_quoted_identifiers", "true");

        emf = Persistence.createEntityManagerFactory("ShopPU", props);
    }

    private String buildJdbcUrl() {
        String explicit = System.getenv("DB_URL");
        if (explicit != null && !explicit.isBlank()) {
            return explicit;
        }
        String host = env("DB_HOST", "db");
        String port = env("DB_PORT", "5432");
        String name = env("DB_NAME", "aspcrm");
        return "jdbc:postgresql://" + host + ":" + port + "/" + name;
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    @Produces
    @RequestScoped
    public EntityManager entityManager() {
        return emf.createEntityManager();
    }

    public void close(@Disposes EntityManager entityManager) {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
