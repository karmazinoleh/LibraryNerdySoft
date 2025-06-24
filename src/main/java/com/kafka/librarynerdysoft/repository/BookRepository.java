package com.kafka.librarynerdysoft.repository;

import com.kafka.librarynerdysoft.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByTitleAndAuthor(String title, String author);
    Optional<Book> findByTitle(String title);
}
