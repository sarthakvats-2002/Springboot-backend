package com.TruMIS.assetservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.metadeta.AssetProcessor;

@Repository
public interface AssetProcessorRepository extends JpaRepository<AssetProcessor, Long> {

	AssetProcessor findByCode(String code);

}
