package com.kafka.librarynerdysoft.controller;

import com.kafka.librarynerdysoft.dto.MemberCreatedRequest;
import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.entity.Member;
import com.kafka.librarynerdysoft.repository.MemberRepository;
import com.kafka.librarynerdysoft.service.MemberService;
import jakarta.validation.Valid;
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
    private final MemberService memberService;

    public MemberController(MemberRepository memberRepository, MemberService memberService) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok().body(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) { // test if working
        Optional<Member> member = memberService.getMemberById(id);
        if (member.isPresent()) {
            return ResponseEntity.ok(member.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@Valid @RequestBody MemberCreatedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberCreatedRequest request
            ){
        Optional<Member> member = memberService.updateMember(id, request);
        if (member.isEmpty()){
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(member.get());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberById(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
