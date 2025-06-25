package com.kafka.librarynerdysoft.services;

import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.entity.Borrowing;
import com.kafka.librarynerdysoft.entity.Member;
import com.kafka.librarynerdysoft.repository.BookRepository;
import com.kafka.librarynerdysoft.repository.BorrowingRepository;
import com.kafka.librarynerdysoft.repository.MemberRepository;
import com.kafka.librarynerdysoft.service.BorrowingService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowingService Tests")
class BorrowingServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private Member testMember;
    private Book testBook;
    private Borrowing testBorrowing;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(borrowingService, "maxBorrowed", 3);

        testMember = createTestMember(1L, "John Doe");
        testBook = createTestBook(1L, "Test Book", "Test Author", 5);
        testBorrowing = createTestBorrowing(testBook, testMember);
    }

    @Nested
    @DisplayName("borrowBook method")
    class BorrowBookTests {

        @Test
        @DisplayName("Should successfully borrow book when all conditions are met")
        void shouldBorrowBookSuccessfully() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(borrowingRepository.countByMember(testMember)).thenReturn(2L);
            when(borrowingRepository.findByBookAndMember(testBook, testMember)).thenReturn(Optional.empty());
            when(borrowingRepository.save(any(Borrowing.class))).thenReturn(testBorrowing);

            // When
            Borrowing result = borrowingService.borrowBook(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBook()).isEqualTo(testBook);
            assertThat(result.getMember()).isEqualTo(testMember);
            assertThat(testBook.getAmount()).isEqualTo(4);

            verify(bookRepository).save(testBook);
            verify(borrowingRepository).save(any(Borrowing.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when member not found")
        void shouldThrowExceptionWhenMemberNotFound() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(1L, 1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Member Not Found");

            verify(bookRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when book not found")
        void shouldThrowExceptionWhenBookNotFound() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(bookRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(1L, 1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Book Not Found");
        }

        @Test
        @DisplayName("Should throw RuntimeException when book is not available")
        void shouldThrowExceptionWhenBookNotAvailable() {
            // Given
            testBook.setAmount(0);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(1L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Book is no available for borrowing");
        }

        @Test
        @DisplayName("Should throw RuntimeException when member exceeds borrow limit")
        void shouldThrowExceptionWhenMemberExceedsBorrowLimit() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(borrowingRepository.countByMember(testMember)).thenReturn(4L);

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(1L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Member is no allowed to borrow any more books");
        }

        @Test
        @DisplayName("Should throw RuntimeException when book already borrowed by member")
        void shouldThrowExceptionWhenBookAlreadyBorrowed() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(borrowingRepository.countByMember(testMember)).thenReturn(2L);
            when(borrowingRepository.findByBookAndMember(testBook, testMember)).thenReturn(Optional.of(testBorrowing));

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(1L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Book is already borrowed");
        }
    }

    @Nested
    @DisplayName("returnBook method")
    class ReturnBookTests {

        @Test
        @DisplayName("Should successfully return book")
        void shouldReturnBookSuccessfully() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

            // When
            borrowingService.returnBook(1L, 1L);

            // Then
            assertThat(testBook.getAmount()).isEqualTo(6);
            verify(bookRepository).save(testBook);
            verify(borrowingRepository).deleteByBookAndMember(testBook, testMember);
        }
    }

    @Nested
    @DisplayName("getBooksBorrowedByMemberName method")
    class GetBooksBorrowedByMemberNameTests {

        @Test
        @DisplayName("Should return books borrowed by member")
        void shouldReturnBooksBorrowedByMember() {
            // Given
            String memberName = "John Doe";
            List<Borrowing> borrowings = Arrays.asList(
                    createTestBorrowing(testBook, testMember),
                    createTestBorrowing(createTestBook(2L, "Another Book", "Another Author", 3), testMember)
            );

            when(memberRepository.findByName(memberName)).thenReturn(Optional.of(testMember));
            when(borrowingRepository.findAllByMember(testMember)).thenReturn(borrowings);

            // When
            List<Book> result = borrowingService.getBooksBorrowedByMemberName(memberName);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Book::getTitle)
                    .containsExactly("Test Book", "Another Book");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when member not found by name")
        void shouldThrowExceptionWhenMemberNotFoundByName() {
            // Given
            String memberName = "Non-existent Member";
            when(memberRepository.findByName(memberName)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> borrowingService.getBooksBorrowedByMemberName(memberName))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Member Not Found");
        }
    }

    @Nested
    @DisplayName("getDistinctBorrowedBookTitles method")
    class GetDistinctBorrowedBookTitlesTests {

        @Test
        @DisplayName("Should return distinct borrowed book titles")
        void shouldReturnDistinctBorrowedBookTitles() {
            // Given
            List<Borrowing> borrowings = Arrays.asList(
                    createTestBorrowing(testBook, testMember),
                    createTestBorrowing(testBook, createTestMember(2L, "Jane Doe")),
                    createTestBorrowing(createTestBook(2L, "Another Book", "Another Author", 3), testMember)
            );

            when(borrowingRepository.findAll()).thenReturn(borrowings);

            // When
            List<String> result = borrowingService.getDistinctBorrowedBookTitles();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder("Test Book", "Another Book");
        }
    }

    @Nested
    @DisplayName("getBookTitleToBorrowCount method")
    class GetBookTitleToBorrowCountTests {

        @Test
        @DisplayName("Should return book title to borrow count map")
        void shouldReturnBookTitleToBorrowCountMap() {
            // Given
            List<Borrowing> borrowings = Arrays.asList(
                    createTestBorrowing(testBook, testMember),
                    createTestBorrowing(testBook, createTestMember(2L, "Jane Doe")),
                    createTestBorrowing(createTestBook(2L, "Another Book", "Another Author", 3), testMember)
            );

            when(borrowingRepository.findAll()).thenReturn(borrowings);

            // When
            Map<String, Long> result = borrowingService.getBookTitleToBorrowCount();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get("Test Book")).isEqualTo(2L);
            assertThat(result.get("Another Book")).isEqualTo(1L);
        }
    }

    // Helper methods
    private Member createTestMember(Long id, String name) {
        Member member = new Member();
        member.setId(id);
        member.setName(name);
        member.setMemberDate(LocalDateTime.now());
        return member;
    }

    private Book createTestBook(Long id, String title, String author, int amount) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setAmount(amount);
        return book;
    }

    private Borrowing createTestBorrowing(Book book, Member member) {
        Borrowing borrowing = new Borrowing();
        borrowing.setBook(book);
        borrowing.setMember(member);
        return borrowing;
    }
}