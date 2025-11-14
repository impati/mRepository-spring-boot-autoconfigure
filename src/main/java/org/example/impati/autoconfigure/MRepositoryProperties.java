package org.example.impati.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "m.repository")
public class MRepositoryProperties {

    private String dir = "./.data/";
    private boolean backupEnable = false;

    public MRepositoryProperties() {
    }

    public MRepositoryProperties(final String dir, final boolean backupEnable) {
        this.dir = dir;
        this.backupEnable = backupEnable;
    }

    public void setDir(final String dir) {
        this.dir = dir;
    }

    public void setBackupEnable(final boolean backupEnable) {
        this.backupEnable = backupEnable;
    }

    public String getDir() {
        return dir;
    }

    public boolean isBackupEnable() {
        return backupEnable;
    }
}
