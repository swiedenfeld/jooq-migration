package com.opitzconsulting.cattlecrew.jooqmigration.migration;

import com.opitzconsulting.cattlecrew.jooqmigration.jooq.demo.JooqDemo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LibraryMigrationTest {
    @Autowired
    private LibraryMigration libraryMigration;

    @Test
    void testMigrateTables() throws Exception {
        libraryMigration.migrate(JooqDemo.JOOQ_DEMO.getTables());
    }
}
