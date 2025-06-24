package com.kafka.librarynerdysoft.repository;

import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.entity.Borrowing;
import com.kafka.librarynerdysoft.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    Optional<Borrowing> findByBookAndMember(Book book, Member member);

    long countByMember(Member member);

    boolean existsByMember(Member member);

    void deleteByBookAndMember(Book book, Member member);
}