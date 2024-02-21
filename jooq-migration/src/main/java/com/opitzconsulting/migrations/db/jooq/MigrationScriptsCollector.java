package com.opitzconsulting.migrations.db.jooq;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MigrationScriptsCollector {
    private final File scriptPath;
    private List<String> scriptNames = new LinkedList<>();
    private boolean sortAlphabetically = false;

    public MigrationScriptsCollector(File scriptPath) {
        this.scriptPath = scriptPath;
        if (!scriptPath.exists()) {
            scriptPath.mkdirs();
        }
    }

    public MigrationScriptsCollector(File scriptPath, boolean sortAlphabetically) {
        this.scriptPath = scriptPath;
        this.sortAlphabetically = sortAlphabetically;
    }

    public StatementCollector newScript(String fileName) {
        scriptNames.add(fileName);
        return new FileWriterCollector(getScriptPath(fileName));
    }

    public void close() throws Exception {
        try (FileWriterCollector collector = new FileWriterCollector(getScriptPath("0000_run_all.sql"))) {
            if (sortAlphabetically) {
                scriptNames.sort(String::compareTo);
            }
            scriptNames.forEach(name -> collector.collect(
                    "\\i " + scriptPath.getPath() + FileSystems.getDefault().getSeparator() + name));
        }
    }

    public String[] getScripts() {
        scriptNames.sort(String::compareTo);
        return scriptNames.stream().map(this::getScriptPath).toArray(String[]::new);
    }

    @NotNull
    private String getScriptPath(String fileName) {
        return scriptPath + FileSystems.getDefault().getSeparator() + fileName;
    }
}
