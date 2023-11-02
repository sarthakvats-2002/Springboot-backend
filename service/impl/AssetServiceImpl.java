package com.TruMIS.assetservice.service.impl;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.TruMIS.assetservice.constant.Constants;
import com.TruMIS.assetservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.TruMIS.assetservice.entity.Depend.Employee;
import com.TruMIS.assetservice.entity.metadeta.AssetDeviceStatus;
import com.TruMIS.assetservice.entity.metadeta.AssetLocation;
import com.TruMIS.assetservice.entity.metadeta.AssetMaker;
import com.TruMIS.assetservice.entity.metadeta.AssetProcessor;
import com.TruMIS.assetservice.entity.metadeta.AssetRam;
import com.TruMIS.assetservice.entity.metadeta.AssetType;
import com.TruMIS.assetservice.entity.transaction.Asset;
import com.TruMIS.assetservice.entity.transaction.AssetHistory;
import com.TruMIS.assetservice.exception.ResourceNotFoundException;
import com.TruMIS.assetservice.exception.ServiceTagAlreadyExistException;

import com.TruMIS.assetservice.payload.AssetCount;
import com.TruMIS.assetservice.repository.AssetDeviceStatusRepository;
import com.TruMIS.assetservice.repository.AssetHistoryRepository;
import com.TruMIS.assetservice.repository.AssetLocationRepository;
import com.TruMIS.assetservice.repository.AssetMakerRepository;
import com.TruMIS.assetservice.repository.AssetProcessorRepository;
import com.TruMIS.assetservice.repository.AssetRamRepository;
import com.TruMIS.assetservice.repository.AssetRepository;
import com.TruMIS.assetservice.repository.AssetTypeRepository;
import com.TruMIS.assetservice.repository.EmployeeRepository;
import com.TruMIS.assetservice.service.AssetService;
import com.TruMIS.assetservice.service.EmailService;

import jakarta.persistence.Tuple;

import java.util.ArrayList;

@Service
public class AssetServiceImpl implements AssetService {

	private final AssetRepository assetRepository;
	private final EmployeeRepository employeeRepository;
	private final AssetHistoryRepository assetHistoryRepository;
	private final AssetDeviceStatusRepository assetDeviceStatusRepository;
	private final AssetLocationRepository assetLocationRepository;
	private final AssetMakerRepository assetMakerRepository;
	private final AssetProcessorRepository assetProcessorRepository;
	private final AssetRamRepository assetRamRepository;
	private final AssetTypeRepository assetTypeRepository;
	private final EmailService emailService;

	private final AssetReportsMetadataRepository assetReportsMetadataRepository;

	public AssetServiceImpl(AssetRepository assetRepository, EmployeeRepository employeeRepository,
			AssetHistoryRepository assetHistoryRepository, AssetDeviceStatusRepository assetDeviceStatusRepository,
			AssetLocationRepository assetLocationRepository, AssetMakerRepository assetMakerRepository,
			AssetProcessorRepository assetProcessorRepository, AssetRamRepository assetRamRepository,
			AssetTypeRepository assetTypeRepository, EmailService emailService,
			AssetReportsMetadataRepository assetReportsMetadataRepository) {
		this.assetRepository = assetRepository;
		this.employeeRepository = employeeRepository;
		this.assetHistoryRepository = assetHistoryRepository;
		this.assetDeviceStatusRepository = assetDeviceStatusRepository;
		this.assetLocationRepository = assetLocationRepository;
		this.assetMakerRepository = assetMakerRepository;
		this.assetProcessorRepository = assetProcessorRepository;
		this.assetRamRepository = assetRamRepository;
		this.assetTypeRepository = assetTypeRepository;
		this.emailService = emailService;
		this.assetReportsMetadataRepository = assetReportsMetadataRepository;
	}

	private static final Logger log = LoggerFactory.getLogger(AssetServiceImpl.class);

	@Override
	public Asset saveAsset(Asset asset) {
		if (Objects.isNull(asset.getServiceTag()) || asset.getServiceTag().isEmpty()) {
			throw new NullPointerException("Asset Service Tag can't be null or empty");
		}
		checkDuplicateConstraintOnServiceTag(asset.getServiceTag());
		log.info("Saving the instance of Asset");
		return assetRepository.save(asset);
	}

	@Override
	public Page<Asset> fetchAssetWithSearchConstraints(int pageNo, int pageSize, String sortBy, String sortDir,
			String searchCriteria, Boolean isClient) {
		Sort sort = (sortDir.equalsIgnoreCase("asc")) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageableRequest = PageRequest.of(pageNo, pageSize, sort);
		return assetRepository.findAssetsWithSearchCriteria(searchCriteria, isClient, pageableRequest);
	}

	@Override
	public Asset updateAsset(Long assetId, Asset asset) {
		log.info("Fetching asset from db with id: {}.", assetId);
		Asset assetInstanceFromDB = assetRepository.findById(assetId)
				.orElseThrow(() -> new ResourceNotFoundException("Asset Not Found For Asset Id: " + assetId));

		// Checking whether the Asset is deleted
		if (assetInstanceFromDB.getIsActive() == false) {
			log.info("Asset with id " + assetId + "has been deleted.");
			throw new ResourceNotFoundException("Asset with AssetId `" + assetId + "` has been deleted.");
		}
		// Assigning
		if (Objects.nonNull(asset.getIssuedTo())) {
			log.info("Fetching employee whom this assest assigned to with id: {}.", asset.getIssuedTo().getId());
			Employee employeeAlloc = employeeRepository.findById(asset.getIssuedTo().getId())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Employee Not Found For Employee Id: " + asset.getIssuedTo().getId()));
			asset.setIssuedTo(employeeAlloc);
			// Assigning new asset to the employee
			log.info("Assigning new asset to the employee.");
			if (Objects.isNull(assetInstanceFromDB.getIssuedTo())) {
				assetInstanceFromDB.setIssuedTo(asset.getIssuedTo());
				String reportingManagerId = getReportingManagerMailId(employeeAlloc.getReportingManager());
				emailService.sendEmailInBackground("support.trumis@truminds.com", employeeAlloc.getMailId(),
						reportingManagerId,
						emailService.findCCMailWithGivenLocation(assetInstanceFromDB.getAssetLocation().getName()),
						emailService.findBCCMailWithGivenLocation(assetInstanceFromDB.getAssetLocation().getName()),
						Constants.ALLOCATION_SUBJECT + employeeAlloc.getFirstName() + " " + employeeAlloc.getLastName(),
						Constants.mailBodyForAssetAssign(assetInstanceFromDB, asset));
				assetInstanceFromDB.setReturnDate(null);
			}
//							} // Reassigning asset to new employee subsequently
//							else if (!assetInstanceFromDB.getIssuedTo().getId().equals(asset.getIssuedTo().getId())) {
//								log.info("Reassigning asset to new employee subsequently.");
//								emailService.sendMail("support.trumis@truminds.com", assetInstanceFromDB.getIssuedTo().getMailId(), emailService.findAllMailObjects(),
//										Constants.RETURN_SUBJECT + assetInstanceFromDB.getIssuedTo().getFirstName()+ " " + assetInstanceFromDB.getIssuedTo().getLastName(),
//										Constants.mailBodyForAssetReturn(assetInstanceFromDB, asset));
//								AssetHistoryWriter(asset, assetInstanceFromDB);
//								emailService.sendMail("support.trumis@truminds.com", employeeAlloc.getMailId(),emailService.findAllMailObjects(),
//										Constants.ALLOCATION_SUBJECT + employeeAlloc.getFirstName()+ " " + employeeAlloc.getLastName(),
//										Constants.mailBodyForAssetAssign(assetInstanceFromDB, asset));
//								assetInstanceFromDB.setReturnDate(null);
//							}
		}
		// Returning the asset into inventory
		else if (Objects.nonNull(assetInstanceFromDB.getIssuedTo())) {
			log.info("Returning the asset into inventory.");
			String reportingManagerId = getReportingManagerMailId(
					assetInstanceFromDB.getIssuedTo().getReportingManager());
			emailService.sendEmailInBackground("support.trumis@truminds.com",
					assetInstanceFromDB.getIssuedTo().getMailId(), reportingManagerId,
					emailService.findCCMailWithGivenLocation(assetInstanceFromDB.getAssetLocation().getName()),
					emailService.findBCCMailWithGivenLocation(assetInstanceFromDB.getAssetLocation().getName()),
					Constants.RETURN_SUBJECT + assetInstanceFromDB.getIssuedTo().getFirstName() + " "
							+ assetInstanceFromDB.getIssuedTo().getLastName(),
					Constants.mailBodyForAssetReturn(assetInstanceFromDB, asset));
			AssetHistoryWriter(asset, assetInstanceFromDB);
			assetInstanceFromDB.setIssuedTo(null);
		}
		// Check for isIssued
		if (Objects.nonNull(asset.getIsIssued()) && !assetInstanceFromDB.getIsIssued().equals(asset.getIsIssued())) {
			assetInstanceFromDB.setIsIssued(asset.getIsIssued());
		}
		// Check for Asset Type
		if (Objects.nonNull(asset.getAssetType()) && !"".equalsIgnoreCase(asset.getAssetType().getCode())) {
			AssetType assetType = assetTypeRepository.findByCode(asset.getAssetType().getCode());
			assetInstanceFromDB.setAssetType(assetType);
		}
		// Check for Asset Maker
		if (Objects.nonNull(asset.getAssetMaker()) && !"".equalsIgnoreCase(asset.getAssetMaker().getCode())) {
			AssetMaker assetMaker = assetMakerRepository.findByCode(asset.getAssetMaker().getCode());
			assetInstanceFromDB.setAssetMaker(assetMaker);
		}
		// Check for Asset Vendor
		if (Objects.nonNull(asset.getAssetVendor()) && !"".equalsIgnoreCase(asset.getAssetVendor())) {
			assetInstanceFromDB.setAssetVendor(asset.getAssetVendor());
		}
		// Check for Asset Model
		if (Objects.nonNull(asset.getAssetModel()) && !"".equalsIgnoreCase(asset.getAssetModel())) {
			assetInstanceFromDB.setAssetModel(asset.getAssetModel());
		}
		// Check for Asset Processor
		if (Objects.nonNull(asset.getAssetProcessor()) && !"".equalsIgnoreCase(asset.getAssetProcessor().getCode())) {
			AssetProcessor assetProcessor = assetProcessorRepository.findByCode(asset.getAssetProcessor().getCode());
			assetInstanceFromDB.setAssetProcessor(assetProcessor);
		}
		// Check for Asset RAM
		if (Objects.nonNull(asset.getAssetRAM()) && !"".equalsIgnoreCase(asset.getAssetRAM().getCode())) {
			AssetRam assetRam = assetRamRepository.findByCode(asset.getAssetRAM().getCode());
			assetInstanceFromDB.setAssetRAM(assetRam);
		}
		// Check for Asset Service Tag
		if (Objects.nonNull(asset.getServiceTag()) && !"".equalsIgnoreCase(asset.getServiceTag())) {
			if (Objects.nonNull(assetInstanceFromDB.getServiceTag())
					&& !assetInstanceFromDB.getServiceTag().equals(asset.getServiceTag()))
				checkDuplicateConstraintOnServiceTag(asset.getServiceTag());
			assetInstanceFromDB.setServiceTag(asset.getServiceTag());
		}
		// Check for Asset Invoice
		if (Objects.nonNull(asset.getInvoiceNo()) && !"".equalsIgnoreCase(asset.getInvoiceNo())) {
			assetInstanceFromDB.setInvoiceNo(asset.getInvoiceNo());
		}
		// Check for Asset Allocation Date
		if (Objects.nonNull(asset.getAllocationDate())) {
			assetInstanceFromDB.setAllocationDate(asset.getAllocationDate());
		}
		// Check for Asset Return Date
		if (Objects.nonNull(asset.getReturnDate())) {
			if (!assetInstanceFromDB.getIsIssued())
				assetInstanceFromDB.setReturnDate(asset.getReturnDate());
		}
		// Check for Asset Warranty Date
		if (Objects.nonNull(asset.getWarrantyDate())) {
			assetInstanceFromDB.setWarrantyDate(asset.getWarrantyDate());
		}
		// Check for Asset Purchase Date
		if (Objects.nonNull(asset.getPurchaseDate())) {
			assetInstanceFromDB.setPurchaseDate(asset.getPurchaseDate());
		}
		// Check for Asset Expiry Date
		if (Objects.nonNull(asset.getExpiryDate())) {
			assetInstanceFromDB.setExpiryDate(asset.getExpiryDate());
		}
		// Check for Asset Device Status
		if (Objects.nonNull(asset.getDeviceStatus()) && !"".equalsIgnoreCase(asset.getDeviceStatus().getCode())) {
			AssetDeviceStatus assetDeviceStatus = assetDeviceStatusRepository
					.findByCode(asset.getDeviceStatus().getCode());
			assetInstanceFromDB.setDeviceStatus(assetDeviceStatus);
		}
		// Check for Asset Location
		if (Objects.nonNull(asset.getAssetLocation()) && !"".equalsIgnoreCase(asset.getAssetLocation().getCode())) {
			AssetLocation assetLocation = assetLocationRepository.findByCode(asset.getAssetLocation().getCode());
			assetInstanceFromDB.setAssetLocation(assetLocation);
		}
		// Check for Asset Entity
		if (Objects.nonNull(asset.getAssetEntity()) && !"".equalsIgnoreCase(asset.getAssetEntity())) {
			assetInstanceFromDB.setAssetEntity(asset.getAssetEntity());
		}
		// Check for isClient
		if (Objects.nonNull(asset.getIsClient())) {
			assetInstanceFromDB.setIsClient(asset.getIsClient());
		}
		// Check for Client Name
		if (Objects.nonNull(asset.getClientName()) && !"".equalsIgnoreCase(asset.getClientName())) {
			assetInstanceFromDB.setClientName(asset.getClientName());
		}
		// Check for Comment
		if (Objects.nonNull(asset.getComment())) {
			assetInstanceFromDB.setComment(asset.getComment());
		}
		// Check for updateBy
		if (Objects.nonNull(asset.getUpdatedBy()) && !"".equalsIgnoreCase(asset.getUpdatedBy())) {
			assetInstanceFromDB.setUpdatedBy(asset.getUpdatedBy());
		}
		log.info("Saving asset into db.");
		return assetRepository.save(assetInstanceFromDB);
	}

	private String getReportingManagerMailId(String reportingManager) {
		// Split the reportingManager string by spaces
		String[] parts = reportingManager.split(" ");
		// Get the last part of the split string (i.e., the email within the brackets)
		String emailWithBrackets = parts[parts.length - 1];
		// Remove the brackets from the email
		String email = emailWithBrackets.substring(1, emailWithBrackets.length() - 1);
		// Concatenate the "@truminds.com"
		String result = email + "@truminds.com";
		return result;
	}

	private void AssetHistoryWriter(Asset asset, Asset assetInstanceFromDB) {
		AssetHistory assetHistory = new AssetHistory();
		Employee employeeIssuedTo = assetInstanceFromDB.getIssuedTo();
		assetHistory.setEmpName(employeeIssuedTo.getFirstName() + " " + employeeIssuedTo.getLastName());
		assetHistory.setEmpEmail(employeeIssuedTo.getMailId());
		assetHistory.setReportngManager(employeeIssuedTo.getReportingManager());
		assetHistory.setServiceTag(assetInstanceFromDB.getServiceTag());
		assetHistory.setFromDate(assetInstanceFromDB.getAllocationDate());
		if (Objects.nonNull(asset.getReturnDate())) {
			assetHistory.setToDate(asset.getReturnDate());
		} else {
			assetHistory.setToDate(LocalDate.now());
		}
		assetHistory.setUpdatedBy(asset.getUpdatedBy());
		assetHistoryRepository.save(assetHistory);
		assetInstanceFromDB.setIssuedTo(asset.getIssuedTo());
	}

	@Override
	public void checkDuplicateConstraintOnServiceTag(String ServiceTag) {
		if (assetRepository.existsByServiceTag(ServiceTag)) {
			log.info("Asset with Service tag  as {} already exists.", ServiceTag);
			throw new ServiceTagAlreadyExistException(
					"Asset with similar Service tag as `" + ServiceTag + "` already exists.");
		}
	}

	@Override
	public Page<AssetHistory> fetchAssetHistoryWithSearchConstraints(int pageNo, int pageSize, String sortBy,
			String sortDir, String searchCriteria, LocalDate startDate, LocalDate endDate) {
		Sort sort = (sortDir.equalsIgnoreCase("asc")) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageableRequest = PageRequest.of(pageNo, pageSize, sort);
		return assetHistoryRepository.findAssetHistoryWithSearchCriteria(searchCriteria, startDate, endDate,
				pageableRequest);
	}

	@Override
	public void setAssetIdFalse(Long assetId) throws Exception {
		Asset assetInstanceviaId = assetRepository.findById(assetId)
				.orElseThrow(() -> new ResourceNotFoundException("Asset Not Found with AssetId: " + assetId));
		Boolean isActive = assetInstanceviaId.getIsActive();
		if (isActive != null && !isActive) {
			log.info("Asset with AssetId as " + assetId + " is already flagged as false");
			throw new Exception("Employee with assetId as " + assetId + " is already flagged as false");
		} else if (isActive == null || isActive) {
			assetInstanceviaId.setIsActive(false);
			assetRepository.save(assetInstanceviaId);
		}
	}

	@Override
	public Map<String, Object> getMetaDataForDropDown() {
		Map<String, Object> allDropDownData = new HashMap<>();

		allDropDownData.put("devicestatus", sortByName(assetDeviceStatusRepository.findAll()));
		allDropDownData.put("location", sortByName(assetLocationRepository.findAll()));
		allDropDownData.put("maker", sortByName(assetMakerRepository.findAll()));
		allDropDownData.put("processor", sortByName(assetProcessorRepository.findAll()));
		allDropDownData.put("ram", sortByName(assetRamRepository.findAll()));
		allDropDownData.put("type", sortByName(assetTypeRepository.findAll()));
		allDropDownData.put("assetreport", sortByName(assetReportsMetadataRepository.findAll()));

		log.info("The required sorted dropdown menu is as follows: ");
		return allDropDownData;
	}

	// Converting into list in order to sort the list objects on the basis of their
	// name fields
	private List<Object> sortByName(List<?> objects) {
		List<Object> sortedList = new ArrayList<>(objects);
		sortedList.sort(new Comparator<Object>() {
			@Override
			public int compare(Object obj1, Object obj2) {
				String name1 = getNameFromObject(obj1);
				String name2 = getNameFromObject(obj2);

				return name1.compareToIgnoreCase(name2);
			}

			// Using reflection to access the "name" field of the objects dynamically
			// .It attempts to retrieve the "name" field using
			// obj.getClass().getDeclaredField("name")
			// and then gets the field value using nameField.get(obj)
			private String getNameFromObject(Object obj) {
				try {
					Field nameField = obj.getClass().getDeclaredField(Constants.DEFAULT_SORT_KEY_METADATA);
					nameField.setAccessible(true);
					return (String) nameField.get(obj);
				} catch (Exception e) {
					e.printStackTrace();
					return ""; // Return an empty string if field access fails
				}
			}
		});
		return sortedList;
	}

	@Override
	public Asset getById(Long id) {
		return assetRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Asset Not Found with AssetId: " + id));
	}

	@Override
	public AssetCount getCountOfAssets() {

		log.info("Inside getCountAsset Function");

		AssetCount assetCount = new AssetCount();
		Long count = assetRepository.findAllActiveAssets();
//		assetCount.setActiveAssets(count);
//		count = assetRepository.findAllActiveAssetsByLocationWise("Gurgaon");
//		assetCount.setGgnActiveAssets(count);
//		count = assetRepository.findAllActiveAssetsByLocationWise("Hyderabad");
//		assetCount.setHydActiveAssets(count);
//		count = assetRepository.findAllActiveAssetsByLocationWise("Bangalore");
//		assetCount.setBlrActiveAssets(count);
//		count = assetRepository.findAllActiveAssetsByLocationWise("Chennai");
//		assetCount.setChnActiveAssets(count);
//		count = assetRepository.findAllActiveAssetsByLocationWise("USA");
//		assetCount.setUsaActiveAssets(count);
		List<Tuple> countTuple = assetRepository.findAllActiveAssetsByLocationWise();
		for (Tuple t : countTuple) {
			System.out.println(t.get(0) + " " + t.get(1));
		}
		List<String> locations = new ArrayList<>();
		locations.add("Gurgaon");
		locations.add("Hyderabad");
		locations.add("Bangalore");
		locations.add("Chennai");
		locations.add("USA");
		Long countOther = assetRepository.findAllOtherActiveAssetsByLocationWise(locations);
		assetCount.setOthersActiveAssets(countOther);
//		count = assetRepository.findAllAssignedOrNotAssigned(null);
//		assetCount.setActiveClientAssets(count);
//		count = assetRepository.findAllAssignedOrNotAssigned(true);
//		assetCount.setAssignedClientAssets(count);
//		count = assetRepository.findAllAssignedOrNotAssigned(false);
//		assetCount.setInStockClientAssets(count);
		return assetCount;
	}
}
