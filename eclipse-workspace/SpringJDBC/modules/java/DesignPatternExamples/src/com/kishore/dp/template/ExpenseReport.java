package com.kishore.dp.template;

public class ExpenseReport extends ReportTemplate {

	@Override
    protected void fetchData() {
        System.out.println("Fetching expense data...");
        // Specific logic to fetch expense data
    }

    @Override
    protected void processData() {
        System.out.println("Processing expense data...");
        // Specific logic to process expense data
    }

}
