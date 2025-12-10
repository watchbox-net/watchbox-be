package net.watchbox.domain.box.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.content.MyBoxContent;
import net.watchbox.domain.box.repository.content.MyBoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyBoxContentService {
    private final MyBoxContentRepository myBoxContentRepository;

    // 내 박스에 콘텐츠 추가
    @Transactional
    public void addContentToMyBox(Content content, Member member) {
        myBoxContentRepository.save(MyBoxContent.builder()
                .content(content)
                .member(member)
                .build());
    }


//    public List<MyContent> getMyBoxContentsAll(Member member) {
//    }


}
