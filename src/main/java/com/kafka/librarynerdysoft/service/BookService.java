package com.kafka.librarynerdysoft.service;

import com.kafka.librarynerdysoft.dto.BookCreatedRequest;
import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book createBook(BookCreatedRequest request) {

        Optional<Book> optionalBook = bookRepository.findByTitleAndAuthor(
                request.getTitle(), request.getAuthor());

        if (optionalBook.isPresent()) {
            Book existingBook = optionalBook.get();
            existingBook.setAmount(existingBook.getAmount() + 1);
            return bookRepository.save(existingBook);
        } else {
            Book newBook = new Book();
            newBook.setTitle(request.getTitle());
            newBook.setAuthor(request.getAuthor());
            newBook.setAmount(1);
            return bookRepository.save(newBook);
        }
    }

    public Optional<Book> updateBook(Long id, BookCreatedRequest request){
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            return Optional.of(bookRepository.save(book));
        } else {
            return Optional.empty();
        }
    }

    public void deleteBook(Long id) {
        // check if book is borrowed
        bookRepository.deleteById(id);
    }


}
