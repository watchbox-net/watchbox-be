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

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member findByNickname(String nickname) {
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 닉네임 등록, 수정
    // 닉네임 중복 검사
}
