package com.kafka.librarynerdysoft.controller;

import com.kafka.librarynerdysoft.dto.BorrowBookRequest;
import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.entity.Borrowing;
import com.kafka.librarynerdysoft.repository.BorrowingRepository;
import com.kafka.librarynerdysoft.service.BorrowingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrow")
public class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping
    public ResponseEntity<Borrowing> borrowBook(@RequestBody BorrowBookRequest request) {
        return ResponseEntity.ok().body(borrowingService.borrowBook(request.getBookId(), request.getMemberId()));
    }

    @DeleteMapping
    public ResponseEntity<Void> returnBook(@RequestBody BorrowBookRequest request) {
        borrowingService.returnBook(request.getBookId(), request.getMemberId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/member/{memberName}")
    public ResponseEntity<List<Book>> getBooksBorrowedByMember(@PathVariable String memberName) {
        List<Book> books = borrowingService.getBooksBorrowedByMemberName(memberName);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/distinct-names")
    public ResponseEntity<List<String>> getDistinctBorrowedBookNames() {
        List<String> bookNames = borrowingService.getDistinctBorrowedBookTitles();
        return ResponseEntity.ok(bookNames);
    }

    @GetMapping("/books/distinct-names-with-count")
    public ResponseEntity<Map<String, Long>> getDistinctBorrowedBookNamesWithCount() {
        Map<String, Long> result = borrowingService.getBookTitleToBorrowCount();
        return ResponseEntity.ok(result);
    }
}
