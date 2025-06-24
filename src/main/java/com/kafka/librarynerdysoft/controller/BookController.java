package com.kafka.librarynerdysoft.controller;

import com.kafka.librarynerdysoft.dto.BookCreatedRequest;
import com.kafka.librarynerdysoft.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kafka.librarynerdysoft.entity.Book;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") Long id) {
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            return ResponseEntity.ok(book.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody BookCreatedRequest request) {
        // todo: BookResponcse dto
        Book book = new Book();
        // todo: mapStruct
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setAmount(request.getAmount());

        Book savedBook = bookRepository.save(book);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable("id") Long id,
            @RequestBody BookCreatedRequest request
            ) {

        Optional<Book> book = bookRepository.findById(id);

        if (book.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        book.get().setTitle(request.getTitle());
        book.get().setAuthor(request.getAuthor());
        book.get().setAmount(request.getAmount());

        Book updatedBook = bookRepository.save(book.get());
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable("id") Long id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }




}
