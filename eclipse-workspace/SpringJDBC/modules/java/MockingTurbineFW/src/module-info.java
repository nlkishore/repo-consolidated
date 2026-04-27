/**
 * 
 */
/**
 * 
 */
module MockingTurbineFW {
	// Required modules for Java base functionality
//    requires java.base;

    // Include Mockito for testing
 //   requires org.mockito; // Mockito module

    // Include Apache Turbine for MVC framework
//    requires org.apache.turbine;

    // Include Apache Torque for persistence
 //   requires org.apache.torque;

    // Export your own packages to make them available to other modules
 //   exports com.example.myapp.service;

    // Open packages for reflection (if needed by frameworks like Turbine)
 //   opens com.example.myapp.model to org.apache.turbine;
}