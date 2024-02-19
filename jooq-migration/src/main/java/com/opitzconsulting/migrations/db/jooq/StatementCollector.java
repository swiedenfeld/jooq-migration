package com.opitzconsulting.migrations.db.jooq;

public interface StatementCollector extends AutoCloseable {
    void collect(String statement);
}
