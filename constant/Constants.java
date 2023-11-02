package com.TruMIS.assetservice.constant;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.TruMIS.assetservice.entity.transaction.Asset;

@Component
public class Constants {
	public static final String DEFAULT_PAGE_NUMBER = "0";
	public static final String DEFAULT_PAGE_SIZE = "10";
	public static final String DEFAULT_SORT_BY_FIELD = "serviceTag";
	public static final String DEFAULT_SORT_DIRECTION = "asc";
	public static final String DEFAULT_SORT_KEY_METADATA = "name";
	public static final String TIME_ZONE = "Asia/Kolkata";
	public static final String GREETINGS_MSG = "Dear ";
	public static final String ASSIGN_MAIL_MSG = "<br><br>This is to inform you that following Truminds IT asset has been allocated to you.";
	public static final String RETURN_MAIL_MSG = "<br><br>This is to inform you that Truminds IT team has received the following asset that was allocated to you.";
	public static final String ASSET_ALLOC_DATE = "<br><br><span style=\"color:#172b4d; font-weight:500;\">Allocation Date</span>: ";
	public static final String ASSET_NAME = "<br><span style=\"color:#172b4d; font-weight:500;\">Asset Name:</span> ";
	private static final String ASSET_TYPE = "<br><span style=\"color:#172b4d; font-weight:500;\">Asset Type:</span> ";
	private static final String ASSET_LOCATION = "<br><span style=\"color:#172b4d; font-weight:500;\">Asset Location:</span> ";
	private static final String BASE_MSG = "<br><br>Best Regards,<br>TruMIS Team.";
	private static final String ASSET_RETURN_DATE = "<br><br><span style=\"color:#172b4d; font-weight:500;\">Return Date:</span> ";
	public static final String ALLOCATION_SUBJECT = "Asset Allocation To ";
	public static final String RETURN_SUBJECT = "Asset Returned By ";
	private static final String SERVICE_TAG = "<br><span style=\"color:#172b4d; font-weight:500;\">Serial No:</span> ";

	public static String mailBodyForAssetAssign(Asset assetInstancefromDB, Asset asset) {
		String mailBody = Constants.GREETINGS_MSG + asset.getIssuedTo().getFirstName() + " "
				+ asset.getIssuedTo().getLastName() + " , " + Constants.ASSIGN_MAIL_MSG + Constants.ASSET_ALLOC_DATE
				+ ((asset.getAllocationDate() == null) ? (LocalDate.now()) : (asset.getAllocationDate()))
				+ Constants.SERVICE_TAG
				+ ((asset.getServiceTag() == null) ? (assetInstancefromDB.getServiceTag()) : (asset.getServiceTag()))
				+ Constants.ASSET_TYPE + assetInstancefromDB.getAssetType().getName() + Constants.ASSET_NAME
				+ assetInstancefromDB.getAssetMaker().getName() + " " + assetInstancefromDB.getAssetModel()
				+ Constants.ASSET_LOCATION + assetInstancefromDB.getAssetLocation().getName() + Constants.BASE_MSG;

		return mailBody;
	}

	public static String mailBodyForAssetReturn(Asset assetInstancefromDB, Asset asset) {
		String mailBody = Constants.GREETINGS_MSG + assetInstancefromDB.getIssuedTo().getFirstName() + " "
				+ assetInstancefromDB.getIssuedTo().getLastName() + " , " + Constants.RETURN_MAIL_MSG
				+ Constants.ASSET_RETURN_DATE
				+ ((asset.getReturnDate() == null)
						? ((asset.getAllocationDate() == null) ? (LocalDate.now()) : (asset.getAllocationDate()))
						: (asset.getReturnDate()))
				+ Constants.SERVICE_TAG + assetInstancefromDB.getServiceTag() + Constants.ASSET_TYPE
				+ assetInstancefromDB.getAssetType().getName() + Constants.ASSET_NAME
				+ assetInstancefromDB.getAssetMaker().getName() + " " + assetInstancefromDB.getAssetModel()
				+ Constants.ASSET_LOCATION + assetInstancefromDB.getAssetLocation().getName() + Constants.BASE_MSG;

		return mailBody;
	}
}
