package com.kishore.dp.template;

public class Client {
	public static void main(String[] args) {
        // Generate Sales Report
        ReportTemplate salesReport = new SalesReport();
        System.out.println("Generating Sales Report:");
        salesReport.generateReport();
        System.out.println();

        // Generate Expense Report
        ReportTemplate expenseReport = new ExpenseReport();
        System.out.println("Generating Expense Report:");
        expenseReport.generateReport();
    }

}
