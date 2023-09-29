package de.vinz.openfls.exceptions;

public class IllegalTimeException extends Exception {
    public IllegalTimeException(String message) {
        super(message);
    }

    public IllegalTimeException() {
        super("Invalid year or month.");
    }
}
