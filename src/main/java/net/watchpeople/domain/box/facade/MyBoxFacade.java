package net.watchpeople.domain.box.facade;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.service.MyBoxService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyBoxFacade {
    private final MyBoxService myBoxService;

    // 마이 박스 컨텐츠 리스트 조회
    // 마이 박스에 컨텐츠 추가
    // 마이 박스에 컨텐츠 삭제
}
