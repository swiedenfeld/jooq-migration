package com.opitzconsulting.cattlecrew.jooqmigration.utils;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexHandler {
    private DSLContext dsl;

    @Autowired
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
