package com.kafka.librarynerdysoft.service;

import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.entity.Borrowing;
import com.kafka.librarynerdysoft.entity.Member;
import com.kafka.librarynerdysoft.repository.BookRepository;
import com.kafka.librarynerdysoft.repository.BorrowingRepository;
import com.kafka.librarynerdysoft.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowingService {
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final MemberRepository memberRepository;

    public BorrowingService(BookRepository bookRepository, BorrowingRepository borrowingRepository, MemberRepository memberRepository) {
        this.bookRepository = bookRepository;
        this.borrowingRepository = borrowingRepository;
        this.memberRepository = memberRepository;
    }
    @Value("${library.max.borrowed.books}")
    private int maxBorrowed;

    public Borrowing borrowBook(Long bookId, Long memberId) {
        // check if member exists
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        // check if book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book Not Found"));
        // check amount of books available
        if (book.getAmount() <= 0) {
            throw new RuntimeException("Book is no available for borrowing"); // todo: custom exceptions
        }
        // check member's limits of borrowing
        if (borrowingRepository.countByMember(member) > maxBorrowed) {
            throw new RuntimeException("Member is no allowed to borrow any more books");
        }
        // check if member already borrowed the book
        if (borrowingRepository.findByBookAndMember(book, member).isPresent()) {
            throw new RuntimeException("Book is already borrowed");
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setBook(book);
        borrowing.setMember(member);

        book.setAmount(book.getAmount() - 1);
        bookRepository.save(book);

        return borrowingRepository.save(borrowing);
    }

    public void returnBook(Long bookId, Long memberId) {
        Member member = memberRepository.findById(memberId).get();
        // !!!
        Book book = bookRepository.findById(bookId).get();

        // increase amount of books
        book.setAmount(book.getAmount() + 1);
        bookRepository.save(book);

        // delete
        borrowingRepository.deleteByBookAndMember(book, member);
    }

    public List<Book> getBooksBorrowedByMemberName(String name){
        Member member = memberRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        List<Borrowing> borrowings = borrowingRepository.findAllByMember(member);

        return borrowings
                .stream().map(Borrowing::getBook).toList();
    }

    public List<String> getDistinctBorrowedBookTitles() {
        return borrowingRepository.findAll().stream()
                .map(b -> b.getBook().getTitle())
                .distinct()
                .toList();
    }

    public Map<String, Long> getBookTitleToBorrowCount() {
        return borrowingRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        borrowing -> borrowing.getBook().getTitle(),
                        Collectors.counting()
                ));
    } // todo: remake it with Query (SQL) in repository.




}
