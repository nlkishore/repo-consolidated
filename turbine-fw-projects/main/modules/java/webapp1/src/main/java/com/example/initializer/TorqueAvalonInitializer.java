package com.example.initializer;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.torque.Torque;
import org.apache.torque.adapter.Adapter;

import java.util.Map;
import java.util.Set;

public class TorqueAvalonInitializer extends AbstractLogEnabled implements Initializable, Configurable, Serviceable {

    private ServiceManager serviceManager;
    private String torqueConfigPath = "WEB-INF/conf/Torque.properties";

    @Override
    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
        getLogger().info("ServiceManager injected into TorqueAvalonInitializer.");
    }

    @Override
    public void configure(Configuration config) throws ConfigurationException {
        Configuration pathConfig = config.getChild("configfile", false);
        if (pathConfig != null) {
            torqueConfigPath = pathConfig.getValue();
        }
        getLogger().info("TorqueAvalonInitializer configured with path: " + torqueConfigPath);
    }

    @Override
    public void initialize() {
        try {
            getLogger().info("Initializing Torque from: " + torqueConfigPath);
            Torque.init(torqueConfigPath);

            // Log default DB handle
            String defaultDB = Torque.getDefaultDB();
            getLogger().info("Torque.getDefaultDB() = " + defaultDB);

			/*
			 * Map<String, Adapter> adapterMap = Torque.getAdapterMap(); for
			 * (Map.Entry<String, Adapter> entry : adapterMap.entrySet()) { String dbHandle
			 * = entry.getKey(); Adapter adapter = entry.getValue(); String adapterClass =
			 * (adapter != null) ? adapter.getClass().getName() : "null";
			 * getLogger().info("Registered Torque adapter: " + dbHandle + " → " +
			 * adapterClass); }
			 */
            TorqueAdapterInspector.logAdapters(getLogger());


            // Validate default adapter
            Adapter adapter = Torque.getAdapter(defaultDB);
            if (adapter == null) {
                getLogger().error("Torque adapter for '" + defaultDB + "' is null — check Torque.properties and init timing.");
                throw new IllegalStateException("Missing adapter for handle: " + defaultDB);
            } else {
                getLogger().info("Torque adapter for '" + defaultDB + "' is: " + adapter.getClass().getName());
            }

        } catch (Exception e) {
            getLogger().error("Error during Torque initialization and adapter diagnostics", e);
        }
    }
}