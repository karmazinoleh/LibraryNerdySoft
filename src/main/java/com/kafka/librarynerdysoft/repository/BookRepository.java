package com.kafka.librarynerdysoft.repository;

import com.kafka.librarynerdysoft.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
