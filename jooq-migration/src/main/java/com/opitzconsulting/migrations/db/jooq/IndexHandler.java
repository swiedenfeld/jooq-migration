package com.opitzconsulting.migrations.db.jooq;

import org.jooq.DSLContext;

public class IndexHandler {
    private DSLContext dsl;

    public IndexHandler(DSLContext dslContext) {
        this.dsl = dslContext.dsl();
    }

    public void dropIndexe(StatementCollector statementCollector) {
        dsl.meta().getTables().forEach(table -> {
            table.getIndexes().forEach(index -> {
                statementCollector.collect(dsl.dropIndexIfExists(index).getSQL());
            });
        });
    }

    public void createIndexe(StatementCollector statementCollector) {
        dsl.meta().getTables().forEach(table -> {
            table.getIndexes().forEach(index -> {
                statementCollector.collect(
                        dsl.createIndex(index).on(table, index.getFields()).getSQL());
            });
        });
    }
}
