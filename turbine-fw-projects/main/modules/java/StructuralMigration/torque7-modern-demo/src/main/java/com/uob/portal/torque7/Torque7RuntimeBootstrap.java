package com.uob.portal.torque7;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;

/**
 * Initialises Torque 7 from {@code /torque.properties} on the classpath, or from an arbitrary
 * {@link Configuration} (for integration tests with Testcontainers).
 */
public final class Torque7RuntimeBootstrap {

    private static volatile boolean initialized;

    private Torque7RuntimeBootstrap() {}

    /**
     * Stops Torque and clears the init guard (for integration tests only).
     */
    public static void resetForTesting() throws TorqueException {
        synchronized (Torque7RuntimeBootstrap.class) {
            if (Torque.isInit()) {
                Torque.shutdown();
            }
            initialized = false;
        }
    }

    public static void initFromClasspath() throws TorqueException {
        if (initialized) {
            return;
        }
        synchronized (Torque7RuntimeBootstrap.class) {
            if (initialized) {
                return;
            }
            try (InputStream in = Torque7RuntimeBootstrap.class.getResourceAsStream("/torque.properties")) {
                if (in == null) {
                    throw new TorqueException("Missing classpath resource /torque.properties");
                }
                initFromConfigurationLocked(loadPropertiesConfiguration(in));
            } catch (TorqueException e) {
                throw e;
            } catch (Exception e) {
                throw new TorqueException("Failed to load Torque configuration", e);
            }
        }
    }

    public static void initFromConfiguration(Configuration cfg) throws TorqueException {
        synchronized (Torque7RuntimeBootstrap.class) {
            if (initialized) {
                return;
            }
            initFromConfigurationLocked(cfg);
        }
    }

    private static Configuration loadPropertiesConfiguration(InputStream in) throws Exception {
        Properties p = new Properties();
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        MapConfiguration cfg = new MapConfiguration(new HashMap<>());
        for (String name : p.stringPropertyNames()) {
            cfg.addProperty(name, p.getProperty(name));
        }
        return cfg;
    }

    private static void initFromConfigurationLocked(Configuration cfg) throws TorqueException {
        try {
            Torque.init(cfg);
            initialized = true;
        } catch (TorqueException e) {
            throw e;
        } catch (Exception e) {
            throw new TorqueException("Failed to initialise Torque runtime", e);
        }
    }
}
