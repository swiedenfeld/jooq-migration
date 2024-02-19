package com.opitzconsulting.migrations.db.jooq;

import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.TableImpl;

public abstract class MigrationSupport<T extends TableImpl<? extends Record>> {
    protected final DSLContext dsl;

    public MigrationSupport(DSLContext dsl) {
        this.dsl = dsl;
    }

    protected void truncateTable(StatementCollector collector, TableImpl<?> tableImpl) {
        collector.collect(dsl.truncateTable(tableImpl).getSQL());
    }

    protected void dropForeignKeyConstraints(
            StatementCollector statementCollector, TableImpl<?> tableImpl, List<Table<?>> tables) {
        tables.forEach(tab -> {
            tableImpl.getReferencesTo(tab).forEach(fk -> {
                statementCollector.collect(dsl.alterTable(tableImpl)
                        .dropConstraintIfExists(fk.getName())
                        .getSQL());
            });
            tableImpl.getReferencesFrom(tab).forEach(fk -> {
                statementCollector.collect(
                        dsl.alterTable(tab).dropConstraintIfExists(fk.getName()).getSQL());
            });
        });
    }

    protected void addForeignKeyConstraints(
            StatementCollector statementCollector, TableImpl<?> tableImpl, List<Table<?>> tables) {
        tables.forEach(tab -> {
            tableImpl.getReferencesTo(tab).forEach(fk -> {
                statementCollector.collect(
                        dsl.alterTable(fk.getTable()).add(fk.constraint()).getSQL());
            });
            tableImpl.getReferencesFrom(tab).forEach(fk -> {
                statementCollector.collect(
                        dsl.alterTable(tab).add(fk.constraint()).getSQL());
            });
        });
    }

    protected void dropUniqueKeyConstraints(StatementCollector collector, TableImpl<?> tableImpl) {
        tableImpl.getKeys().stream()
                .map(uc -> dsl.alterTable(uc.getTable())
                        .dropConstraintIfExists(uc.getName())
                        .getSQL())
                .forEach(collector::collect);
    }

    protected void addUniqueKeyConstraints(StatementCollector collector, TableImpl<?> tableImpl) {
        tableImpl.getKeys().forEach(k -> {
            collector.collect(dsl.alterTable(k.getTable()).add(k.constraint()).getSQL());
        });
    }

    protected void dropIndexe(StatementCollector collector, TableImpl<?> tableImpl) {
        tableImpl.getIndexes().forEach(idx -> {
            collector.collect(dsl.dropIndexIfExists(idx.getName()).getSQL());
        });
    }

    protected void addIndexe(StatementCollector collector, TableImpl<?> tableImpl) {
        tableImpl.getIndexes().forEach(idx -> {
            collector.collect(dsl.createIndexIfNotExists(idx.getName())
                    .on(idx.getTable(), idx.getFields())
                    .getSQL());
        });
    }

    public void migrateSingleTable(
            StatementCollector collector, TableImpl<?> targetTable, List<Table<?>> relatedTables) {
        dropForeignKeyConstraints(collector, targetTable, relatedTables);
        dropUniqueKeyConstraints(collector, targetTable);
        dropIndexe(collector, targetTable);
        truncateTable(collector, targetTable);
        migrateTable(collector);
        addUniqueKeyConstraints(collector, targetTable);
        addIndexe(collector, targetTable);
        addForeignKeyConstraints(collector, targetTable, relatedTables);
    }

    protected abstract void migrateTable(StatementCollector collector);
}
