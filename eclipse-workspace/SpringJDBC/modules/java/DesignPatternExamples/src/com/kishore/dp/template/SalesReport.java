package com.kishore.dp.template;

public class SalesReport extends ReportTemplate {

	 @Override
	    protected void fetchData() {
	        System.out.println("Fetching sales data...");
	        // Specific logic to fetch sales data
	    }

	    @Override
	    protected void processData() {
	        System.out.println("Processing sales data...");
	        // Specific logic to process sales data
	    }

}
