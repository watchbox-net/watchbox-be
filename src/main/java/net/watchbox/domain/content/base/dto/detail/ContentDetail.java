package net.watchbox.domain.content.base.dto.detail;

public sealed interface ContentDetail
    permits MovieDetail, TvDetail, PersonDetail {}
