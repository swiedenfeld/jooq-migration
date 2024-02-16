package com.opitzconsulting.cattlecrew.jooqmigration.utils;

import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class FullMigrationSupport {
    protected final DSLContext dsl;

    @Autowired
    public FullMigrationSupport(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void migrate(List<Table<?>> tables) throws Exception {
        dropIndexe("scripts/0010_disable_indexe.sql", tables);
        dropConstraints("scripts/0020_drop_constraints.sql", tables);
        migrateTables();
        addConstraints("scripts/2040_add_constraints.sql", tables);
        addIndexe("scripts/2050_enable_indexe.sql", tables);
    }

    protected abstract void migrateTables() throws Exception;

    private void addConstraints(String fileName, List<Table<?>> tables) throws Exception {
        List<Table<?>> topoSorted = TopologicalSort.topologicalSort(tables).reversed();
        try (FileWriterCollector statementCollector = new FileWriterCollector(fileName)) {
            topoSorted.forEach(table -> {
                table.getKeys().forEach(k -> {
                    statementCollector.collect(
                            dsl.alterTable(k.getTable()).add(k.constraint()).getSQL());
                });
                table.getChecks().forEach(check -> {
                    statementCollector.collect(
                            dsl.alterTable(table).add(check.constraint()).getSQL());
                });
                table.getReferences().forEach(fk -> {
                    statementCollector.collect(
                            dsl.alterTable(fk.getTable()).add(fk.constraint()).getSQL());
                });
            });
        }
    }

    private void dropConstraints(String fileName, List<Table<?>> tables) throws Exception {
        List<Table<?>> topoSorted = TopologicalSort.topologicalSort(tables);
        try (FileWriterCollector statementCollector = new FileWriterCollector(fileName)) {
            topoSorted.forEach(table -> {
                dropForeignKeys(table, statementCollector);
                dropUniqueConstraints(table, statementCollector);
                dropCheckConstraints(table, statementCollector);
            });
        }
    }

    private void dropCheckConstraints(Table<?> table, FileWriterCollector statementCollector) {
        table.getChecks().forEach(check -> {
            statementCollector.collect(dsl.alterTable(table)
                    .dropConstraintIfExists(check.getName())
                    .getSQL());
        });
    }

    private void dropUniqueConstraints(Table<?> table, FileWriterCollector statementCollector) {
        table.getKeys().forEach(uc -> {
            statementCollector.collect(dsl.alterTable(uc.getTable())
                    .dropConstraintIfExists(uc.getName())
                    .getSQL());
        });
    }

    private void dropForeignKeys(Table<?> table, FileWriterCollector statementCollector) {
        table.getReferences().forEach(fk -> {
            statementCollector.collect(dsl.alterTable(fk.getTable())
                    .dropConstraintIfExists(fk.getName())
                    .getSQL());
        });
    }

    private void addIndexe(String fileName, List<Table<?>> tables) throws Exception {
        try (FileWriterCollector statementCollector = new FileWriterCollector(fileName)) {
            tables.forEach(table -> {
                table.getIndexes().forEach(index -> {
                    statementCollector.collect(
                            dsl.createIndex(index).on(table, index.getFields()).getSQL());
                });
            });
        }
    }

    private void dropIndexe(String fileName, List<Table<?>> tables) throws Exception {
        try (FileWriterCollector statementCollector = new FileWriterCollector(fileName)) {
            tables.forEach(table -> {
                table.getIndexes().forEach(index -> {
                    statementCollector.collect(dsl.dropIndexIfExists(index).getSQL());
                });
            });
        }
    }
}
