package com.opitzconsulting.cattlecrew.jooqmigration.generator;

import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.Tables.BOOK;
import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.Tables.CHECKOUT;
import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.Member.MEMBER;

import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.records.BookRecord;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.records.CheckoutRecord;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.records.MemberRecord;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import net.datafaker.providers.base.Book;
import org.jetbrains.annotations.NotNull;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.InsertReturningStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StagingDataGeneratorTest {
    public static final int MIN_NUMBER_OF_BOOKS = 10000;
    public static final int MAX_NUMBER_OF_BOOKS = 100000;
    public static final int MIN_NUMBER_OF_MEMBERS = 1000;
    public static final int MAX_NUMBER_MEMBERS = 10000;
    public static final int MAX_NUMBER_CHECKOUTS = 20;

    @Autowired
    private DSLContext create;

    private int memberSequence = 0;
    private int checkoutSequence = 0;

    @Test
    void testGenerateStagingData() {
        Faker faker = new Faker(Locale.GERMANY);
        Collection<BookRecord> books;
        ArrayList<MemberRecord> members;
        Collection<CheckoutRecord> checkoutRecords = new LinkedList<>();
        books = faker.collection(() -> createBookRecord(faker))
                .minLen(MIN_NUMBER_OF_BOOKS)
                .maxLen(MAX_NUMBER_OF_BOOKS)
                .generate();
        members = faker.collection(() -> createMemberRecord(faker))
                .minLen(MIN_NUMBER_OF_MEMBERS)
                .maxLen(MAX_NUMBER_MEMBERS)
                .generate();
        books.forEach(bookRecord -> {
            Date latestReturnDate = java.sql.Date.valueOf(bookRecord.getPublished());
            for (int i = 0; i < faker.random().nextInt(0, MAX_NUMBER_CHECKOUTS); i++) {
                CheckoutRecord checkoutRecord = checkoutRecord(bookRecord, faker, members, latestReturnDate);
                checkoutRecords.add(checkoutRecord);
                latestReturnDate = java.sql.Date.valueOf(checkoutRecord.getCheckoutDate());
            }
        });
        create.transaction((Configuration trx) -> {
            trx.dsl().truncateTable(BOOK).cascade().execute();
            trx.dsl().truncateTable(MEMBER).cascade().execute();
            trx.dsl()
                    .batch(books.stream()
                            .map(bookRecord -> create.insertInto(BOOK)
                                    .columns(bookRecord.fields())
                                    .values(Arrays.stream(bookRecord.fields())
                                            .map(bookRecord::getValue)
                                            .toArray())
                                    .onConflictDoNothing())
                            .toList()
                            .toArray(InsertReturningStep[]::new))
                    .execute();
            trx.dsl().batchInsert(members).execute();
            trx.dsl().batchInsert(checkoutRecords).execute();
        });
    }

    @NotNull
    private CheckoutRecord checkoutRecord(
            BookRecord bookRecord, Faker faker, ArrayList<MemberRecord> memberRecords, Date latestReturnDate) {
        CheckoutRecord checkoutRecord = create.newRecord(CHECKOUT);
        checkoutRecord.setId(checkoutSequence++);

        // create with datafaker faker a date between the published date and 20 years later
        Date toDate = java.sql.Date.valueOf(bookRecord.getPublished().plusYears(20));
        Date checkoutDate = faker.date().between(latestReturnDate, toDate);
        checkoutRecord.setCheckoutDate(
                checkoutDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        checkoutRecord.setReturnDate(checkoutRecord.getCheckoutDate().plusWeeks(4));

        // create with datafaker a date between the checkout date minus 12 years and the checkout date minus 75 years
        Date birthdayBefore =
                java.sql.Date.valueOf(checkoutRecord.getCheckoutDate().minusYears(12));
        Date birthdayAfter =
                java.sql.Date.valueOf(checkoutRecord.getCheckoutDate().minusYears(75));
        Date birthDay = faker.date().between(birthdayAfter, birthdayBefore);
        // choose a memberecord at random from the memberRecords array
        MemberRecord memberRecord = memberRecords.get(faker.random().nextInt(0, memberRecords.size() - 1));
        memberRecord.setDateOfBirth(
                birthDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        checkoutRecord.setMemberId(memberRecord.getId());
        checkoutRecord.setIsbn13(bookRecord.getIsbn13());
        return checkoutRecord;
    }

    private MemberRecord createMemberRecord(Faker faker) {
        MemberRecord memberRecord = create.newRecord(MEMBER);
        memberRecord.setEmail(faker.internet().emailAddress());
        memberRecord.setId(memberSequence++);
        memberRecord.setFirstName(faker.name().firstName());
        memberRecord.setLastName(faker.name().lastName());
        memberRecord.setDateOfBirth(faker.date().birthday().toLocalDateTime().toLocalDate());
        memberRecord.setPhone(faker.phoneNumber().phoneNumber());
        return memberRecord;
    }

    @NotNull
    private BookRecord createBookRecord(Faker faker) {
        BookRecord bookRecord = create.newRecord(BOOK);
        Book book = faker.book();
        bookRecord.setAuthor(book.author());
        bookRecord.setGenre(book.genre());
        bookRecord.setTitle(book.title());
        bookRecord.setIsbn13(faker.code().isbn13(true));
        bookRecord.setPublisher(book.publisher());
        bookRecord.setPublished(
                faker.date().past(70 * 365, TimeUnit.DAYS).toLocalDateTime().toLocalDate());

        return bookRecord;
    }
}
