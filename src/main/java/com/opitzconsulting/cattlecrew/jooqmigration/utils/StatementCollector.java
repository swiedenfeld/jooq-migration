package com.opitzconsulting.cattlecrew.jooqmigration.utils;

public interface StatementCollector extends AutoCloseable {
    void collect(String statement);
}
