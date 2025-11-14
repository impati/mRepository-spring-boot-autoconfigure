package org.example.impati.autoconfigure;

import java.util.Objects;
import org.example.impati.core.backup.BackupRepository;
import org.springframework.context.event.EventListener;

public class MEventHandler {

    private final BackupRepository backupRepository;

    public MEventHandler(BackupRepository backupRepository) {
        this.backupRepository = backupRepository;
    }

    @EventListener(MEvent.class)
    public <T> void handle(MEvent<T> event) {
        if (Objects.requireNonNull(event.eventType()) == MEventType.SAVE) {
            backupRepository.save(event.data(), event.clazz());
        } else if (event.eventType() == MEventType.DELETE) {
            backupRepository.delete(event.data(), event.clazz());
        }
    }

    @SuppressWarnings("unchecked")
    @EventListener(MEvents.class)
    public <T> void handle(MEvents<T> events) {
        if (events.eventType() == MEventType.SAVE) {
            for (T data : events.events()) {
                backupRepository.save(data, (Class<T>) data.getClass());
            }
        } else if (events.eventType() == MEventType.DELETE) {
            for (T data : events.events()) {
                backupRepository.delete(data, (Class<T>) data.getClass());
            }
        }
    }
}
