package com.example.monitor;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.fulcrum.parser.ParserService;
import org.apache.fulcrum.parser.DefaultParserService;
import org.apache.fulcrum.parser.pool.BaseValueParserPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.lang.reflect.Field;

public class ParserPoolMonitorViaReflection extends AbstractLogEnabled implements Initializable, Serviceable {

    private ServiceManager serviceManager;

    @Override
    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
    }

    @Override
    public void initialize() {
        try {
            ParserService parserService = (ParserService) serviceManager.lookup(ParserService.ROLE);
            DefaultParserService defaultParser = (DefaultParserService) parserService;

            Field poolField = DefaultParserService.class.getDeclaredField("valueParserPool");
            poolField.setAccessible(true);
            BaseValueParserPool pool = (BaseValueParserPool) poolField.get(defaultParser);

            if (pool instanceof GenericObjectPool) {
                GenericObjectPool<?> genericPool = (GenericObjectPool<?>) pool;
                logPoolStats(genericPool);
            } else {
                getLogger().warn("Parser pool is not a GenericObjectPool.");
            }

        } catch (Exception e) {
            getLogger().error("Error monitoring parser pool via reflection", e);
        }
    }

    private void logPoolStats(GenericObjectPool<?> pool) {
        getLogger().info("Parser Pool Stats:");
        getLogger().info(" - Active: " + pool.getNumActive());
        getLogger().info(" - Idle: " + pool.getNumIdle());
        getLogger().info(" - MaxTotal: " + pool.getMaxTotal());
        getLogger().info(" - EvictionRunInterval: " + pool.getTimeBetweenEvictionRunsMillis());
    }
}