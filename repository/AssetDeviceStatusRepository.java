package com.TruMIS.assetservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TruMIS.assetservice.entity.metadeta.AssetDeviceStatus;

@Repository
public interface AssetDeviceStatusRepository extends JpaRepository<AssetDeviceStatus, Long> {

	AssetDeviceStatus findByCode(String code);
}
