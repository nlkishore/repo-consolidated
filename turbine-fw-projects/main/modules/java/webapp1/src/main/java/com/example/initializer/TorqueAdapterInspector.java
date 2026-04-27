package com.example.initializer;

import org.apache.torque.Torque;
import org.apache.torque.adapter.Adapter;

import java.lang.reflect.Field;
import java.util.Map;

public class TorqueAdapterInspector {

    public static void logAdapters(org.apache.avalon.framework.logger.Logger logger) {
        try {
            Field adapterMapField = Torque.class.getDeclaredField("adapterMap");
            adapterMapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Adapter> adapterMap = (Map<String, Adapter>) adapterMapField.get(null);

            for (Map.Entry<String, Adapter> entry : adapterMap.entrySet()) {
                String dbHandle = entry.getKey();
                Adapter adapter = entry.getValue();
                String adapterClass = (adapter != null) ? adapter.getClass().getName() : "null";
                logger.info("Torque adapter: " + dbHandle + " → " + adapterClass);
            }

        } catch (Exception e) {
            logger.error("Failed to inspect Torque adapters via reflection", e);
        }
    }
}