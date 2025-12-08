package net.watchpeople.domain.box.service;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.repository.BoxMemberRepository;
import net.watchpeople.domain.box.repository.SharedBoxRepository;
import net.watchpeople.domain.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyBoxService {
    private final SharedBoxRepository sharedBoxRepository;
    private final BoxMemberRepository boxMemberRepository;

    // 내 박스에 콘텐츠 추가
    public void addContentToMyBox(Member member, Long tmdbId) {

    }


//    public List<MyContent> getMyBoxContentsAll(Member member) {
//    }


}
