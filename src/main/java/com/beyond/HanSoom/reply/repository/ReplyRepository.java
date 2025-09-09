package com.beyond.HanSoom.reply.repository;

import com.beyond.HanSoom.reply.domain.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @Query("""
        select count(rp)
        from Reply rp
        join rp.review rv
        where rv.hotel.id = :hotelId
    """)
    long countByHotelId(@Param("hotelId") Long hotelId);
}
