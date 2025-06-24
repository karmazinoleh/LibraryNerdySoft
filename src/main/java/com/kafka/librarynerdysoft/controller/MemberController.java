package com.kafka.librarynerdysoft.controller;

import com.kafka.librarynerdysoft.dto.MemberCreatedRequest;
import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.entity.Member;
import com.kafka.librarynerdysoft.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) { // test if working
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()) {
            return ResponseEntity.ok(member.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody MemberCreatedRequest request) {
        Member savedMember = new Member();
        savedMember.setName(request.getName());
        savedMember.setMemberDate(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(memberRepository.save(savedMember));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
            @PathVariable Long id,
            @RequestBody MemberCreatedRequest request
            ){
        Optional<Member> member = memberRepository.findById(id);
        if (member.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        member.get().setName(request.getName());
        member.get().setMemberDate(LocalDateTime.now());
        return ResponseEntity.ok(memberRepository.save(member.get()));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMemberById(@PathVariable Long id) {
        if (!memberRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        memberRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
