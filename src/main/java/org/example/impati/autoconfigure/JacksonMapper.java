package org.example.impati.autoconfigure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.impati.core.backup.BackupMapper;
import org.example.impati.core.backup.BackupRecord;
import org.example.impati.core.backup.RecordType;

public class JacksonMapper implements BackupMapper {

    private static final String DELIMITER = "~!~~!~";

    private final ObjectMapper objectMapper;

    public JacksonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> String serialize(final BackupRecord<T> backupRecord, final Class<T> aClass) {
        try {
            // 객체를 JSON 문자열로 직렬화
            String json = objectMapper.writeValueAsString(backupRecord.data());
            // 기존 포맷 유지: recordType + DELIMITER + json
            return backupRecord.recordType() + DELIMITER + json;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize BackupRecord", e);
        }
    }

    @Override
    public <T> BackupRecord<T> deserialize(final String s, final Class<T> aClass) {
        try {
            String[] parts = s.split(DELIMITER, 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid backup format: " + s);
            }

            RecordType recordType = RecordType.valueOf(parts[0]);
            String json = parts[1];

            // JSON → 객체 변환
            T obj = objectMapper.readValue(json, aClass);

            return BackupRecord.of(recordType, obj);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize BackupRecord", e);
        }
    }
}
