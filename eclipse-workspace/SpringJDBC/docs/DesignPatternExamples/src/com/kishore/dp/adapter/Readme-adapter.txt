Example Java Program following Adapter Pattern

ChatGPT
The Adapter Pattern is a structural design pattern that allows incompatible interfaces to work together. It acts as a bridge between two incompatible interfaces by converting the interface of a class into another interface that a client expects. Here's an example Java program illustrating the Adapter Pattern:

Let's consider a scenario where we have an existing interface OldSystem with a method request(), and we want to use a new system with an interface NewSystem that has a method specificRequest(). We'll create an adapter class Adapter to make the old system compatible with the new system.

java
Copy code
// Existing interface (Old System)
interface OldSystem {
    void request();
}

// Existing class implementing the OldSystem interface
class OldSystemImpl implements OldSystem {
    @Override
    public void request() {
        System.out.println("Old System is processing the request.");
    }
}

// New interface (New System)
interface NewSystem {
    void specificRequest();
}

// New class implementing the NewSystem interface
class NewSystemImpl implements NewSystem {
    @Override
    public void specificRequest() {
        System.out.println("New System is processing the specific request.");
    }
}

// Adapter class that adapts the OldSystem to the NewSystem interface
class Adapter implements NewSystem {
    private OldSystem oldSystem;

    public Adapter(OldSystem oldSystem) {
        this.oldSystem = oldSystem;
    }

    @Override
    public void specificRequest() {
        // Delegate the request to the existing OldSystem
        oldSystem.request();
    }
}

// Client code using the NewSystem interface
public class Client {
    public static void main(String[] args) {
        // Using the New System directly
        NewSystem newSystem = new NewSystemImpl();
        newSystem.specificRequest();

        System.out.println("-----------------------------");

        // Using the Old System through the Adapter
        OldSystem oldSystem = new OldSystemImpl();
        NewSystem adaptedSystem = new Adapter(oldSystem);
        adaptedSystem.specificRequest();
    }
}
In this example:

OldSystem represents an existing system with the method request().
NewSystem represents a new system with the method specificRequest().
OldSystemImpl and NewSystemImpl are classes implementing the respective interfaces.
Adapter is the adapter class that adapts the OldSystem to the NewSystem interface.
The Client class demonstrates how both the new system and the adapted old system can be used interchangeably.
This Adapter Pattern allows the client code to work with the NewSystem interface, even though the actual implementation is based on the OldSystem. The adapter (Adapter class) acts as a bridge, enabling the use of existing components in a system that expects a different interface.