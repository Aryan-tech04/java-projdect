package com.studentledger.repository;

import com.studentledger.model.Asset;
import com.studentledger.model.AssetCategory;

import java.util.List;
import java.util.Optional;

public interface AssetRepository {
    List<Asset> findAll(String search, AssetCategory category);
    Optional<Asset> findById(Long id);
    Asset save(Asset asset);
    Asset update(Long id, Asset asset);
    void delete(Long id);
}
