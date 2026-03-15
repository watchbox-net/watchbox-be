package net.watchbox.domain.content.base.dto.detail;

public sealed interface ContentInfo
    permits MovieInfo, TvInfo, PersonInfo {}
