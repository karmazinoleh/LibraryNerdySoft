package com.kafka.librarynerdysoft.repository;

import com.kafka.librarynerdysoft.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
