package com.kafka.librarynerdysoft.controller;

import com.kafka.librarynerdysoft.dto.BorrowBookRequest;
import com.kafka.librarynerdysoft.entity.Borrowing;
import com.kafka.librarynerdysoft.repository.BorrowingRepository;
import com.kafka.librarynerdysoft.service.BorrowingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
