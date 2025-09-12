package com.appcenter.BJJ.domain.banner;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    @Query("SELECT MAX(b.sortOrder) FROM Banner b")
    Integer findMaxSortOrder();

    @Query("SELECT b FROM Banner b ORDER BY b.sortOrder ASC NULLS LAST")
    List<Banner> findAllOrderBySortOrderAscNullsLast();

    List<Banner> findBySortOrderGreaterThan(Integer sortOrder);

    List<Banner> findBySortOrderGreaterThanEqual(Integer sortOrder);

    List<Banner> findBySortOrderBetween(Integer start, Integer end);
}
