package com.TruMIS.assetservice.entity.metadeta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset_maker_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetMaker {
	@Column(name = "id")
	private String id;

	@Id
	@Column(name = "code")
	private String code;

	@Column(name = "field_name")
	private String name;
}