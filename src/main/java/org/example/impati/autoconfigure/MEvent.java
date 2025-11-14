package org.example.impati.autoconfigure;

public record MEvent<T>(
        T data,
        MEventType eventType
) {

    @SuppressWarnings("unchecked")
    public Class<T> clazz() {
        return (Class<T>) data.getClass();
    }
}
