package net.watchbox.domain.record.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.record.repository.WatchRecordRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WatchLikedService {
    private final WatchRecordRepository watchRecordRepository;
}
