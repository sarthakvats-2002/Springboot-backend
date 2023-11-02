package com.TruMIS.assetservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.transaction.AssetMailConfig;

@Repository
public interface EmailRepository extends JpaRepository<AssetMailConfig, Long> {
	List<AssetMailConfig> findAll();

	AssetMailConfig findByMailId(String mailId);

	@Query("SELECT a FROM AssetMailConfig a WHERE " + "(a.location = 'All' OR a.location = :location) AND "
			+ "a.type = 'CC' AND " + "a.ownerService = 'Asset'")
	List<AssetMailConfig> findCCIdsByLocation(@Param("location") String location);

	@Query("SELECT a FROM AssetMailConfig a WHERE " + "(a.location= 'All' OR a.location = :location) AND " + "a.type = 'BCC' AND "
			+ "a.ownerService = 'Asset'")
	List<AssetMailConfig> findBCCIdsByLocation(@Param("location") String location);

	@Query("SELECT a FROM AssetMailConfig a WHERE " + "(a.location = :location) AND " + "a.ownerService = 'Asset'")
	List<AssetMailConfig> findCCandBCCIdsByLocation(@Param("location") String location);

}
