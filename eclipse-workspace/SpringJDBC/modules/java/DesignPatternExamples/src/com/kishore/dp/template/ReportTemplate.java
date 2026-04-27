package com.kishore.dp.template;

public abstract class ReportTemplate {
	// Template method
    public final void generateReport() {
        fetchData();
        processData();
        formatReport();
    }

    // Abstract methods to be implemented by subclasses
    protected abstract void fetchData();
    protected abstract void processData();

    // Common step for all reports
    private void formatReport() {
        System.out.println("Formatting the report...");
        // Common formatting logic
    }

}
