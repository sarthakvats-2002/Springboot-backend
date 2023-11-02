package com.TruMIS.assetservice.payload;

import lombok.Data;

@Data
public class AssetCount {
	private Long activeAssets;
	private Long ggnActiveAssets;
	private Long hydActiveAssets;
	private Long blrActiveAssets;
	private Long chnActiveAssets;
	private Long usaActiveAssets;
	private Long othersActiveAssets;
	private Long activeClientAssets;
	private Long assignedClientAssets;
	private Long inStockClientAssets;
}
