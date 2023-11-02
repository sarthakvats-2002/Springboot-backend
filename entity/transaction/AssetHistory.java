package com.TruMIS.assetservice.entity.transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "asset_history")
@AllArgsConstructor
@NoArgsConstructor
public class AssetHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "emp_name")
	private String empName;

	@Column(name = "emp_email")
	private String empEmail;

	@Column(name = "rep_manager")
	private String reportngManager;

	@Column(name = "service_tag")
	private String serviceTag;

	@Column(name = "from_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate fromDate;

	@Column(name = "to_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate toDate;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createDateTime;

	@UpdateTimestamp
	@Column(name = "updated_at", updatable = true)
	private LocalDateTime updateDateTime;

	@Column(name = "updated_by")
	private String updatedBy;
}
