package com.beyond.HanSoom.hotel.repository;

import com.beyond.HanSoom.hotel.domain.HotelDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelDocumentRepository extends ElasticsearchRepository<HotelDocument, String> {
}
