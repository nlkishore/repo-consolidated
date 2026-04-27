package com.uob.portal.torque3;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uob.portal.torque3.dto.GtpUserRow;
import com.uob.portal.torque3.turbine.GtpUserTurbineIntegrationService;
import com.uob.portal.torque3.turbine.SimpleTurbineUser;
import com.uob.portal.torque3.turbine.TurbineUserContract;

import org.junit.jupiter.api.Assumptions;

@Testcontainers
class GtpUserLegacyCrudServiceIT {

    @Container
    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0").withDatabaseName("uob_portal").withUsername("test").withPassword("test");

    @BeforeAll
    static void beforeAll() throws Exception {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available");
        MYSQL.start();
        applySchema();
        PropertiesConfiguration cfg = new PropertiesConfiguration();
        cfg.setProperty("torque.database.default", "uob_portal");
        cfg.setProperty("torque.database.uob_portal.adapter", "mysql");
        cfg.setProperty(
                "torque.dsfactory.uob_portal.factory",
                "org.apache.torque.dsfactory.SharedPoolDataSourceFactory");
        cfg.setProperty("torque.dsfactory.uob_portal.connection.driver", "com.mysql.cj.jdbc.Driver");
        cfg.setProperty("torque.dsfactory.uob_portal.connection.url", MYSQL.getJdbcUrl());
        cfg.setProperty("torque.dsfactory.uob_portal.connection.user", MYSQL.getUsername());
        cfg.setProperty("torque.dsfactory.uob_portal.connection.password", MYSQL.getPassword());
        Torque3RuntimeBootstrap.resetForTesting();
        Torque3RuntimeBootstrap.initFromConfiguration(cfg);
    }

    @AfterAll
    static void afterAll() throws Exception {
        Torque3RuntimeBootstrap.resetForTesting();
    }

    private static void applySchema() throws Exception {
        try (Connection con =
                        DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
                Statement st = con.createStatement()) {
            try (InputStream in =
                    GtpUserLegacyCrudServiceIT.class.getResourceAsStream("/test-schema/gtp_user.sql")) {
                assertThat(in).isNotNull();
                String sql = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
                for (String part : sql.split(";")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        st.execute(trimmed);
                    }
                }
            }
        }
    }

    @Test
    void crud_roundTrip() throws Exception {
        GtpUserRow created =
                GtpUserLegacyCrudService.insert(
                        "jdoe", "secret", "John", "Doe", "john.doe@example.com");
        assertThat(created.getUserId()).isNotNull();
        assertThat(created.getLoginName()).isEqualTo("jdoe");

        GtpUserRow byId = GtpUserLegacyCrudService.findByUserId(created.getUserId());
        assertThat(byId).isNotNull();
        assertThat(byId.getEmail()).isEqualTo("john.doe@example.com");

        GtpUserRow byLogin = GtpUserLegacyCrudService.findByLoginName("jdoe");
        assertThat(byLogin).isNotNull();
        assertThat(byLogin.getUserId()).isEqualTo(created.getUserId());

        assertThat(GtpUserLegacyCrudService.updateEmail(created.getUserId(), "new@example.com"))
                .isTrue();
        GtpUserRow updated = GtpUserLegacyCrudService.findByUserId(created.getUserId());
        assertThat(updated.getEmail()).isEqualTo("new@example.com");

        List<GtpUserRow> all = GtpUserLegacyCrudService.findAll();
        assertThat(all).extracting(GtpUserRow::getLoginName).contains("jdoe");

        assertThat(GtpUserLegacyCrudService.deleteByUserId(created.getUserId())).isTrue();
        assertThat(GtpUserLegacyCrudService.findByUserId(created.getUserId())).isNull();
    }

    @Test
    void turbine_mapping_roundTrip() throws Exception {
        SimpleTurbineUser turbineUser =
                new SimpleTurbineUser(
                        Integer.valueOf(1001),
                        "legacy.turbine",
                        "secret",
                        "Legacy",
                        "User",
                        "legacy.user@example.com");
        GtpUserRow created = GtpUserTurbineIntegrationService.createFromTurbineUser(turbineUser);
        assertThat(created.getUserId()).isNotNull();
        assertThat(created.getTurbineUserId()).isEqualTo(1001);
        assertThat(created.getLoginName()).isEqualTo("legacy.turbine");

        GtpUserRow byTurbineId = GtpUserLegacyCrudService.findByTurbineUserId(1001);
        assertThat(byTurbineId).isNotNull();
        assertThat(byTurbineId.getLoginName()).isEqualTo("legacy.turbine");

        TurbineUserContract resolved = GtpUserTurbineIntegrationService.findTurbineUserById(1001);
        assertThat(resolved).isNotNull();
        assertThat(resolved.getUserName()).isEqualTo("legacy.turbine");
    }
}
