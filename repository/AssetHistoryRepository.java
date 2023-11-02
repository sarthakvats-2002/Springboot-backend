package com.TruMIS.assetservice.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.transaction.AssetHistory;

@Repository
public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {

	@Query("SELECT a FROM AssetHistory a WHERE " + "(:searchTerm IS NULL OR "
			+ "(LOWER(a.empName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(a.empEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(a.serviceTag) LIKE LOWER(CONCAT('%', :searchTerm, '%')))) "
			+ "AND (( cast(:startDate as LocalDate) IS NULL AND cast(:endDate as LocalDate) IS NULL) OR "
			+ "(cast(:startDate as LocalDate) IS NULL AND a.toDate <= :endDate) OR "
			+ "(cast(:endDate as LocalDate) IS NULL AND a.fromDate >= :startDate) OR "
			+ "(a.fromDate >= :startDate AND a.toDate <= :endDate))")
	Page<AssetHistory> findAssetHistoryWithSearchCriteria(@Param("searchTerm") String searchCriteria,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageableRequest);
}
