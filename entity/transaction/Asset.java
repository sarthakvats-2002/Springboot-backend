package com.TruMIS.assetservice.entity.transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.TruMIS.assetservice.entity.Depend.Employee;
import com.TruMIS.assetservice.entity.metadeta.AssetDeviceStatus;
import com.TruMIS.assetservice.entity.metadeta.AssetLocation;
import com.TruMIS.assetservice.entity.metadeta.AssetMaker;
import com.TruMIS.assetservice.entity.metadeta.AssetProcessor;
import com.TruMIS.assetservice.entity.metadeta.AssetRam;
import com.TruMIS.assetservice.entity.metadeta.AssetType;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Asset {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "asset_type")
	private AssetType assetType;

	@ManyToOne
	@JoinColumn(name = "asset_maker")
	private AssetMaker assetMaker;

	@Column(name = "asset_vendor")
	private String assetVendor;

	@Column(name = "asset_model")
	private String assetModel;

	@ManyToOne
	@JoinColumn(name = "asset_processor")
	private AssetProcessor assetProcessor;

	@ManyToOne
	@JoinColumn(name = "asset_ram")
	private AssetRam assetRAM;

	@Column(name = "service_tag", unique = true)
	@NonNull
	private String serviceTag;

	@ManyToOne
	@JoinColumn(name = "issued_to")
	private Employee issuedTo;

	@Column(name = "invoice_no")
	private String invoiceNo;

	@Column(name = "allocation_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate allocationDate;

	@Column(name = "return_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate returnDate;

	@Column(name = "warranty_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate warrantyDate;

	@Column(name = "purchase_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate purchaseDate;

	@Column(name = "expiry_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate expiryDate;

	@ManyToOne
	@JoinColumn(name = "device_status")
	private AssetDeviceStatus deviceStatus;

	@ManyToOne
	@JoinColumn(name = "asset_location")
	private AssetLocation assetLocation;

	@Column(name = "asset_entity")
	private String assetEntity;

	@Column(name = "is_issued")
	private Boolean isIssued;

	@Column(name = "is_client")
	private Boolean isClient;

	@Column(name = "client_name")
	private String clientName;

	@Column(name = "comment", length = 400)
	private String comment;

	@Column(name = "is_active")
	private Boolean isActive;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createDateTime;

	@UpdateTimestamp
	@Column(name = "updated_at", updatable = true)
	private LocalDateTime updateDateTime;

	@Column(name = "updated_by")
	private String updatedBy;
}
