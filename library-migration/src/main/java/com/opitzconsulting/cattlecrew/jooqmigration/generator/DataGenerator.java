package com.opitzconsulting.cattlecrew.jooqmigration.generator;

import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.Tables.BOOK;
import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.Tables.CHECKOUT;
import static com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.Member.MEMBER;

import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.records.BookRecord;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.records.CheckoutRecord;
import com.opitzconsulting.cattlecrew.jooqmigration.jooq.staging.tables.records.MemberRecord;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import net.datafaker.providers.base.Book;
import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.InsertReturningStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
@Command
public class DataGenerator {
    public static final int MIN_NUMBER_OF_BOOKS = 10000;
    public static final int MAX_NUMBER_OF_BOOKS = 50000;
    public static final int MIN_NUMBER_OF_MEMBERS = 500;
    public static final int MAX_NUMBER_MEMBERS = 5000;
    public static final int MAX_NUMBER_CHECKOUTS = 20;
    public static final int PARTITION_SIZE = 10000;

    @Autowired
    private DSLContext create;

    private int memberSequence = 0;
    private int checkoutSequence = 0;

    public static void main(String[] args) {
        SpringApplication.run(DataGenerator.class, args);
        DataGenerator stagingDataGeneratorTest = new DataGenerator();
        stagingDataGeneratorTest.generateData();
    }

    private static LocalDate fakeCheckoutDate(BookRecord bookRecord, Faker faker, Date latestReturnDate) {
        java.sql.Date noCheckoutAfterDate =
                java.sql.Date.valueOf(bookRecord.getPublished().plusYears(20));
        Date now = new Date();
        Date toDate = noCheckoutAfterDate.after(now) ? now : noCheckoutAfterDate;
        Date checkoutDate = faker.date().between(latestReturnDate, toDate);
        return convertToLocalDate(checkoutDate);
    }

    private static LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date fakeBirthday(Faker faker, CheckoutRecord checkoutRecord) {
        Date birthdayBefore =
                java.sql.Date.valueOf(checkoutRecord.getCheckoutDate().minusYears(12));
        Date birthdayAfter =
                java.sql.Date.valueOf(checkoutRecord.getCheckoutDate().minusYears(75));
        return faker.date().between(birthdayAfter, birthdayBefore);
    }

    private static LocalDate fakeActualReturnDate(Faker faker, CheckoutRecord checkoutRecord) {
        Date actualReturnAfter =
                java.sql.Date.valueOf(checkoutRecord.getCheckoutDate().plusDays(1));
        Date actualReturnBefore =
                java.sql.Date.valueOf(checkoutRecord.getReturnDate().plusWeeks(8));
        return convertToLocalDate(faker.date().between(actualReturnAfter, actualReturnBefore));
    }

    @Command(command = "generateData")
    private void generateData() {
        Faker faker = new Faker(
                Locale.GERMANY,
                new Random(1234)); // we want to have the same data every time for this blog post so we use a fixed seed
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
        create.truncateTable(BOOK).cascade().execute();
        create.truncateTable(MEMBER).cascade().execute();
        ListUtils.partition(books.stream().toList(), PARTITION_SIZE).forEach(bookRecords -> {
            create.batch(bookRecords.stream()
                            .map(bookRecord -> create.insertInto(BOOK)
                                    .columns(bookRecord.fields())
                                    .values(Arrays.stream(bookRecord.fields())
                                            .map(bookRecord::getValue)
                                            .toArray())
                                    .onConflictDoNothing())
                            .toList()
                            .toArray(InsertReturningStep[]::new))
                    .execute();
        });
        create.batchInsert(members).execute();
        ListUtils.partition(checkoutRecords.stream().toList(), PARTITION_SIZE).forEach(checkoutRecordsPartition -> {
            create.batchInsert(checkoutRecordsPartition).execute();
        });
    }

    @NotNull
    private CheckoutRecord checkoutRecord(
            BookRecord bookRecord, Faker faker, List<MemberRecord> memberRecords, Date latestReturnDate) {
        CheckoutRecord checkoutRecord = create.newRecord(CHECKOUT);
        checkoutRecord.setId(checkoutSequence++);
        // create with datafaker faker a date between the published date and 20 years later
        checkoutRecord.setCheckoutDate(fakeCheckoutDate(bookRecord, faker, latestReturnDate));
        checkoutRecord.setReturnDate(checkoutRecord.getCheckoutDate().plusWeeks(4));
        checkoutRecord.setActualReturnDate(fakeActualReturnDate(faker, checkoutRecord));
        // create with datafaker a date between the checkout date minus 12 years and the checkout date minus 75 years
        Date birthDay = fakeBirthday(faker, checkoutRecord);
        MemberRecord memberRecord = memberRecords.get(faker.random().nextInt(0, memberRecords.size() - 1));
        memberRecord.setDateOfBirth(convertToLocalDate(birthDay));
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
