package net.watchpeople.domain.box.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.entity.Box;
import net.watchpeople.domain.box.entity.BoxContent;
import net.watchpeople.domain.box.entity.BoxMember;
import net.watchpeople.domain.box.enums.BoxType;
import net.watchpeople.domain.box.repository.BoxMemberRepository;
import net.watchpeople.domain.box.repository.BoxRepository;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.global.dto.response.exception.CustomException;
import net.watchpeople.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyBoxService {
    private final BoxRepository boxRepository;
    private final BoxMemberRepository boxMemberRepository;

    @Transactional
    public void createMyBox(Member member) {
        boxRepository.save(Box.builder()
                .boxType(BoxType.MY)
                .owner(member)
                .build()
        );
    }

    public List<BoxContent> getMyBoxContentsAll(Member member) {
        BoxMember boxMember = boxMemberRepository.findByMemberAndBox_BoxType(member, BoxType.MY)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_MEMBER_NOT_FOUND, member.getMemberId()));
        return boxMember.getBox().getBoxContents();
    }


}
