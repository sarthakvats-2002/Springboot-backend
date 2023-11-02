package com.TruMIS.assetservice.utils;
import com.TruMIS.assetservice.constant.Constants;
import com.TruMIS.assetservice.entity.Depend.Employee;
import com.TruMIS.assetservice.entity.metadeta.*;
import com.TruMIS.assetservice.entity.transaction.Asset;
import com.TruMIS.assetservice.exception.ExcelException;
import com.TruMIS.assetservice.exception.ResourceNotFoundException;
import com.TruMIS.assetservice.repository.AssetRepository;
import com.TruMIS.assetservice.repository.EmployeeRepository;
import com.TruMIS.assetservice.service.AssetService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelHelper.class);

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AssetService assetService;

    public Boolean checkFileType(MultipartFile file)
    {
        LOGGER.info("Checking file type.");
        String contentType=file.getContentType();
        return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    public Boolean checkFileFormat(InputStream is) throws IOException, InvalidFormatException
    {
        try (XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            XSSFSheet sheet = workbook.getSheet("Data");
            Iterator<Row> iterator = sheet.iterator();
            String[] headers=new String[] {"AssetLocation","DateOfPurchase","AssetEntity","Invoice","AssetType",
                    "AssetMaker","AssetModel","AssetVendor","Processor","RAM","ServiceTag","IssuedTo",
                    "WarrantyDate","AllocationDate","ExpiryDate","DeviceStatus",
                    "ClientName","Comment"};
            LOGGER.info("Checking headers of excel sheet.");
            if(iterator.hasNext())
            {
                Row row=iterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                int cellCount=0;
                while(cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                    String stringCellValue = cell.getStringCellValue();
                    if(!headers[cellCount].equals(stringCellValue))
                        return false;
                    cellCount++;
                }
            }
        }
        return true;
    }
    public void uploadExcel(MultipartFile file) throws InvalidFormatException, IOException {
		if (!checkFileType(file)) {
			throw new ExcelException("Excel file type is not correct. Please upload excel of type 'xlsx'.");
		}
		if (!checkFileFormat(file.getInputStream())) {
			throw new ExcelException("Excel file format is not correct. Please mention the correct headers' name.");
		}
		XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
		XSSFSheet sheet = workbook.getSheet("Data");
		Iterator<Row> iterator = sheet.iterator();
		if (iterator.hasNext()) {
			Row row = iterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			StringBuilder headerSB = new StringBuilder();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				String stringCellValue = cell.getStringCellValue();
				headerSB.append(stringCellValue + ",");
			}
			LOGGER.info("Headers : {}", headerSB.toString());
		}
		List<Asset> assetList = new ArrayList<>();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			Asset asset = new Asset();
			AssetDeviceStatus assetDeviceStatus = new AssetDeviceStatus();
			AssetLocation assetLocation = new AssetLocation();
			AssetMaker assetMaker = new AssetMaker();
			AssetProcessor assetProcessor = new AssetProcessor();
			AssetRam assetRam = new AssetRam();
			AssetType assetType = new AssetType();
			ZoneId zoneId = ZoneId.of(Constants.TIME_ZONE);
			if (cellIterator.hasNext()) {
				assetLocation.setCode(cellIterator.next().getStringCellValue().trim());
				asset.setAssetLocation(assetLocation);
			}
			if (cellIterator.hasNext()) {
				Date dateCellValue = cellIterator.next().getDateCellValue();
				LocalDate purchaseDate = dateCellValue.toInstant().atZone(zoneId).toLocalDate();
				if(!purchaseDate.equals(LocalDate.of(2050, 1, 1))) {
					asset.setPurchaseDate(purchaseDate);
				} else {
					asset.setPurchaseDate(null);
				}
			}
			if (cellIterator.hasNext()) {
				asset.setAssetEntity(cellIterator.next().getStringCellValue().trim());
			}
			if (cellIterator.hasNext()) {
				asset.setInvoiceNo(cellIterator.next().getStringCellValue());
			}
			if (cellIterator.hasNext()) {
				assetType.setCode(cellIterator.next().getStringCellValue().trim());
				asset.setAssetType(assetType);
			}
			if (cellIterator.hasNext()) {
				assetMaker.setCode(cellIterator.next().getStringCellValue().trim());
				asset.setAssetMaker(assetMaker);
			}
			if (cellIterator.hasNext()) {
				asset.setAssetModel(cellIterator.next().getStringCellValue().trim());
			}
			if (cellIterator.hasNext()) {
				String assetVendor = cellIterator.next().getStringCellValue().trim();
				if (!assetVendor.equalsIgnoreCase("NA")) {
					asset.setAssetVendor(assetVendor);
				} else {
					asset.setAssetVendor(null);
				}
			}
			if (cellIterator.hasNext()) {
				assetProcessor.setCode(cellIterator.next().getStringCellValue().trim());
				asset.setAssetProcessor(assetProcessor);
			}
			if (cellIterator.hasNext()) {
				assetRam.setCode(cellIterator.next().getStringCellValue().trim());
				asset.setAssetRAM(assetRam);
			}
			if (cellIterator.hasNext()) {
				String serviceTag = cellIterator.next().getStringCellValue().trim();
				assetService.checkDuplicateConstraintOnServiceTag(serviceTag);
				asset.setServiceTag(serviceTag);
			}
			if (cellIterator.hasNext()) {
				String email = cellIterator.next().getStringCellValue().trim();
				if (!email.equalsIgnoreCase("In-stock")) {
					Employee employeeReference = employeeRepository.findByMailIdIgnoreCase(email);
					if (Objects.isNull(employeeReference)){
						throw new ResourceNotFoundException("Employee associated with this '"+email+"' is not found");
					}
					asset.setIssuedTo(employeeReference);
					asset.setIsIssued(true);
				} else {
					asset.setIssuedTo(null); // Set issuedTo property to null when email is blank
					asset.setIsIssued(false);
				}
			}
			if (cellIterator.hasNext()) {
				Date dateCellValue = cellIterator.next().getDateCellValue();
				LocalDate warrantyDate = dateCellValue.toInstant().atZone(zoneId).toLocalDate();
				if(!warrantyDate.equals(LocalDate.of(2050, 1, 1))) {
					asset.setWarrantyDate(warrantyDate);
				} else {
					asset.setWarrantyDate(null);
				}
			}
			if (cellIterator.hasNext()) {
				Date dateCellValue = cellIterator.next().getDateCellValue();
				LocalDate allocationDate = dateCellValue.toInstant().atZone(zoneId).toLocalDate();
				if(!allocationDate.equals(LocalDate.of(2050, 1, 1))) {
					asset.setAllocationDate(allocationDate);
				} else {
					asset.setAllocationDate(null);
				}
			}
			if (cellIterator.hasNext()) {
				Date dateCellValue = cellIterator.next().getDateCellValue();
				LocalDate expiryDate = dateCellValue.toInstant().atZone(zoneId).toLocalDate();
				if(!expiryDate.equals(LocalDate.of(2050, 1, 1))) {
					asset.setExpiryDate(expiryDate);
				} else {
					asset.setExpiryDate(null);
				}
			}
			if (cellIterator.hasNext()) {
				assetDeviceStatus.setCode(cellIterator.next().getStringCellValue().trim());
				asset.setDeviceStatus(assetDeviceStatus);
			}
			if (cellIterator.hasNext()) {
				String clientName = cellIterator.next().getStringCellValue().trim();
				if (!clientName.equalsIgnoreCase("NA")) {
					asset.setClientName(clientName);
					asset.setIsClient(true);
				} else {
					asset.setClientName(null);
					asset.setIsClient(false);
				}
			}
			if (cellIterator.hasNext()) {
				String comment = cellIterator.next().getStringCellValue().trim();
				if(!comment.equalsIgnoreCase("NA"))
					asset.setComment(comment);
			}
			asset.setIsActive(true);
			assetList.add(asset);
		}
		assetRepository.saveAll(assetList);
	}

	public void downloadAssetForExcel(HttpServletResponse response, Boolean isClient) throws IOException {
		if(Objects.isNull(isClient))
			downloadAllAssetForExcel(response);
		else if(isClient)
			downloadClientAssetForExcel(response, isClient);
		else
			downloadTrumindsAssetForExcel(response, isClient);
	}
	private void downloadTrumindsAssetForExcel(HttpServletResponse response, Boolean isClient) throws IOException {
		List<Asset> assets = assetRepository.findByIsActiveAndIsClient(true, isClient);
		List<String> columnHeadings = new ArrayList<>();
		columnHeadings.add("AssetLocation");//0
		columnHeadings.add("DateOfPurchase");//1
		columnHeadings.add("AssetEntity");//2
		columnHeadings.add("Invoice");//3
		columnHeadings.add("AssetType");//4
		columnHeadings.add("AssetMaker");//5
		columnHeadings.add("AssetModel");//6
		columnHeadings.add("AssetVendor");//7
		columnHeadings.add("Processor");//8
		columnHeadings.add("RAM");//9
		columnHeadings.add("ServiceTag");//10
		columnHeadings.add("IssuedTo");//11
		columnHeadings.add("WarrantyDate");//12
		columnHeadings.add("AllocationDate");//13
		columnHeadings.add("ExpiryDate");//14
		columnHeadings.add("ReturnedDate");//15
		columnHeadings.add("DeviceStatus");//16
		columnHeadings.add("Comment");//18

		Workbook workbook=new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Truminds Assets");

		Row row = sheet.createRow(0);
		CellStyle headerStyle = workbook.createCellStyle();
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeight(10);
		headerStyle.setFont(headerFont);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		for (int i = 0; i < columnHeadings.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(columnHeadings.get(i));
			cell.setCellStyle(headerStyle);
			sheet.autoSizeColumn(i);
		}

		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setFont(font);

		Font emailFont = workbook.createFont();
		emailFont.setFontHeightInPoints((short) 10);
		emailFont.setColor(IndexedColors.BLUE.getIndex());

		CellStyle emailStyle = workbook.createCellStyle();
		emailStyle.cloneStyleFrom(style);
		emailStyle.setFont(emailFont);

		Font greenFont = workbook.createFont();
		greenFont.setFontHeightInPoints((short) 10);
		greenFont.setColor(IndexedColors.DARK_GREEN.getIndex());

		CellStyle inStockStyle = workbook.createCellStyle();
		inStockStyle.cloneStyleFrom(style);
		inStockStyle.setFont(greenFont);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		int rowIndex =1;
		for(Asset asset:assets){
			Row dataRow = sheet.createRow(rowIndex);
			Cell locationCell = dataRow.createCell(0);
			locationCell.setCellValue(asset.getAssetLocation().getName());
			locationCell.setCellStyle(style);
			Cell purchaseDateCell = dataRow.createCell(1);
			purchaseDateCell.setCellStyle(style);
			LocalDate purchaseDate = asset.getPurchaseDate();
			if (Objects.nonNull(purchaseDate)) {
				purchaseDateCell.setCellValue(dateTimeFormatter.format(purchaseDate));
			} else {
				purchaseDateCell.setCellValue("");
			}
			sheet.autoSizeColumn(1);
			Cell entityCell = dataRow.createCell(2);
			entityCell.setCellValue(asset.getAssetEntity());
			entityCell.setCellStyle(style);
			sheet.autoSizeColumn(2);
			Cell invoiceCell = dataRow.createCell(3);
			invoiceCell.setCellValue(asset.getInvoiceNo());
			invoiceCell.setCellStyle(style);
			sheet.autoSizeColumn(3);
			Cell assetTypeCell = dataRow.createCell(4);
			assetTypeCell.setCellValue(asset.getAssetType().getName());
			assetTypeCell.setCellStyle(style);
			sheet.autoSizeColumn(4);
			Cell assetMakerCell = dataRow.createCell(5);
			assetMakerCell.setCellValue(asset.getAssetMaker().getName());
			assetMakerCell.setCellStyle(style);
			sheet.autoSizeColumn(5);
			Cell assetModelCell = dataRow.createCell(6);
			assetModelCell.setCellValue(asset.getAssetModel());
			assetModelCell.setCellStyle(style);
			sheet.autoSizeColumn(6);
			Cell assetVendorCell = dataRow.createCell(7);
			assetVendorCell.setCellValue(asset.getAssetVendor());
			assetVendorCell.setCellStyle(style);
			sheet.autoSizeColumn(7);
			Cell assetProcessorCell = dataRow.createCell(8);
			assetProcessorCell.setCellValue(asset.getAssetProcessor().getName());
			assetProcessorCell.setCellStyle(style);
			sheet.autoSizeColumn(8);
			Cell assetRAMCell = dataRow.createCell(9);
			assetRAMCell.setCellValue(asset.getAssetRAM().getName());
			assetRAMCell.setCellStyle(style);
			sheet.autoSizeColumn(9);
			Cell serviceTagCell = dataRow.createCell(10);
			serviceTagCell.setCellValue(asset.getServiceTag());
			serviceTagCell.setCellStyle(style);
			sheet.autoSizeColumn(10);

			Cell emailCell = dataRow.createCell(11);
			String mailId = asset.getIssuedTo() != null ? asset.getIssuedTo().getMailId() : null;
			if (Objects.nonNull(mailId) && !mailId.isEmpty()) {
				emailCell.setCellValue(mailId);
				emailCell.setCellStyle(emailStyle);
			} else {
				emailCell.setCellValue("In Stock");
				emailCell.setCellStyle(inStockStyle);
			}
			sheet.autoSizeColumn(11);
			Cell warrantyDateCell = dataRow.createCell(12);
			LocalDate warrantyDate = asset.getWarrantyDate();
			if (Objects.nonNull(warrantyDate)) {
				warrantyDateCell.setCellValue(dateTimeFormatter.format(warrantyDate));
			} else {
				warrantyDateCell.setCellValue("");
			}
			warrantyDateCell.setCellStyle(style);
			sheet.autoSizeColumn(12);
			Cell allocationDateCell = dataRow.createCell(13);
			allocationDateCell.setCellStyle(style);
			LocalDate allocationDate = asset.getAllocationDate();
			if (Objects.nonNull(allocationDate)) {
				allocationDateCell.setCellValue(dateTimeFormatter.format(allocationDate));
			} else {
				allocationDateCell.setCellValue("");
			}
			sheet.autoSizeColumn(13);
			Cell expiryDateCell = dataRow.createCell(14);
			LocalDate expiryDate = asset.getExpiryDate();
			expiryDateCell.setCellStyle(style);
			if (Objects.nonNull(expiryDate)) {
				expiryDateCell.setCellValue(dateTimeFormatter.format(expiryDate));
			} else {
				expiryDateCell.setCellValue("");
			}
			sheet.autoSizeColumn(14);
			Cell returnedDateCell = dataRow.createCell(15);
			returnedDateCell.setCellStyle(style);
			LocalDate returnDate = asset.getReturnDate();
			if (Objects.nonNull(returnDate)) {
				returnedDateCell.setCellValue(dateTimeFormatter.format(returnDate));
			} else {
				returnedDateCell.setCellValue("");
			}
			sheet.autoSizeColumn(15);
			Cell deviceStatusCell = dataRow.createCell(16);
			deviceStatusCell.setCellValue(asset.getDeviceStatus().getName());
			deviceStatusCell.setCellStyle(style);
			sheet.autoSizeColumn(16);
			Cell assetCommentCell = dataRow.createCell(17);
			assetCommentCell.setCellValue(asset.getComment());
			assetCommentCell.setCellStyle(style);
			sheet.autoSizeColumn(17);
			rowIndex++;
		}
		ServletOutputStream ops = response.getOutputStream();
		workbook.write(ops);
		workbook.close();
		ops.close();
	}

	private void downloadClientAssetForExcel(HttpServletResponse response, Boolean isClient) throws IOException {
		List<Asset> clientassets = assetRepository.findByIsActiveAndIsClient(true, isClient);
		List<String> columnHeadings = new ArrayList<>();
		columnHeadings.add("AssetLocation");//0
		columnHeadings.add("ClientName");//1
		columnHeadings.add("AssetType");//2
		columnHeadings.add("AssetMaker");//3
		columnHeadings.add("AssetModel");//4
		columnHeadings.add("Processor");//5
		columnHeadings.add("RAM");//6
		columnHeadings.add("ServiceTag");//7
		columnHeadings.add("IssuedTo");//8
		columnHeadings.add("AllocationDate");//9
		columnHeadings.add("ReturnedDate");//10
		columnHeadings.add("DeviceStatus");//11
		columnHeadings.add("Comment");//12

		Workbook workbook=new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Client Assets");

		Row row = sheet.createRow(0);
		CellStyle headerStyle = workbook.createCellStyle();
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeight(10);
		headerStyle.setFont(headerFont);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		for (int i = 0; i < columnHeadings.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(columnHeadings.get(i));
			cell.setCellStyle(headerStyle);
			sheet.autoSizeColumn(i);
		}
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setFont(font);

		Font emailFont = workbook.createFont();
		emailFont.setFontHeightInPoints((short) 10);
		emailFont.setColor(IndexedColors.BLUE.getIndex());

		CellStyle emailStyle = workbook.createCellStyle();
		emailStyle.cloneStyleFrom(style);
		emailStyle.setFont(emailFont);

		Font greenFont = workbook.createFont();
		greenFont.setFontHeightInPoints((short) 10);
		greenFont.setColor(IndexedColors.DARK_GREEN.getIndex());

		CellStyle inStockStyle = workbook.createCellStyle();
		inStockStyle.cloneStyleFrom(style);
		inStockStyle.setFont(greenFont);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		int rowIndex =1;
		for(Asset asset: clientassets){
			Row dataRow = sheet.createRow(rowIndex);
			Cell locationCell = dataRow.createCell(0);
			locationCell.setCellValue(asset.getAssetLocation().getName());
			locationCell.setCellStyle(style);
			Cell clientNameCell = dataRow.createCell(1);
			clientNameCell.setCellValue(asset.getClientName());
			clientNameCell.setCellStyle(style);
			Cell typeCell = dataRow.createCell(2);
			typeCell.setCellValue(asset.getAssetType().getName());
			typeCell.setCellStyle(style);
			Cell makerCell = dataRow.createCell(3);
			makerCell.setCellValue(asset.getAssetMaker().getName());
			makerCell.setCellStyle(style);
			Cell modelCell = dataRow.createCell(4);
			modelCell.setCellValue(asset.getAssetModel());
			modelCell.setCellStyle(style);
			sheet.autoSizeColumn(4);
			Cell processorCell = dataRow.createCell(5);
			processorCell.setCellValue(asset.getAssetProcessor().getName());
			processorCell.setCellStyle(style);
			Cell RAMCell = dataRow.createCell(6);
			RAMCell.setCellValue(asset.getAssetRAM().getName());
			RAMCell.setCellStyle(style);
			Cell ServiceTagCell = dataRow.createCell(7);
			ServiceTagCell.setCellValue(asset.getServiceTag());
			ServiceTagCell.setCellStyle(style);
			Cell emailCell = dataRow.createCell(8);
			String mailId = asset.getIssuedTo() != null ? asset.getIssuedTo().getMailId() : null;
			if (Objects.nonNull(mailId) && !mailId.isEmpty()) {
				emailCell.setCellValue(mailId);
				emailCell.setCellStyle(emailStyle);
			} else {
				emailCell.setCellValue("In Stock");
				emailCell.setCellStyle(inStockStyle);
			}
			sheet.autoSizeColumn(8);
			Cell allocationDateCell = dataRow.createCell(9);
			LocalDate allocationDate = asset.getAllocationDate();
			if (Objects.nonNull(allocationDate)) {
				allocationDateCell.setCellValue(dateTimeFormatter.format(allocationDate));
			} else {
				allocationDateCell.setCellValue("");
			}
			allocationDateCell.setCellStyle(style);
			Cell returnedDateCell = dataRow.createCell(10);
			LocalDate returnDate = asset.getReturnDate();
			if (Objects.nonNull(returnDate)) {
				returnedDateCell.setCellValue(dateTimeFormatter.format(returnDate));
			} else {
				returnedDateCell.setCellValue("");
			}
			returnedDateCell.setCellStyle(style);
			Cell deviceStatusCell = dataRow.createCell(11);
			deviceStatusCell.setCellValue(asset.getDeviceStatus().getName());
			deviceStatusCell.setCellStyle(style);
			Cell commentCell = dataRow.createCell(12);
			commentCell.setCellValue(asset.getComment());
			commentCell.setCellStyle(style);
			sheet.autoSizeColumn(12);
			rowIndex++;
		}
		ServletOutputStream ops = response.getOutputStream();
		workbook.write(ops);
		workbook.close();
		ops.close();
	}

	private void downloadAllAssetForExcel(HttpServletResponse response) throws IOException {
		List<Asset> allAssets = assetRepository.findByIsActive(true);
		String sheetName = "AllAssets";
		prepareExcel(response, allAssets,sheetName);
	}

	public void downloadAllIssuedAsset(HttpServletResponse response) throws IOException {
		List<Asset> issuedAssets = assetRepository.findByIsActiveAndIsIssued(true, true);
		String sheetName = "IssuedAssets";
		prepareExcel(response, issuedAssets,sheetName);
	}

	public void downloadAssetByLocation(HttpServletResponse response, String location) throws IOException {
		List<Asset> assetLocation = assetRepository.findByIsActiveAndAssetLocationName(true, location);
		String sheetName = location+"Assets";
		prepareExcel(response,assetLocation,sheetName);
	}

	private void prepareExcel(HttpServletResponse response, List<Asset> assets,String SheetName) throws IOException {
		List<String> columnHeadings = new ArrayList<>();
		columnHeadings.add("AssetLocation");//0
		columnHeadings.add("DateOfPurchase");//1
		columnHeadings.add("AssetEntity");//2
		columnHeadings.add("Invoice");//3
		columnHeadings.add("AssetType");//4
		columnHeadings.add("AssetMaker");//5
		columnHeadings.add("AssetModel");//6
		columnHeadings.add("AssetVendor");//7
		columnHeadings.add("Processor");//8
		columnHeadings.add("RAM");//9
		columnHeadings.add("ServiceTag");//10
		columnHeadings.add("IssuedTo");//11
		columnHeadings.add("WarrantyDate");//12
		columnHeadings.add("AllocationDate");//13
		columnHeadings.add("ExpiryDate");//14
		columnHeadings.add("DeviceStatus");//15
		columnHeadings.add("ClientName");//16
		columnHeadings.add("Comment");//17

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(SheetName);

		Row row = sheet.createRow(0);
		CellStyle headerStyle = workbook.createCellStyle();
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeight(10);
		headerStyle.setFont(headerFont);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		for (int i = 0; i < columnHeadings.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(columnHeadings.get(i));
			cell.setCellStyle(headerStyle);
			sheet.autoSizeColumn(i);
		}
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setFont(font);

		Font emailFont = workbook.createFont();
		emailFont.setFontHeightInPoints((short) 10);
		emailFont.setColor(IndexedColors.BLUE.getIndex());

		CellStyle emailStyle = workbook.createCellStyle();
		emailStyle.cloneStyleFrom(style);
		emailStyle.setFont(emailFont);

		Font greenFont = workbook.createFont();
		greenFont.setFontHeightInPoints((short) 10);
		greenFont.setColor(IndexedColors.DARK_GREEN.getIndex());

		CellStyle inStockStyle = workbook.createCellStyle();
		inStockStyle.cloneStyleFrom(style);
		inStockStyle.setFont(greenFont);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		int rowIndex = 1;
		for(Asset asset: assets){
			Row dataRow = sheet.createRow(rowIndex);
			Cell locationCell = dataRow.createCell(0);
			locationCell.setCellValue(asset.getAssetLocation().getName());
			locationCell.setCellStyle(style);
			Cell purchaseDateCell = dataRow.createCell(1);
			purchaseDateCell.setCellStyle(style);
			LocalDate purchaseDate = asset.getPurchaseDate();
			if (Objects.nonNull(purchaseDate)) {
				purchaseDateCell.setCellValue(dateTimeFormatter.format(purchaseDate));
			} else {
				purchaseDateCell.setCellValue("");
			}
			sheet.autoSizeColumn(1);
			Cell entityCell = dataRow.createCell(2);
			entityCell.setCellValue(asset.getAssetEntity());
			entityCell.setCellStyle(style);
			sheet.autoSizeColumn(2);
			Cell invoiceCell = dataRow.createCell(3);
			invoiceCell.setCellValue(asset.getInvoiceNo());
			invoiceCell.setCellStyle(style);
			sheet.autoSizeColumn(3);
			Cell assetTypeCell = dataRow.createCell(4);
			assetTypeCell.setCellValue(asset.getAssetType().getName());
			assetTypeCell.setCellStyle(style);
			sheet.autoSizeColumn(4);
			Cell assetMakerCell = dataRow.createCell(5);
			assetMakerCell.setCellValue(asset.getAssetMaker().getName());
			assetMakerCell.setCellStyle(style);
			sheet.autoSizeColumn(5);
			Cell assetModelCell = dataRow.createCell(6);
			assetModelCell.setCellValue(asset.getAssetModel());
			assetModelCell.setCellStyle(style);
			sheet.autoSizeColumn(6);
			Cell assetVendorCell = dataRow.createCell(7);
			assetVendorCell.setCellValue(asset.getAssetVendor());
			assetVendorCell.setCellStyle(style);
			sheet.autoSizeColumn(7);
			Cell assetProcessorCell = dataRow.createCell(8);
			assetProcessorCell.setCellValue(asset.getAssetProcessor().getName());
			assetProcessorCell.setCellStyle(style);
			sheet.autoSizeColumn(8);
			Cell assetRAMCell = dataRow.createCell(9);
			assetRAMCell.setCellValue(asset.getAssetRAM().getName());
			assetRAMCell.setCellStyle(style);
			sheet.autoSizeColumn(9);
			Cell serviceTagCell = dataRow.createCell(10);
			serviceTagCell.setCellValue(asset.getServiceTag());
			serviceTagCell.setCellStyle(style);
			sheet.autoSizeColumn(10);
			Cell emailCell = dataRow.createCell(11);
			String mailId = asset.getIssuedTo() != null ? asset.getIssuedTo().getMailId() : null;
			if (Objects.nonNull(mailId) && !mailId.isEmpty()) {
				emailCell.setCellValue(mailId);
				emailCell.setCellStyle(emailStyle);
			} else {
				emailCell.setCellValue("In Stock");
				emailCell.setCellStyle(inStockStyle);
			}
			sheet.autoSizeColumn(11);
			Cell warrantyDateCell = dataRow.createCell(12);
			LocalDate warrantyDate = asset.getWarrantyDate(); // Assuming the warranty date is of type LocalDate
			if (Objects.nonNull(warrantyDate)) {
				warrantyDateCell.setCellValue(dateTimeFormatter.format(warrantyDate));
			} else {
				warrantyDateCell.setCellValue("");
			}
			warrantyDateCell.setCellStyle(style);
			sheet.autoSizeColumn(12);
			Cell allocationDateCell = dataRow.createCell(13);
			allocationDateCell.setCellStyle(style);
			LocalDate allocationDate = asset.getAllocationDate();
			if (Objects.nonNull(allocationDate)) {
				allocationDateCell.setCellValue(dateTimeFormatter.format(allocationDate));
			} else {
				allocationDateCell.setCellValue("");
			}
			sheet.autoSizeColumn(13);
			Cell expiryDateCell = dataRow.createCell(14);
			LocalDate expiryDate = asset.getExpiryDate();
			expiryDateCell.setCellStyle(style);
			if (Objects.nonNull(expiryDate)) {
				expiryDateCell.setCellValue(dateTimeFormatter.format(expiryDate));
			} else {
				expiryDateCell.setCellValue("");
			}
			sheet.autoSizeColumn(14);
			Cell deviceStatusCell = dataRow.createCell(15);
			deviceStatusCell.setCellValue(asset.getDeviceStatus().getName());
			deviceStatusCell.setCellStyle(style);
			sheet.autoSizeColumn(15);
			Cell clientNameCell = dataRow.createCell(16);
			clientNameCell.setCellValue(asset.getClientName());
			clientNameCell.setCellStyle(style);
			sheet.autoSizeColumn(16);
			Cell assetCommentCell = dataRow.createCell(17);
			assetCommentCell.setCellValue(asset.getComment());
			assetCommentCell.setCellStyle(style);
			sheet.autoSizeColumn(17);
			rowIndex++;
		}
		ServletOutputStream ops = response.getOutputStream();
		workbook.write(ops);
		workbook.close();
		ops.close();
	}

}
