package com.uob.portal.torque3;

import java.io.InputStream;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;

import com.uob.portal.torque3.map.GtpUserMapBuilder;

/**
 * Initializes Torque 3.x runtime from the classpath resource {@code /torque.properties} and registers
 * the demo {@link com.uob.portal.torque3.map.GtpUserMapBuilder}.
 */
public final class Torque3RuntimeBootstrap {

    private static volatile boolean initialized;

    private Torque3RuntimeBootstrap() {}

    /**
     * Stops Torque and clears the init guard (for integration tests only).
     */
    public static void resetForTesting() throws TorqueException {
        synchronized (Torque3RuntimeBootstrap.class) {
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
        synchronized (Torque3RuntimeBootstrap.class) {
            if (initialized) {
                return;
            }
            try (InputStream in = Torque3RuntimeBootstrap.class.getResourceAsStream("/torque.properties")) {
                if (in == null) {
                    throw new TorqueException("Missing classpath resource /torque.properties");
                }
                PropertiesConfiguration cfg = new PropertiesConfiguration();
                cfg.load(in);
                initFromConfigurationLocked(cfg);
            } catch (TorqueException e) {
                throw e;
            } catch (Exception e) {
                throw new TorqueException("Failed to load Torque configuration", e);
            }
        }
    }

    /**
     * Initialise Torque from an in-memory configuration (used by tests with Testcontainers JDBC URLs).
     */
    public static void initFromConfiguration(PropertiesConfiguration cfg) throws TorqueException {
        synchronized (Torque3RuntimeBootstrap.class) {
            if (initialized) {
                return;
            }
            initFromConfigurationLocked(cfg);
        }
    }

    private static void initFromConfigurationLocked(PropertiesConfiguration cfg) throws TorqueException {
        try {
            Torque.init(cfg);
            Torque.registerMapBuilder(new GtpUserMapBuilder());
            initialized = true;
        } catch (TorqueException e) {
            throw e;
        } catch (Exception e) {
            throw new TorqueException("Failed to initialise Torque", e);
        }
    }
}
