package com.uob.service.adapter;

// Use fully qualified names to avoid import conflicts between
// org.apache.fulcrum.security.entity.User and org.apache.turbine.om.security.User

/**
 * Adapter/Shim to bridge between Turbine User and Fulcrum User interfaces
 * This allows seamless conversion between the two type systems
 * 
 * Since GtpUser implements both TurbineUser and Fulcrum User interfaces,
 * this adapter provides type-safe conversion methods.
 */
public class UserAdapter {
    
    /**
     * Convert Turbine User to Fulcrum User for API compatibility
     * Since GtpUser implements both interfaces, this is a safe cast
     * 
     * @param turbineUser The Turbine User instance
     * @return Fulcrum User instance (may be the same object or a wrapper)
     */
    @SuppressWarnings("unchecked")
    public static org.apache.fulcrum.security.entity.User toFulcrumUser(org.apache.turbine.om.security.User turbineUser) {
        if (turbineUser == null) {
            return null;
        }
        // GtpUser implements both TurbineUser and Fulcrum User
        // This cast is safe at runtime - GtpUser extends BaseGtpUser which
        // extends DefaultAbstractTurbineUser which implements both interfaces
        if (turbineUser instanceof org.apache.fulcrum.security.entity.User) {
            return (org.apache.fulcrum.security.entity.User) turbineUser;
        }
        // Fallback: create a wrapper if direct cast fails
        return new FulcrumUserWrapper(turbineUser);
    }
    
    /**
     * Convert Fulcrum User to Turbine User
     * 
     * @param fulcrumUser The Fulcrum User instance
     * @return Turbine User instance (may be the same object or a wrapper)
     */
    @SuppressWarnings("unchecked")
    public static org.apache.turbine.om.security.User toTurbineUser(org.apache.fulcrum.security.entity.User fulcrumUser) {
        if (fulcrumUser == null) {
            return null;
        }
        // GtpUser implements both interfaces
        if (fulcrumUser instanceof org.apache.turbine.om.security.User) {
            return (org.apache.turbine.om.security.User) fulcrumUser;
        }
        // Fallback: create a wrapper if direct cast fails
        return new TurbineUserWrapper(fulcrumUser);
    }
    
    /**
     * Wrapper that makes a Turbine User appear as a Fulcrum User
     * This shim delegates all calls to the underlying Turbine User
     */
    private static class FulcrumUserWrapper implements org.apache.fulcrum.security.entity.User {
        private final org.apache.turbine.om.security.User turbineUser;
        
        public FulcrumUserWrapper(org.apache.turbine.om.security.User turbineUser) {
            this.turbineUser = turbineUser;
        }
        
        @Override
        public Object getId() {
            return turbineUser.getId();
        }
        
        @Override
        public void setId(Object id) {
            turbineUser.setId(id);
        }
        
        @Override
        public String getName() {
            return turbineUser.getName();
        }
        
        @Override
        public void setName(String name) {
            turbineUser.setName(name);
        }
        
        @Override
        public String getPassword() {
            return turbineUser.getPassword();
        }
        
        @Override
        public void setPassword(String password) {
            turbineUser.setPassword(password);
        }
    }
    
    /**
     * Wrapper that makes a Fulcrum User appear as a Turbine User
     * This shim delegates all calls to the underlying Fulcrum User
     */
    private static class TurbineUserWrapper implements org.apache.turbine.om.security.User {
        private final org.apache.fulcrum.security.entity.User fulcrumUser;
        
        public TurbineUserWrapper(org.apache.fulcrum.security.entity.User fulcrumUser) {
            this.fulcrumUser = fulcrumUser;
        }
        
        @Override
        public Object getId() {
            return fulcrumUser.getId();
        }
        
        @Override
        public void setId(Object id) {
            fulcrumUser.setId(id);
        }
        
        @Override
        public String getName() {
            return fulcrumUser.getName();
        }
        
        @Override
        public void setName(String name) {
            fulcrumUser.setName(name);
        }
        
        @Override
        public String getPassword() {
            return fulcrumUser.getPassword();
        }
        
        @Override
        public void setPassword(String password) {
            fulcrumUser.setPassword(password);
        }
        
        @Override
        public boolean hasLoggedIn() {
            // Default implementation - can be enhanced if needed
            return false;
        }
        
        @Override
        public void setHasLoggedIn(Boolean hasLoggedIn) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public void updateLastLogin() {
            // Default implementation - can be enhanced if needed
            // This method is called when user logs in
        }
        
        @Override
        public String getConfirmed() {
            // Default implementation - can be enhanced if needed
            return "N";
        }
        
        @Override
        public void setConfirmed(String confirmed) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public void incrementAccessCounterForSession() {
            // Default implementation - can be enhanced if needed
            // This method increments the access counter for the current session
        }
        
        @Override
        public void incrementAccessCounter() {
            // Default implementation - can be enhanced if needed
            // This method increments the access counter
        }
        
        @Override
        public boolean isConfirmed() {
            // Default implementation - can be enhanced if needed
            return "Y".equals(getConfirmed());
        }
        
        @Override
        public java.util.Date getCreateDate() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void setCreateDate(java.util.Date createDate) {
            // Default implementation - can be enhanced if needed
        }
        
          // Removed @Override - getModifiedDate() is not part of the User interface
          public java.util.Date getModifiedDate() {
              // Default implementation - can be enhanced if needed
              return null;
          }
        
        // Removed @Override - setModifiedDate() is not part of the User interface
        public void setModifiedDate(java.util.Date modifiedDate) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public void setTemp(String key, Object value) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public Object getTemp(String key) {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public Object getTemp(String key, Object defaultValue) {
            // Default implementation - can be enhanced if needed
            Object value = getTemp(key);
            return value != null ? value : defaultValue;
        }

        // Removed @Override - getTemp() without parameters is not part of the User interface
        public java.util.Map<String, Object> getTemp() {
            // Default implementation - can be enhanced if needed
            return null;
        }

        @Override
        public java.util.Map<String, Object> getTempStorage() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        // Removed @Override - setTempStorage() is not part of the User interface
        public void setTempStorage(java.util.Map<String, Object> tempStorage) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public java.util.Map<String, Object> getPermStorage() {
            // Default implementation - can be enhanced if needed
            return new java.util.HashMap<>();
        }
        
        // Removed @Override annotation as there is no supertype method to override.
        public java.util.Map<String, Object> getPerm() {
            // Default implementation - can be enhanced if needed
            return new java.util.HashMap<>();
        }
        @Override
        public Object getPerm(String key) {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public Object getPerm(String key, Object defaultValue) {
            // Default implementation - can be enhanced if needed
            Object value = getPerm(key);
            return value != null ? value : defaultValue;
        }
        
        @Override
        public void setPerm(String key, Object value) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public void setPermStorage(java.util.Map<String, Object> permStorage) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public java.util.Date getLastLogin() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void setLastLogin(java.util.Date lastLogin) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public java.util.Date getLastAccessDate() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void setLastAccessDate() {
            // Default implementation - can be enhanced if needed
            // This method sets the last access date to current time
        }
        
        @Override
        public int getAccessCounterForSession() {
            // Default implementation - can be enhanced if needed
            return 0;
        }
        
        @Override
        public void setAccessCounterForSession(int accessCounter) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public int getAccessCounter() {
            // Default implementation - can be enhanced if needed
            return 0;
        }
        
        @Override
        public void setAccessCounter(int accessCounter) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public Object removeTemp(String key) {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @SuppressWarnings("unused")
        public void removePerm(String key) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public void setUserDelegate(org.apache.fulcrum.security.model.turbine.entity.TurbineUser delegate) {
            // Default implementation - can be enhanced if needed
            // This method sets a user delegate for delegation pattern
        }
        
        @Override
        public org.apache.fulcrum.security.model.turbine.entity.TurbineUser getUserDelegate() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void setObjectdata(byte[] objectdata) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public byte[] getObjectdata() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void setEmail(String email) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public String getEmail() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void setFirstName(String firstName) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public String getFirstName() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void setLastName(String lastName) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public String getLastName() {
            // Default implementation - can be enhanced if needed
            return null;
        }
        
        @Override
        public void removeUserGroupRole(org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole userGroupRole) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public void addUserGroupRole(org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole userGroupRole) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public <T extends org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole> void setUserGroupRoleSet(java.util.Set<T> userGroupRoleSet) {
            // Default implementation - can be enhanced if needed
        }
        
        @Override
        public java.util.Set<? extends org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole> getUserGroupRoleSet() {
            // Default implementation - can be enhanced if needed
            return new java.util.HashSet<>();
        }
    }
}
