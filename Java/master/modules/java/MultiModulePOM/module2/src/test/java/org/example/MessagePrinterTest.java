package org.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessagePrinterTest {
    @Test
    public void testPrintMessage() {
        MessagePrinter printer = new MessagePrinter();
        assertEquals("Message: Hello World", printer.printMessage("Hello World"));
    }
}

