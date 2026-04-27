Jav Example using Template pattern
ChatGPT
The Template Method Pattern is a behavioral design pattern that defines the skeleton of an algorithm in the superclass but lets subclasses override specific steps of the algorithm without changing its structure. This pattern promotes code reuse and allows subclasses to provide their own implementation for certain parts of the algorithm.

Let's consider a simple example of a template method pattern in Java where we create a template for a report generation process. The template includes common steps such as data retrieval, data processing, and report formatting. Subclasses can then provide their own implementations for specific steps.

java
Copy code
// Abstract class defining the template method
abstract class ReportTemplate {
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

// Concrete subclass implementing a specific report
class SalesReport extends ReportTemplate {
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

// Concrete subclass implementing another specific report
class ExpenseReport extends ReportTemplate {
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

// Client code
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
In this example:

ReportTemplate is an abstract class representing the template for generating a report.
The generateReport method is the template method, which calls common steps (fetchData, processData, and formatReport) in a specific order.
Subclasses (SalesReport and ExpenseReport) extend ReportTemplate and provide their own implementations for the abstract methods fetchData and processData.
The formatReport method is a common step shared among all reports.
When the client code invokes the generateReport method on instances of SalesReport or ExpenseReport, the template method orchestrates the common steps defined in the superclass while allowing specific steps to be customized by the subclasses. This follows the principles of the Template Method Pattern.