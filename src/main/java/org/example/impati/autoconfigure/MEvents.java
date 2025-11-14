package org.example.impati.autoconfigure;

import java.util.List;

public record MEvents<T>(
        List<T> events,
        MEventType eventType
) {

}
