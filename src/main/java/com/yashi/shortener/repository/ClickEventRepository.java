package com.yashi.shortener.repository;

import com.yashi.shortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByUrlId(Long urlId);

    @Query("select c.device, count(c) from ClickEvent c where c.urlId = :urlId group by c.device")
    List<Object[]> countByDevice(@Param("urlId") Long urlId);

    @Query("select c.browser, count(c) from ClickEvent c where c.urlId = :urlId group by c.browser")
    List<Object[]> countByBrowser(@Param("urlId") Long urlId);
}