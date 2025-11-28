package net.watchpeople.domain.member.service;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.domain.member.repository.MemberRepository;
import net.watchpeople.global.dto.response.exception.CustomException;
import net.watchpeople.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member findById(Long tokenMemberId) {
        return memberRepository.findById(tokenMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
