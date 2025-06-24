package com.kafka.librarynerdysoft.service;

import com.kafka.librarynerdysoft.dto.BookCreatedRequest;
import com.kafka.librarynerdysoft.dto.MemberCreatedRequest;
import com.kafka.librarynerdysoft.entity.Book;
import com.kafka.librarynerdysoft.entity.Member;
import com.kafka.librarynerdysoft.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    public Member createMember(MemberCreatedRequest request) {
        Member newMember = new Member();
        newMember.setName(request.getName());
        newMember.setMemberDate(LocalDateTime.now());
        return memberRepository.save(newMember);
    }

    public Optional<Member> updateMember(Long id, MemberCreatedRequest request){
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setName(request.getName());
            return Optional.of(memberRepository.save(member));
        } else {
            return Optional.empty();
        }
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }
}
