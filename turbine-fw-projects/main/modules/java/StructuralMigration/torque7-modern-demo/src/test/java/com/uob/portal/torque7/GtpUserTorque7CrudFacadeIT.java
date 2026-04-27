package com.uob.portal.torque7;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uob.portal.torque7.om.GtpUser;

import org.junit.jupiter.api.Assumptions;

@Testcontainers
class GtpUserTorque7CrudFacadeIT {

    @Container
    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0").withDatabaseName("uob_portal").withUsername("test").withPassword("test");

    @BeforeAll
    static void beforeAll() throws Exception {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available");
        MYSQL.start();
        applySchema();
        MapConfiguration cfg = new MapConfiguration(new HashMap<>());
        cfg.addProperty("torque.database.default", "uob_portal");
        cfg.addProperty("torque.database.uob_portal.adapter", "mysql");
        cfg.addProperty(
                "torque.dsfactory.uob_portal.factory",
                "org.apache.torque.dsfactory.SharedPool2DataSourceFactory");
        cfg.addProperty("torque.dsfactory.uob_portal.connection.driver", "com.mysql.cj.jdbc.Driver");
        cfg.addProperty("torque.dsfactory.uob_portal.connection.url", MYSQL.getJdbcUrl());
        cfg.addProperty("torque.dsfactory.uob_portal.connection.user", MYSQL.getUsername());
        cfg.addProperty("torque.dsfactory.uob_portal.connection.password", MYSQL.getPassword());
        Torque7RuntimeBootstrap.resetForTesting();
        Torque7RuntimeBootstrap.initFromConfiguration(cfg);
    }

    @AfterAll
    static void afterAll() throws Exception {
        Torque7RuntimeBootstrap.resetForTesting();
    }

    private static void applySchema() throws Exception {
        try (Connection con =
                        DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
                Statement st = con.createStatement()) {
            try (InputStream in =
                    GtpUserTorque7CrudFacadeIT.class.getResourceAsStream("/test-schema/gtp_user.sql")) {
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
        GtpUser created =
                GtpUserTorque7CrudFacade.insert(
                        "jdoe7", "secret", "Jane", "Doe", "jane.doe@example.com");
        assertThat(created.getUserId()).isNotNull();
        assertThat(created.getLoginName()).isEqualTo("jdoe7");

        GtpUser byId = GtpUserTorque7CrudFacade.findByUserId(created.getUserId());
        assertThat(byId).isNotNull();
        assertThat(byId.getEmail()).isEqualTo("jane.doe@example.com");

        GtpUser byLogin = GtpUserTorque7CrudFacade.findByLoginName("jdoe7");
        assertThat(byLogin).isNotNull();
        assertThat(byLogin.getUserId()).isEqualTo(created.getUserId());

        GtpUser updated = GtpUserTorque7CrudFacade.updateEmail(created.getUserId(), "new7@example.com");
        assertThat(updated).isNotNull();
        assertThat(updated.getEmail()).isEqualTo("new7@example.com");

        List<GtpUser> all = GtpUserTorque7CrudFacade.findAll();
        assertThat(all).extracting(GtpUser::getLoginName).contains("jdoe7");

        assertThat(GtpUserTorque7CrudFacade.deleteByUserId(created.getUserId())).isTrue();
        assertThat(GtpUserTorque7CrudFacade.findByUserId(created.getUserId())).isNull();
    }
}
