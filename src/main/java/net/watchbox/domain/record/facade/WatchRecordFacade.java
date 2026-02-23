package net.watchbox.domain.record.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.record.service.WatchLikedService;
import net.watchbox.domain.record.service.WatchRecordService;
import net.watchbox.domain.record.service.WatchStatusService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchRecordFacade {
    private final WatchRecordService watchRecordService;
    private final WatchStatusService watchStatusService;
    private final WatchLikedService watchLikedService;
}
