package com.TruMIS.assetservice.entity.metadeta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset_rep_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetReportsMetadata {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "name")
    private String name;
}
