package org.example.impati.autoconfigure;

import java.util.List;
import org.example.impati.core.backup.BackupEntityLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackupConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "m.repository",
            name = "backup-enable",
            havingValue = "true"
    )
    public BackupProcessor processor(List<BackupEntityLoader<?, ?>> loaders) {
        return new BackupProcessor(loaders);
    }
}
