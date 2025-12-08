package net.watchpeople.domain.box.service;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.repository.BoxMemberRepository;
import net.watchpeople.domain.box.repository.BoxRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyBoxService {
    private final BoxRepository boxRepository;
    private final BoxMemberRepository boxMemberRepository;


//    public List<MyContent> getMyBoxContentsAll(Member member) {
//    }


}
