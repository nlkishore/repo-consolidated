package com.uob.turbine.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.torque.criteria.Criteria;
import org.apache.torque.TorqueException;

// Example OM and Peer imports — adjust to match your actual package
import com.mycompany.webapp.om.TurbineUser;
import com.mycompany.webapp.om.TurbineUserPeer;
//import com.mycompany.webapp.om.Account;
//import com.mycompany.webapp.om.AccountPeer;

public class PeerRegistry {

    private static final Map<Class<?>, Function<Criteria, ?>> registry = new HashMap<>();

    static {
    	registry.put(TurbineUser.class, criteria -> {
    	    try {
    	        return TurbineUserPeer.doSelect(criteria);
    	    } catch (TorqueException e) {
    	        throw new RuntimeException("Failed to execute query for User", e);
    	    }
    	});


        // Add more mappings as needed
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<Criteria, List<T>> getPeerSelector(Class<T> omClass) {
        Function<Criteria, ?> selector = registry.get(omClass);
        if (selector == null) {
            throw new IllegalArgumentException("No Peer mapping found for class: " + omClass.getName());
        }
        return (Function<Criteria, List<T>>) selector;
    }
}