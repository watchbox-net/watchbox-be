package net.watchbox.domain.box.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.repository.content.MyContentRepository;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyBoxContentService {
    private final MyContentRepository myContentRepository;

    // 내 박스에 콘텐츠 추가
    public void addContentToMyBox(Member member, Long tmdbId) {

    }


//    public List<MyContent> getMyBoxContentsAll(Member member) {
//    }


}
