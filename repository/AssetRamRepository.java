package com.TruMIS.assetservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.metadeta.AssetRam;

@Repository
public interface AssetRamRepository extends JpaRepository<AssetRam, Long> {

	AssetRam findByCode(String code);

}
