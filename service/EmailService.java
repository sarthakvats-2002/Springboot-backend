package com.TruMIS.assetservice.service;

import java.util.List;

import com.TruMIS.assetservice.entity.transaction.AssetMailConfig;

public interface EmailService {

	AssetMailConfig saveMailObject(AssetMailConfig assetMailConfig) throws Exception;

	List<AssetMailConfig> findAllMailObjects();

	AssetMailConfig updateMailObject(Long objectid, AssetMailConfig assetMailConfig);

	void deleteById(Long objectId);

	List<AssetMailConfig> findCCMailWithGivenLocation(String location);

	public void sendEmailInBackground(String from, String to, String reportingManagerId, List<AssetMailConfig> cc, List<AssetMailConfig> bcc,
			String subject, String body);

	List<AssetMailConfig> findBCCMailWithGivenLocation(String name);

	List<AssetMailConfig> findCCandBCCMailWithGivenLocation(String location);
}
