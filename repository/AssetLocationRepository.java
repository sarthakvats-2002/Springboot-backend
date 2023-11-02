package com.TruMIS.assetservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.metadeta.AssetLocation;

@Repository
public interface AssetLocationRepository extends JpaRepository<AssetLocation, Long> {

	AssetLocation findByCode(String code);

}
