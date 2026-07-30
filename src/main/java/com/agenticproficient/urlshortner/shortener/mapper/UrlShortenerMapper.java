package com.agenticproficient.urlshortner.shortener.mapper;

import java.time.Instant;
import java.util.List;

import com.agenticproficient.urlshortner.shortener.dto.AnalyticsResponse;
import com.agenticproficient.urlshortner.shortener.dto.RecentClickResponse;
import com.agenticproficient.urlshortner.shortener.dto.UrlResponse;
import com.agenticproficient.urlshortner.shortener.entity.ClickEvent;
import com.agenticproficient.urlshortner.shortener.entity.ShortUrl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UrlShortenerMapper {

	@Mapping(target = "shortUrl", expression = "java(baseUrl + \"/\" + entity.getShortCode())")
	UrlResponse toResponse(ShortUrl entity, String baseUrl);

	RecentClickResponse toRecentClickResponse(ClickEvent event);

	@Mapping(target = "totalClicks", source = "entity.accessCount")
	AnalyticsResponse toAnalyticsResponse(ShortUrl entity, long uniqueVisitors, Instant lastAccessedAt,
			List<RecentClickResponse> recentClicks);
}
