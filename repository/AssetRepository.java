package com.TruMIS.assetservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.transaction.Asset;

import jakarta.persistence.Tuple;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
	Boolean existsByServiceTag(String serviceTag);

	@Query("SELECT a FROM Asset a LEFT JOIN Employee e ON a.issuedTo.id=e.id WHERE ((:isClient IS NULL) OR (a.isClient = :isClient)) AND "
			+ "(a.isActive = true OR a.isActive IS NULL) AND " + "(:searchTerm IS NULL OR "
			+ "(LOWER(e.mailId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR "
			+ "(LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR "
			+ "(LOWER(a.serviceTag) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR "
			+ "(LOWER(a.assetModel) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR "
			+ "(LOWER(a.assetType.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR "
			+ "(LOWER(a.assetMaker.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))))")
	Page<Asset> findAssetsWithSearchCriteria(@Param("searchTerm") String searchCriteria,
			@Param("isClient") Boolean isClient, Pageable pageableRequest);

	@Query("SELECT count(a.id) FROM Asset a WHERE (a.isActive = true OR a.isActive IS NULL) AND (a.isClient = false) and a.assetLocation.name not in :locations")
	Long findAllOtherActiveAssetsByLocationWise(@Param("locations") List<String> locations);

	@Query("SELECT al.name, count(*) FROM Asset a LEFT JOIN AssetLocation al on a.assetLocation.code=al.code WHERE (a.isActive = true OR a.isActive IS NULL) AND (a.isClient = false) group by al.code")
	List<Tuple> findAllActiveAssetsByLocationWise();

	List<Asset> findByIsClient(Boolean isClient);

	@Query("SELECT count(a.id) FROM Asset a WHERE a.isActive = true OR a.isActive IS NULL ")
	Long findAllActiveAssets();

	@Query("SELECT count(a.id) FROM Asset a WHERE (a.isActive = true OR a.isActive IS NULL) AND (a.isClient = true) AND (:b IS NULL OR a.isIssued = :b) ")
	Long findAllAssignedOrNotAssigned(@Param("b") Boolean b);

	List<Asset> findByIsActiveAndIsClient(Boolean isActive, Boolean isClient);

	List<Asset> findByIsActiveAndIsIssued(Boolean isActive, Boolean isIssued);

	List<Asset> findByIsActiveAndAssetLocationName(Boolean isActive, String location);

	List<Asset> findByIsActive(Boolean isActive);
}
