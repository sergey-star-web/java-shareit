package ru.practicum.shareit.exception;

public class AvailableException extends IllegalStateException {
    public AvailableException(String message) {
        super(message);
    }
}