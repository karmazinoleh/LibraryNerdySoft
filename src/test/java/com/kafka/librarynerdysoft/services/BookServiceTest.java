package com.kafka.librarynerdysoft.services;

import com.kafka.librarynerdysoft.dto.BookCreatedRequest;
import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.repository.BookRepository;
import com.kafka.librarynerdysoft.repository.BorrowingRepository;
import com.kafka.librarynerdysoft.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private BookCreatedRequest testRequest;

    @BeforeEach
    void setUp() {
        testBook = createTestBook(1L, "Test Book", "Test Author", 1);
        testRequest = createBookRequest("Test Book", "Test Author");
    }

    @Nested
    class GetAllBooksTests {

        @Test
        void shouldReturnAllBooks() {
            // Given
            List<Book> books = Arrays.asList(
                    testBook,
                    createTestBook(2L, "Another Book", "Another Author", 2)
            );
            when(bookRepository.findAll()).thenReturn(books);

            // When
            List<Book> result = bookService.getAllBooks();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(books);
        }
    }

    @Nested
    class GetBookByIdTests {

        @Test
        void shouldReturnBookWhenFound() {
            // Given
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

            // When
            Optional<Book> result = bookService.getBookById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testBook);
        }

        @Test
        void shouldReturnEmptyWhenBookNotFound() {
            // Given
            when(bookRepository.findById(1L)).thenReturn(Optional.empty());

            // When
            Optional<Book> result = bookService.getBookById(1L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class CreateBookTests {

        @Test
        void shouldCreateNewBookWhenBookDoesntExist() {
            // Given
            when(bookRepository.findByTitleAndAuthor("Test Book", "Test Author"))
                    .thenReturn(Optional.empty());
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);

            // When
            Book result = bookService.createBook(testRequest);

            // Then
            assertThat(result).isNotNull();
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        void shouldIncrementAmountWhenBookExists() {
            // Given
            Book existingBook = createTestBook(1L, "Test Book", "Test Author", 3);
            when(bookRepository.findByTitleAndAuthor("Test Book", "Test Author"))
                    .thenReturn(Optional.of(existingBook));
            when(bookRepository.save(existingBook)).thenReturn(existingBook);

            // When
            Book result = bookService.createBook(testRequest);

            // Then
            assertThat(result.getAmount()).isEqualTo(4);
            verify(bookRepository).save(existingBook);
        }
    }

    @Nested
    class UpdateBookTests {

        @Test
        void shouldUpdateBookWhenFound() {
            // Given
            BookCreatedRequest updateRequest = createBookRequest("Updated Title", "Updated Author");
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(testBook)).thenReturn(testBook);

            // When
            Optional<Book> result = bookService.updateBook(1L, updateRequest);

            // Then
            assertThat(result).isPresent();
            assertThat(testBook.getTitle()).isEqualTo("Updated Title");
            assertThat(testBook.getAuthor()).isEqualTo("Updated Author");
            verify(bookRepository).save(testBook);
        }

        @Test
        void shouldReturnEmptyWhenBookNotFoundForUpdate() {
            // Given
            BookCreatedRequest updateRequest = createBookRequest("Updated Title", "Updated Author");
            when(bookRepository.findById(1L)).thenReturn(Optional.empty());

            // When
            Optional<Book> result = bookService.updateBook(1L, updateRequest);

            // Then
            assertThat(result).isEmpty();
            verify(bookRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteBookTests {

        @Test
        void shouldDeleteBookWhenNotBorrowed() {
            // Given
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(borrowingRepository.countByBook(testBook)).thenReturn(0L);

            // When
            bookService.deleteBook(1L);

            // Then
            verify(bookRepository).deleteById(1L);
        }

        @Test
        void shouldThrowExceptionWhenBookNotFoundForDeletion() {
            // Given
            when(bookRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Book with id 1 not found");

            verify(bookRepository, never()).deleteById(any());
        }

        @Test
        void shouldThrowExceptionWhenBookIsBorrowed() {
            // Given
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(borrowingRepository.countByBook(testBook)).thenReturn(1L);

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Member with id 1 is borrowing a book");

            verify(bookRepository, never()).deleteById(any());
        }
    }

    // Helper methods
    private Book createTestBook(Long id, String title, String author, int amount) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setAmount(amount);
        return book;
    }

    private BookCreatedRequest createBookRequest(String title, String author) {
        BookCreatedRequest request = new BookCreatedRequest();
        request.setTitle(title);
        request.setAuthor(author);
        return request;
    }
}