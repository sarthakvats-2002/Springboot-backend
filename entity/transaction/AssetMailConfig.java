package com.TruMIS.assetservice.entity.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset_mail_config")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetMailConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "mail_id", unique = true)
	@NotNull
	private String mailId;

	@Column(name = "location")
	private String location;

	@Column(name = "owner_service")
	@NotNull
	private String ownerService;
}
