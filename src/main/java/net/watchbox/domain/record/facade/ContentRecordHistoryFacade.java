package net.watchbox.domain.record.facade;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.record.service.history.ContentRecordHistoryCommandService;
import net.watchbox.domain.record.service.history.ContentRecordHistoryQueryService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Observed
public class ContentRecordHistoryFacade {
    private final ContentRecordHistoryCommandService contentRecordHistoryCommandService;
    private final ContentRecordHistoryQueryService contentRecordHistoryQueryService;
}
