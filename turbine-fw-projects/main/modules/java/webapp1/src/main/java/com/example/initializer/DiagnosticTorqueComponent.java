package com.example.initializer;

import org.apache.torque.avalon.TorqueComponent;
//import org.apache.torque.avalon.impl.TorqueServiceImpl;
import org.apache.torque.Torque;
import org.apache.torque.adapter.Adapter;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.lang.reflect.Field;
import java.util.Map;

public class DiagnosticTorqueComponent extends TorqueComponent {

    @Override
    public void initialize() throws Exception {
        super.initialize(); // this calls Torque.init()

        String defaultDB = Torque.getDefaultDB();
        getLogger().info("Torque.getDefaultDB() = " + defaultDB);

        Adapter adapter = Torque.getAdapter(defaultDB);
        if (adapter == null) {
            getLogger().error("Torque adapter for '" + defaultDB + "' is null — check Torque.properties and init timing.");
        } else {
            getLogger().info("Torque adapter for '" + defaultDB + "' is: " + adapter.getClass().getName());
        }

        // Reflection-based dump of all adapters
        try {
            Field adapterMapField = Torque.class.getDeclaredField("adapterMap");
            adapterMapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Adapter> adapterMap = (Map<String, Adapter>) adapterMapField.get(null);

            for (Map.Entry<String, Adapter> entry : adapterMap.entrySet()) {
                getLogger().info("Torque adapter: " + entry.getKey() + " → " + entry.getValue().getClass().getName());
            }
        } catch (Exception e) {
            getLogger().error("Failed to inspect Torque adapters via reflection", e);
        }
    }
}