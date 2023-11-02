package com.TruMIS.assetservice.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import com.TruMIS.assetservice.utils.ExcelHelper;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.TruMIS.assetservice.constant.Constants;
import com.TruMIS.assetservice.entity.transaction.Asset;
import com.TruMIS.assetservice.entity.transaction.AssetHistory;
import com.TruMIS.assetservice.payload.AssetCount;
import com.TruMIS.assetservice.repository.AssetRepository;
import com.TruMIS.assetservice.service.AssetService;
import com.TruMIS.assetservice.service.impl.AssetServiceImpl;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/v1/assets")
public class AssetController {
	//http://localhost:8081/api/v1/assets/asset/issued/excel
	private final AssetService assetService;
	private final ExcelHelper excelHelper;
	private static final Logger log = LoggerFactory.getLogger(AssetServiceImpl.class);

	public AssetController(AssetService assetService, ExcelHelper excelHelper, AssetRepository assetRepository) {
		this.assetService = assetService;
		this.excelHelper = excelHelper;
	}

	@PostMapping("/asset")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> saveAsset(@RequestBody Asset asset) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Inside the saveAsset controller and saving the asset object. Asset object is : " + asset);
			Asset savedAssets = assetService.saveAsset(asset);
			response.put("Success", savedAssets);
		} catch (Exception e) {
			log.info("Exception Occurred  " + e.getMessage());
			response.put("Exception", "Error while creating the asset: " + e.getMessage());
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/asset")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> getAllAsset(
			@RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int pageNo,
			@RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(defaultValue = Constants.DEFAULT_SORT_BY_FIELD) String sortBy,
			@RequestParam(defaultValue = Constants.DEFAULT_SORT_DIRECTION) String sortDir,
			@RequestParam(required = false) String searchCriteria, @RequestParam(required = false) Boolean isClient) {
		log.info("INSIDE /api/v1/assets/asset" + "pageNo" + pageNo + "&" + "pageSize" + pageSize + "&" + "sortBy"
				+ sortBy);
		Map<String, Object> response = new HashMap<>();
		try {
			Page<Asset> pagedResult = assetService.fetchAssetWithSearchConstraints(pageNo, pageSize, sortBy, sortDir,
					((searchCriteria != null) ? searchCriteria.toLowerCase() : null), isClient);
			log.info("Asset with search String as -- " + searchCriteria + " and whose isClient is :- " + isClient
					+ "are" + pagedResult);
			// log.info("Asset search content is "+ pagedResult.getContent());
			log.info("TotalPAge " + pagedResult.getTotalPages() + "TotalStuff" + pagedResult.getTotalElements());
			return getMapResponseEntity(pagedResult);
		} catch (Exception e) {
			response.put("Exception", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/asset/{id}")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> getAssetById(@PathVariable(name = "id") Long id) {
		log.info("Fetching asset from db with id: {}.", id);
		Map<String, Object> response = new HashMap<>();
		try {
			Asset a = assetService.getById(id);
			response.put("Success", a);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex) {
			response.put("Exception", ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/asset/assetCount")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> getCountOfAssets() {
		log.info("Inside the count Asset function ");
		HashMap<String, Object> res = new HashMap<>();
		try {
			AssetCount assetCount = assetService.getCountOfAssets();
			res.put("assetCount", assetCount);
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (Exception e) {
			res.put("Exception", e.getMessage());
			return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/asset/{id}")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> updateAsset(@PathVariable("id") Long assetId, @RequestBody Asset asset) {
		log.info("Inside Put Mapping : /api/v1/assets/asset/" + assetId);
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Inside the try-catch of PutMapping, updating the current asset with id " + assetId);
			Asset updatedAsset = assetService.updateAsset(assetId, asset);
			response.put("assetUpdatedObject", updatedAsset);
		} catch (Exception e) {
			response.put("Exception", e.getMessage());
			log.info("Exception while updating the asset resource" + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/asset/{id}")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> deleteAssetById(@PathVariable("id") Long assetId) {
		log.info("Inside Delete Mapping : /api/v1/assets/asset/" + assetId);
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Inside Try catch of delete mapping. Marking the current asset's IsActive with id" + assetId
					+ "as false");
			assetService.setAssetIdFalse(assetId);
			String msg = "Asset with AssetId as " + assetId + " Deleted Successfully";
			response.put("Success", msg);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.info("Exception with Deleting the asset resource " + e.getMessage());
			response.put("Exception", e.getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/get-asset-history")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> getAllAssetHistory(
			@RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int pageNo,
			@RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(defaultValue = Constants.DEFAULT_SORT_BY_FIELD) String sortBy,
			@RequestParam(defaultValue = Constants.DEFAULT_SORT_DIRECTION) String sortDir,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) String searchCriteria) {
		log.info("INSIDE /api/v1/assets/" + "pageNo" + pageNo + "&" + "pageSize" + pageSize + "&" + "sortBy" + sortBy);
		Map<String, Object> response = new HashMap<>();
		try {
			if (startDate != null && endDate != null) {
				// Check if startDate is smaller than endDate
				if (startDate.isAfter(endDate)) {
					throw new IllegalArgumentException("StartDate should always be smaller than endDate");
				}
			}
			// Searching and pagination on this search
			Page<AssetHistory> pagedResult = assetService.fetchAssetHistoryWithSearchConstraints(pageNo, pageSize,
					sortBy, sortDir, ((searchCriteria != null) ? searchCriteria.toLowerCase() : null), startDate,
					endDate);
			log.info("AssetHistory with search String as -- " + searchCriteria + " is :- " + pagedResult);
			log.info("TotalPAge " + pagedResult.getTotalPages() + "TotalStuff" + pagedResult.getTotalElements());
			return getMapHistoryResponseEntity(pagedResult);
		} catch (IllegalArgumentException e) {
			response.put("Exception", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.put("Exception", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/asset/dropdown")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> getMetaDataForDropDown() {
		log.info("Inside the dropdown API");
		// getting a map of String to array of Objects of metadata
		Map<String, Object> response = new HashMap<>();
		try {
			response = assetService.getMetaDataForDropDown();
		} catch (Exception e) {
			response.put("Exception", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private ResponseEntity<Map<String, Object>> getMapResponseEntity(Page<Asset> pagedResult) {
		Map<String, Object> responseWithSearchConstraint = new HashMap<>();
		responseWithSearchConstraint.put("Results", pagedResult.getContent());
		responseWithSearchConstraint.put("TotalAssets", pagedResult.getTotalElements());
		responseWithSearchConstraint.put("CurrenPage", pagedResult.getNumber());
		responseWithSearchConstraint.put("TotalPages", pagedResult.getTotalPages());
		return new ResponseEntity<>(responseWithSearchConstraint, HttpStatus.OK);
	}

	private ResponseEntity<Map<String, Object>> getMapHistoryResponseEntity(Page<AssetHistory> pagedResult) {
		Map<String, Object> responseWithSearchConstraint = new HashMap<>();
		responseWithSearchConstraint.put("Results", pagedResult.getContent());
		responseWithSearchConstraint.put("TotalAssets", pagedResult.getTotalElements());
		responseWithSearchConstraint.put("CurrenPage", pagedResult.getNumber());
		responseWithSearchConstraint.put("TotalPages", pagedResult.getTotalPages());
		return new ResponseEntity<>(responseWithSearchConstraint, HttpStatus.OK);
	}

	@PostMapping("/upload")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> uploadExcelOfAsset(@RequestParam("file") MultipartFile file) {
		Map<String, Object> res = new HashMap<>();
		try {
			res.put("success", true);
			excelHelper.uploadExcel(file);
			res.put("data", "Asset Excel Uploaded successfully");
		} catch (Exception ex) {
			res.put("success", false);
			res.put("message", "Error while saving the asset excel: " + ex.getMessage());
			log.info(ex.getMessage());
			return new ResponseEntity<Map<String, Object>>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<String, Object>>(res, HttpStatus.OK);
	}

	@GetMapping("/asset/excel")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String, Object>> getAllAssetForExcel(HttpServletResponse response,
			@RequestParam(required = false) Boolean isClient) {
		Map<String, Object> res = new HashMap<>();
		try {
			response.setContentType("application/octet-stream");
			Date date = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy_hhmmss");
			String fileName = "";
			if (Objects.isNull(isClient))
				fileName = "AllAssets_";
			else if (isClient)
				fileName = "ClientAssets_";
			else
				fileName = "TrumindsAssets_";
			response.setHeader("Content-Disposition",
					"attachment;filename=" + fileName + simpleDateFormat.format(date) + ".xlsx");
			excelHelper.downloadAssetForExcel(response, isClient);
			res.put("assetsdata", "downloaded");
			res.put("success", true);
		} catch (Exception ex) {
			res.put("success", false);
			res.put("message", "Error fetching the Asset Excel with error: " + ex.getMessage());
			return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
	@GetMapping("/issued/excel")
	@CrossOrigin(origins = "*")
	public ResponseEntity<Map<String,Object>> getAllIssuedAsset(HttpServletResponse response){
		Map<String,Object> res = new HashMap<>();
		try {
			response.setContentType("applicatiop/octet-stream");
			Date date = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy_hhmmss");
			response.setHeader("Content-Disposition",
					"attachment;filename=IssuedAsset_" + simpleDateFormat.format(date) + ".xlsx");
			excelHelper.downloadAllIssuedAsset(response);
			res.put("assetissueddata", "downloaded");
			res.put("success", true);
		} catch(Exception ex) {
			res.put("success",false);
			res.put("message", "Error fetching the Asset Excel with error: " + ex.getMessage());
			return new ResponseEntity<>(res,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(res,HttpStatus.OK);
	}

	@GetMapping("/location/{location}")
	public ResponseEntity<Map<String,Object>> getAllAssetByLocation(HttpServletResponse response, @PathVariable String location){
		Map<String,Object> res = new HashMap<>();
		try{
			response.setContentType("application/octet-stream");
			Date date = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy_hhmmss");
			String fileName = "";
			if (location.equals("Gurgaon"))
				fileName = "GurgaonAssets_";
			else if (location.equals("Hyderabad"))
				fileName = "HyderabadAssets_";
			else if(location.equals("Bangalore"))
				fileName = "BangaloreAssets_";
			else if(location.equals("Chennai"))
				fileName = "ChennaiAssets_";
			else if(location.equals("USA"))
				fileName = "USAAssets_";
			else if(location.equals("Others"))
				fileName = "OthersAssets_";
			response.setHeader("Content-Disposition","attachment;filename="+fileName + simpleDateFormat.format(date) + ".xlsx");
			excelHelper.downloadAssetByLocation(response,location);
		} catch (IOException ex) {
			res.put("success", false);
			res.put("message", "Error fetching the Asset Excel with error: " + ex.getMessage());
			return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
}
