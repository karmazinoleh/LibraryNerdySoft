package com.kafka.librarynerdysoft.services;
import com.kafka.librarynerdysoft.dto.MemberCreatedRequest;
import com.kafka.librarynerdysoft.entity.Member;
import com.kafka.librarynerdysoft.repository.BorrowingRepository;
import com.kafka.librarynerdysoft.repository.MemberRepository;
import com.kafka.librarynerdysoft.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService Tests")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private MemberCreatedRequest testRequest;

    @BeforeEach
    void setUp() {
        testMember = createTestMember(1L, "John Doe");
        testRequest = createMemberRequest("John Doe");
    }

    @Nested
    @DisplayName("getAllMembers method")
    class GetAllMembersTests {

        @Test
        @DisplayName("Should return all members")
        void shouldReturnAllMembers() {
            // Given
            List<Member> members = Arrays.asList(
                    testMember,
                    createTestMember(2L, "Jane Doe")
            );
            when(memberRepository.findAll()).thenReturn(members);

            // When
            List<Member> result = memberService.getAllMembers();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(members);
        }
    }

    @Nested
    @DisplayName("getMemberById method")
    class GetMemberByIdTests {

        @Test
        @DisplayName("Should return member when found")
        void shouldReturnMemberWhenFound() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

            // When
            Optional<Member> result = memberService.getMemberById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testMember);
        }

        @Test
        @DisplayName("Should return empty when member not found")
        void shouldReturnEmptyWhenMemberNotFound() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // When
            Optional<Member> result = memberService.getMemberById(1L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createMember method")
    class CreateMemberTests {

        @Test
        @DisplayName("Should create member successfully")
        void shouldCreateMemberSuccessfully() {
            // Given
            when(memberRepository.save(any(Member.class))).thenReturn(testMember);

            // When
            Member result = memberService.createMember(testRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("John Doe");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("Should set member date when creating member")
        void shouldSetMemberDateWhenCreatingMember() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                member.setId(1L);
                return member;
            });

            // When
            Member result = memberService.createMember(testRequest);

            // Then
            assertThat(result.getMemberDate()).isAfter(beforeCreation);
            assertThat(result.getMemberDate()).isBefore(LocalDateTime.now().plusSeconds(1));
        }
    }

    @Nested
    @DisplayName("updateMember method")
    class UpdateMemberTests {

        @Test
        @DisplayName("Should update member when found")
        void shouldUpdateMemberWhenFound() {
            // Given
            MemberCreatedRequest updateRequest = createMemberRequest("Updated Name");
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(memberRepository.save(testMember)).thenReturn(testMember);

            // When
            Optional<Member> result = memberService.updateMember(1L, updateRequest);

            // Then
            assertThat(result).isPresent();
            assertThat(testMember.getName()).isEqualTo("Updated Name");
            verify(memberRepository).save(testMember);
        }

        @Test
        @DisplayName("Should return empty when member not found for update")
        void shouldReturnEmptyWhenMemberNotFoundForUpdate() {
            // Given
            MemberCreatedRequest updateRequest = createMemberRequest("Updated Name");
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // When
            Optional<Member> result = memberService.updateMember(1L, updateRequest);

            // Then
            assertThat(result).isEmpty();
            verify(memberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteMember method")
    class DeleteMemberTests {

        @Test
        @DisplayName("Should delete member when not borrowing books")
        void shouldDeleteMemberWhenNotBorrowingBooks() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(borrowingRepository.countByMember(testMember)).thenReturn(0L);

            // When
            memberService.deleteMember(1L);

            // Then
            verify(memberRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when member not found for deletion")
        void shouldThrowExceptionWhenMemberNotFoundForDeletion() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberService.deleteMember(1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Member with id 1 not found");

            verify(memberRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw RuntimeException when member is borrowing books")
        void shouldThrowExceptionWhenMemberIsBorrowingBooks() {
            // Given
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(borrowingRepository.countByMember(testMember)).thenReturn(1L);

            // When & Then
            assertThatThrownBy(() -> memberService.deleteMember(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Member with id 1 is borrowing a book");

            verify(memberRepository, never()).deleteById(any());
        }
    }

    // Helper methods
    private Member createTestMember(Long id, String name) {
        Member member = new Member();
        member.setId(id);
        member.setName(name);
        member.setMemberDate(LocalDateTime.now());
        return member;
    }

    private MemberCreatedRequest createMemberRequest(String name) {
        MemberCreatedRequest request = new MemberCreatedRequest();
        request.setName(name);
        return request;
    }
}