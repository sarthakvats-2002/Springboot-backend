package com.TruMIS.assetservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.metadeta.AssetMaker;

@Repository
public interface AssetMakerRepository extends JpaRepository<AssetMaker, Long> {

	AssetMaker findByCode(String code);

}
