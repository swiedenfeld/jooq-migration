package com.opitzconsulting.migrations.db.jooq;

import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Table;

public abstract class FullMigrationSupport {
    protected final DSLContext dsl;
    protected final MigrationScriptsCollector migrationScriptsCollector;

    public FullMigrationSupport(DSLContext dsl, MigrationScriptsCollector migrationScriptsCollector) {
        this.dsl = dsl;
        this.migrationScriptsCollector = migrationScriptsCollector;
    }

    public void migrate(List<Table<?>> tables) throws Exception {
        dropIndexe(migrationScriptsCollector.newScript("0010_disable_indexe.sql"), tables);
        dropConstraints(migrationScriptsCollector.newScript("0020_drop_constraints.sql"), tables);
        unlogTables(migrationScriptsCollector.newScript("0030_unlog_tables.sql"), tables);
        migrateTables();
        logTables(migrationScriptsCollector.newScript("2040_log_tables.sql"), tables);
        addConstraints(migrationScriptsCollector.newScript("2050_add_constraints.sql"), tables);
        addIndexe(migrationScriptsCollector.newScript("2060_enable_indexe.sql"), tables);
        analyzeTables(migrationScriptsCollector.newScript("2070_analyze_tables.sql"), tables);
        migrationScriptsCollector.close();
    }

    private void analyzeTables(StatementCollector statementCollector, List<Table<?>> tables) throws Exception {
        try (statementCollector) {
            tables.forEach(table -> {
                statementCollector.collect("ANALYZE " + table.toString());
            });
        }
    }

    private void logTables(StatementCollector statementCollector, List<Table<?>> tables) throws Exception {
        try (statementCollector) {
            TopologicalSort.topologicalSort(tables).reversed().forEach(table -> {
                statementCollector.collect("ALTER TABLE " + table.toString() + " SET LOGGED");
            });
        }
    }

    private void unlogTables(StatementCollector statementCollector, List<Table<?>> tables) throws Exception {
        try (statementCollector) {
            TopologicalSort.topologicalSort(tables).forEach(table -> {
                statementCollector.collect("ALTER TABLE " + table.toString() + " SET UNLOGGED");
            });
        }
    }

    protected abstract void migrateTables() throws Exception;

    private void addConstraints(StatementCollector statementCollector, List<Table<?>> tables) throws Exception {
        List<Table<?>> topoSorted = TopologicalSort.topologicalSort(tables).reversed();
        try (statementCollector) {
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

    private void dropConstraints(StatementCollector statementCollector, List<Table<?>> tables) throws Exception {
        List<Table<?>> topoSorted = TopologicalSort.topologicalSort(tables);
        try (statementCollector) {
            topoSorted.forEach(table -> {
                dropForeignKeys(table, statementCollector);
                dropUniqueConstraints(table, statementCollector);
                dropCheckConstraints(table, statementCollector);
            });
        }
    }

    private void dropCheckConstraints(Table<?> table, StatementCollector statementCollector) {
        table.getChecks().forEach(check -> {
            statementCollector.collect(dsl.alterTable(table)
                    .dropConstraintIfExists(check.getName())
                    .getSQL());
        });
    }

    private void dropUniqueConstraints(Table<?> table, StatementCollector statementCollector) {
        table.getKeys().forEach(uc -> {
            statementCollector.collect(dsl.alterTable(uc.getTable())
                    .dropConstraintIfExists(uc.getName())
                    .getSQL());
        });
    }

    private void dropForeignKeys(Table<?> table, StatementCollector statementCollector) {
        table.getReferences().forEach(fk -> {
            statementCollector.collect(dsl.alterTable(fk.getTable())
                    .dropConstraintIfExists(fk.getName())
                    .getSQL());
        });
    }

    private void addIndexe(StatementCollector statementCollector, List<Table<?>> tables) throws Exception {
        try (statementCollector) {
            tables.forEach(table -> {
                table.getIndexes().forEach(index -> {
                    statementCollector.collect(
                            dsl.createIndex(index).on(table, index.getFields()).getSQL());
                });
            });
        }
    }

    private void dropIndexe(StatementCollector statementCollector, List<Table<?>> tables) throws Exception {
        try (statementCollector) {
            tables.forEach(table -> {
                table.getIndexes().forEach(index -> {
                    System.out.println("Dropping index " + index.getName() + " on table " + table.getName());
                    statementCollector.collect(dsl.dropIndexIfExists(index).getSQL());
                });
            });
        }
    }
}
