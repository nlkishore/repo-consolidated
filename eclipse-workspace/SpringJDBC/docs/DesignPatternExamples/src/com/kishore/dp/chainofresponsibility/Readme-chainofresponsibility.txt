ava Example Using Chain of Responsibility pattern
ChatGPT
The Chain of Responsibility Pattern is a behavioral design pattern that allows an object to pass a request along a chain of potential handlers. Each handler in the chain decides either to process the request or to pass it to the next handler in the chain. Let's consider a simple example in Java where we implement a chain of handlers to process purchase requests.

java
Copy code
// Handler interface
interface PurchaseHandler {
    void handleRequest(PurchaseRequest request);
}

// Concrete Handler 1: Manager
class Manager implements PurchaseHandler {
    private static final double MANAGER_LIMIT = 1000.0;
    private PurchaseHandler nextHandler;

    public void setNextHandler(PurchaseHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handleRequest(PurchaseRequest request) {
        if (request.getAmount() <= MANAGER_LIMIT) {
            System.out.println("Manager approves the purchase request of $" + request.getAmount());
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        } else {
            System.out.println("Request exceeds the limit. No one can handle it.");
        }
    }
}

// Concrete Handler 2: Director
class Director implements PurchaseHandler {
    private static final double DIRECTOR_LIMIT = 5000.0;
    private PurchaseHandler nextHandler;

    public void setNextHandler(PurchaseHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handleRequest(PurchaseRequest request) {
        if (request.getAmount() <= DIRECTOR_LIMIT) {
            System.out.println("Director approves the purchase request of $" + request.getAmount());
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        } else {
            System.out.println("Request exceeds the limit. No one can handle it.");
        }
    }
}

// Concrete Handler 3: VicePresident
class VicePresident implements PurchaseHandler {
    private static final double VICE_PRESIDENT_LIMIT = 10000.0;

    @Override
    public void handleRequest(PurchaseRequest request) {
        if (request.getAmount() <= VICE_PRESIDENT_LIMIT) {
            System.out.println("Vice President approves the purchase request of $" + request.getAmount());
        } else {
            System.out.println("Request exceeds the limit. No one can handle it.");
        }
    }
}

// Client code
public class Client {
    public static void main(String[] args) {
        // Create instances of handlers
        PurchaseHandler manager = new Manager();
        PurchaseHandler director = new Director();
        PurchaseHandler vicePresident = new VicePresident();

        // Set up the chain of responsibility
        manager.setNextHandler(director);
        director.setNextHandler(vicePresident);

        // Create a purchase request
        PurchaseRequest request1 = new PurchaseRequest(800.0);
        PurchaseRequest request2 = new PurchaseRequest(5000.0);
        PurchaseRequest request3 = new PurchaseRequest(12000.0);

        // Process purchase requests
        manager.handleRequest(request1);
        System.out.println("----------------------");
        manager.handleRequest(request2);
        System.out.println("----------------------");
        manager.handleRequest(request3);
    }
}

// PurchaseRequest class
class PurchaseRequest {
    private double amount;

    public PurchaseRequest(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
In this example:

PurchaseHandler is the handler interface with the handleRequest method.
Manager, Director, and VicePresident are concrete handler classes that implement the PurchaseHandler interface. Each handler decides whether to approve the purchase request based on its own limit and passes the request to the next handler if needed.
The PurchaseRequest class represents the purchase request with a specific amount.
The client code sets up the chain of responsibility by linking the handlers in a specific order and then processes different purchase requests.
The Chain of Responsibility Pattern allows you to decouple senders and receivers of requests and provides a flexible way to handle requests in a chain. Each handler in the chain can choose to handle the request or pass it to the next handler, promoting a clear separation of concerns.