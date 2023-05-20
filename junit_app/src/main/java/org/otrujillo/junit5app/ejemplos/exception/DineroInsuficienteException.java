package org.otrujillo.junit5app.ejemplos.exception;

public class DineroInsuficienteException extends RuntimeException{
    public DineroInsuficienteException(String message) {
        super(message);
    }
}
