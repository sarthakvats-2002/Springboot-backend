package com.TruMIS.assetservice.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.TruMIS.assetservice.entity.transaction.Asset;
import com.TruMIS.assetservice.entity.transaction.AssetHistory;
import com.TruMIS.assetservice.payload.AssetCount;

public interface AssetService {

	Asset saveAsset(Asset asset);

	Page<Asset> fetchAssetWithSearchConstraints(int pageNo, int pageSize, String sortBy, String sortDir,
			String searchCriteria, Boolean isClient);

	Asset updateAsset(Long assetId, Asset asset);

	Page<AssetHistory> fetchAssetHistoryWithSearchConstraints(int pageNo, int pageSize, String sortBy, String sortDir,
			String searchCriteria, LocalDate startDate, LocalDate endDate);

	void setAssetIdFalse(Long assetId) throws Exception;

	Map<String, Object> getMetaDataForDropDown();

	void checkDuplicateConstraintOnServiceTag(String serviceTag);

	Asset getById(Long id);

	AssetCount getCountOfAssets();
}
