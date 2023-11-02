package com.TruMIS.assetservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.metadeta.AssetType;

@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {

	AssetType findByCode(String code);

}
