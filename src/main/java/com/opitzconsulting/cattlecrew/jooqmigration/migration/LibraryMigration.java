package com.opitzconsulting.cattlecrew.jooqmigration.migration;

import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.TmpMappingBookIsbn13Uuid.TMP_MAPPING_BOOK_ISBN13_UUID;
import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.TmpMappingMemberIdUuid.TMP_MAPPING_MEMBER_ID_UUID;
import static org.jooq.impl.DSL.*;

import com.opitzconsulting.cattlecrew.jooqmigration.jooq.demo.tables.Instance;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.extensions.Routines;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.Book;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.Checkout;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.Member;
import com.opitzconsulting.cattlecrew.jooqmigration.utils.FullMigrationSupport;
import com.opitzconsulting.cattlecrew.jooqmigration.utils.MigrationScriptsCollector;
import com.opitzconsulting.cattlecrew.jooqmigration.utils.StatementCollector;
import java.io.File;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
public class LibraryMigration extends FullMigrationSupport {
    public LibraryMigration(DSLContext dsl, String scriptsPath) {
        super(dsl, new MigrationScriptsCollector(new File(scriptsPath)));
    }

    @Override
    protected void migrateTables() throws Exception {
        createBookMappingTables(migrationScriptsCollector.newScript("1010_create_mapping_tables.sql"));
        mapMembers(migrationScriptsCollector.newScript("1020_members.sql"));
        mapBooks(migrationScriptsCollector.newScript("1030_books.sql"));
        mapCheckouts(migrationScriptsCollector.newScript("1040_checkout.sql"));
    }

    private void mapCheckouts(StatementCollector statementCollector) throws Exception {
        var checkoutStaging = Checkout.CHECKOUT;
        var source = checkoutStaging.as("source");
        var checkoutDemo = com.opitzconsulting.cattlecrew.jooqmigration.jooq.demo.tables.Checkout.CHECKOUT;
        var target = checkoutDemo.as("target");
        var instance = Instance.INSTANCE;
        var mappingBooks = TMP_MAPPING_BOOK_ISBN13_UUID;
        var mappingMembers = TMP_MAPPING_MEMBER_ID_UUID;
        try (statementCollector) {
            var sql = dsl.insertInto(
                            target,
                            target.ID,
                            target.INSTANCE_ID,
                            target.MEMBER_ID,
                            target.CHECKOUT_DATE,
                            target.RETURN_DATE,
                            target.ACTUAL_RETURN_DATE)
                    .select(dsl.select(
                                    Routines.uuidGenerateV7(),
                                    instance.ID,
                                    mappingMembers.UUID,
                                    source.CHECKOUT_DATE,
                                    source.RETURN_DATE,
                                    source.ACTUAL_RETURN_DATE)
                            .from(source)
                            .join(mappingBooks)
                            .on(source.ISBN13.eq(mappingBooks.ISBN13))
                            .join(mappingMembers)
                            .on(source.MEMBER_ID.eq(mappingMembers.MEMBER_ID))
                            .join(instance)
                            .on(instance.BOOK_ID.eq(mappingBooks.UUID)))
                    .onConflictDoNothing()
                    .getSQL();
            statementCollector.collect(dsl.truncateTable(checkoutDemo).cascade().getSQL());
            statementCollector.collect(sql);
        }
    }

    private void mapBooks(StatementCollector statementCollector) throws Exception {
        var source = Book.BOOK.as("source");
        var bookTarget = com.opitzconsulting.cattlecrew.jooqmigration.jooq.demo.tables.Book.BOOK;
        var target = bookTarget.as("target");
        Instance instance = Instance.INSTANCE;
        var instanceTarget = instance.as("instanceTarget");
        var mapping = TMP_MAPPING_BOOK_ISBN13_UUID.as("mapping");
        try (statementCollector) {
            String sql = dsl.insertInto(
                            target,
                            target.ID,
                            target.TITLE,
                            target.AUTHOR,
                            target.PUBLISHER,
                            target.PUBLISHED,
                            target.ISBN13,
                            target.GENRE)
                    .select(dsl.select(
                                    mapping.UUID,
                                    source.TITLE,
                                    source.AUTHOR,
                                    source.PUBLISHER,
                                    source.PUBLISHED,
                                    source.ISBN13,
                                    source.GENRE)
                            .from(source)
                            .innerJoin(mapping)
                            .on(source.ISBN13.eq(mapping.ISBN13)))
                    .getSQL();
            statementCollector.collect(dsl.truncateTable(bookTarget).cascade().getSQL());
            statementCollector.collect(sql);
            sql = dsl.insertInto(
                            instanceTarget, instanceTarget.ID, instanceTarget.BOOK_ID, instanceTarget.ACQUIRED_DATE)
                    .select(dsl.select(Routines.uuidGenerateV7(), target.ID, target.PUBLISHED)
                            .from(target))
                    .getSQL();
            statementCollector.collect(dsl.truncateTable(instance).cascade().getSQL());
            statementCollector.collect(sql);
        }
    }

    private void mapMembers(StatementCollector statementCollector) throws Exception {
        var memberTarget = com.opitzconsulting.cattlecrew.jooqmigration.jooq.demo.tables.Member.MEMBER;
        var target = memberTarget.as("target");
        var source = Member.MEMBER.as("source");
        var mapping = TMP_MAPPING_MEMBER_ID_UUID.as("mapping");
        try (statementCollector) {
            String sql = dsl.insertInto(
                            target,
                            target.ID,
                            target.EMAIL,
                            target.FIRST_NAME,
                            target.LAST_NAME,
                            target.DATE_OF_BIRTH,
                            target.PHONE)
                    .select(dsl.select(
                                    mapping.UUID,
                                    source.EMAIL,
                                    source.FIRST_NAME,
                                    source.LAST_NAME,
                                    source.DATE_OF_BIRTH,
                                    source.PHONE)
                            .from(source)
                            .innerJoin(mapping)
                            .on(source.ID.eq(mapping.MEMBER_ID)))
                    .getSQL();
            statementCollector.collect(dsl.truncateTable(memberTarget).cascade().getSQL());
            statementCollector.collect(sql);
            statementCollector.collect(dsl.deleteFrom(memberTarget)
                    .where(memberTarget.EMAIL.in(dsl.select(memberTarget.EMAIL)
                            .from(target)
                            .groupBy(target.EMAIL)
                            .having(count().gt(1))))
                    .getSQL());
        }
    }

    private void createBookMappingTables(StatementCollector statementCollector) throws Exception {
        Book book = Book.BOOK.as("book");
        Member member = Member.MEMBER.as("member");
        try (statementCollector) {
            statementCollector.collect(
                    dsl.truncateTable(TMP_MAPPING_BOOK_ISBN13_UUID).getSQL());
            statementCollector.collect(
                    dsl.truncateTable(TMP_MAPPING_MEMBER_ID_UUID).getSQL());
            var bookMapping = dsl.insertInto(
                            TMP_MAPPING_BOOK_ISBN13_UUID,
                            TMP_MAPPING_BOOK_ISBN13_UUID.ISBN13,
                            TMP_MAPPING_BOOK_ISBN13_UUID.UUID)
                    .select(dsl.select(book.ISBN13, Routines.uuidGenerateV7()).from(book))
                    .getSQL();
            var memberMapping = dsl.insertInto(
                            TMP_MAPPING_MEMBER_ID_UUID,
                            TMP_MAPPING_MEMBER_ID_UUID.MEMBER_ID,
                            TMP_MAPPING_MEMBER_ID_UUID.UUID)
                    .select(dsl.select(member.ID, Routines.uuidGenerateV7()).from(member))
                    .getSQL();
            statementCollector.collect(bookMapping);
            statementCollector.collect(memberMapping);
        }
    }
}
