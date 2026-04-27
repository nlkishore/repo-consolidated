package com.uob.aspect;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.fulcrum.security.torque.peer.TurbineUserGroupRoleModelPeerMapper;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * AOP Aspect to intercept maptoModel() calls and filter out GtpGroup objects
 * before they cause ClassCastException.
 * 
 * This aspect uses AspectJ Load-Time Weaving (LTW) to intercept calls to
 * DefaultAbstractTurbineUser.maptoModel() and filters the collection to only
 * include TurbineUserGroupRoleModelPeerMapper objects, preventing GtpGroup
 * objects from causing ClassCastException.
 * 
 * IMPORTANT: This interceptor preserves the original collection type (Set vs List)
 * to avoid ClassCastException when the framework expects a specific type.
 * 
 * Configuration:
 * - AspectJ LTW is configured via META-INF/aop.xml
 * - Requires -javaagent:aspectjweaver.jar JVM argument for LTW
 * 
 * @author UOB Development Team
 */
@Aspect
public class MaptoModelInterceptor {
    
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(MaptoModelInterceptor.class);
    
    /**
     * Intercepts maptoModel() calls and filters the collection to remove
     * objects that are not TurbineUserGroupRoleModelPeerMapper instances.
     * 
     * CRITICAL: Preserves the original collection type (Set vs List) to avoid
     * ClassCastException when framework expects a specific type.
     * 
     * @param joinPoint The join point representing the maptoModel() method call
     * @return The filtered collection or original result if not a collection
     * @throws Throwable If the intercepted method throws an exception
     */
    @Around("execution(* org.apache.fulcrum.security.torque.turbine.DefaultAbstractTurbineUser.maptoModel(..))")
    public Object filterCollection(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("MaptoModelInterceptor: Intercepting maptoModel() call");
        
        Object[] args = joinPoint.getArgs();
        
        // Log all arguments for debugging
        log.info("MaptoModelInterceptor: Method called with {} argument(s)", args.length);
        
        boolean filteredAny = false;
        
        // Check ALL arguments for Collections, not just the first one
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            
            if (arg == null) {
                log.debug("MaptoModelInterceptor: Argument[{}] is null", i);
                continue;
            }
            
            log.debug("MaptoModelInterceptor: Argument[{}] type: {}", i, arg.getClass().getName());
            
            // Check if this argument is a Collection
            if (arg instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) arg;
                log.info("MaptoModelInterceptor: Found Collection argument[{}] with {} object(s) (type: {})", 
                    i, collection.size(), collection.getClass().getName());
                
                       // Log collection contents for debugging
                       int mapperCount = 0;
                       int nonMapperCount = 0;
                       for (Object obj : collection) {
                           if (obj instanceof TurbineUserGroupRoleModelPeerMapper) {
                               mapperCount++;
                           } else {
                               nonMapperCount++;
                               // Only log non-mapper objects if they're not GtpGroup (to reduce noise)
                               // GtpGroup objects are expected during ACL construction and are filtered out
                               if (!(obj instanceof com.uob.om.GtpGroup)) {
                                   log.warn("MaptoModelInterceptor: Non-mapper object in collection: {} (class: {})", 
                                       obj, obj != null ? obj.getClass().getName() : "null");
                               } else {
                                   log.debug("MaptoModelInterceptor: Filtering out GtpGroup: {} (expected during ACL construction)", obj);
                               }
                           }
                       }
                log.info("MaptoModelInterceptor: Collection contains {} mapper(s) and {} non-mapper(s)", 
                    mapperCount, nonMapperCount);
                
                // Determine if original is a Set
                boolean isSet = collection instanceof Set;
                boolean isLinkedHashSet = collection instanceof LinkedHashSet;
                
                // Filter and preserve collection type
                Collection<Object> filtered;
                if (isSet) {
                    if (isLinkedHashSet) {
                        // Preserve LinkedHashSet to maintain insertion order
                        filtered = collection.stream()
                            .filter(obj -> obj instanceof TurbineUserGroupRoleModelPeerMapper)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    } else {
                        // Use HashSet for other Set types
                        filtered = collection.stream()
                            .filter(obj -> obj instanceof TurbineUserGroupRoleModelPeerMapper)
                            .collect(Collectors.toCollection(HashSet::new));
                    }
                    log.debug("MaptoModelInterceptor: Preserved Set type (LinkedHashSet: {})", isLinkedHashSet);
                } else {
                    // Preserve List type
                    filtered = collection.stream()
                        .filter(obj -> obj instanceof TurbineUserGroupRoleModelPeerMapper)
                        .collect(Collectors.toList());
                    log.debug("MaptoModelInterceptor: Preserved List type");
                }
                
                // Log filtering result
                if (collection.size() != filtered.size()) {
                    int filteredCount = collection.size() - filtered.size();
                    log.info("MaptoModelInterceptor: Filtered {} non-mapper object(s) from collection (original: {}, filtered: {}, type preserved: {})", 
                        filteredCount, collection.size(), filtered.size(), isSet ? "Set" : "List");
                    filteredAny = true;
                } else {
                    log.debug("MaptoModelInterceptor: No filtering needed (all objects are mappers)");
                }
                
                // Replace the argument with filtered collection of same type
                args[i] = filtered;
            }
        }
        
        if (args.length == 0) {
            log.warn("MaptoModelInterceptor: maptoModel() called with no arguments - cannot filter");
        } else if (!filteredAny) {
            log.debug("MaptoModelInterceptor: No Collection arguments found to filter");
        }
        
        // Proceed with the (possibly filtered) arguments
        try {
            Object result = joinPoint.proceed(args);
            log.info("MaptoModelInterceptor: maptoModel() completed successfully");
            return result;
        } catch (ClassCastException e) {
            log.error("MaptoModelInterceptor: ClassCastException occurred: {} - Collection type mismatch?", 
                e.getMessage(), e);
            throw e;
        } catch (Throwable e) {
            log.error("MaptoModelInterceptor: Exception in maptoModel(): {}", e.getMessage(), e);
            throw e;
        }
    }
}