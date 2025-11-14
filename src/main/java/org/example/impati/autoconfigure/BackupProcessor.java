package org.example.impati.autoconfigure;

import java.util.List;
import org.example.impati.core.backup.BackupEntityLoader;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

public class BackupProcessor {

    private final List<BackupEntityLoader<?, ?>> loaders;

    public BackupProcessor(List<BackupEntityLoader<?, ?>> loaders) {
        this.loaders = loaders;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        for (BackupEntityLoader<?, ?> loader : loaders) {
            loader.load();
        }
    }
}
