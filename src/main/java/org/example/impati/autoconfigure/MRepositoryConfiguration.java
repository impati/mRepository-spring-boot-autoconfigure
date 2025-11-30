package org.example.impati.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.impati.core.backup.BackupMapper;
import org.example.impati.core.backup.BackupRepository;
import org.example.impati.core.backup.FileBackupRepository;
import org.example.impati.core.backup.SimpleBackupMapper;
import org.example.impati.core.method_invoker.MRepositoryMethodInvoker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MRepositoryProperties.class)
public class MRepositoryConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "m.repository",
            name = "backup-enable",
            havingValue = "true"
    )
    public List<MRepositoryMethodInvoker<?>> methodInvoker(ApplicationEventPublisher applicationEventPublisher) {
        return List.of(
                new SaveInvokerWithBackup<>(applicationEventPublisher),
                new DeleteInvokerWithBackup<>(applicationEventPublisher)
        );
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "m.repository",
            name = "backup-enable",
            havingValue = "true"
    )
    public MEventHandler mEventHandler(BackupMapper backupMapper, MRepositoryProperties properties) {
        return new MEventHandler(backupRepository(backupMapper, properties));
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "m.repository",
            name = "backup-enable",
            havingValue = "true"
    )
    public BackupRepository backupRepository(BackupMapper backupMapper, MRepositoryProperties properties) {
        return new FileBackupRepository(backupMapper, properties.getDir());
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "m.repository",
            name = "backup-enable",
            havingValue = "true"
    )
    public BackupMapper backupMapper(ObjectMapper objectMapper) {
        return new JacksonMapper(objectMapper);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "m.repository",
            name = "backup-enable",
            havingValue = "true"
    )
    @ConditionalOnMissingBean(ObjectMapper.class)
    public BackupMapper backupMapper() {
        return new SimpleBackupMapper();
    }
}
