package com.studentledger.service;

import com.studentledger.dto.AssetRequest;
import com.studentledger.exception.InvalidRequestException;
import com.studentledger.exception.ResourceNotFoundException;
import com.studentledger.model.Asset;
import com.studentledger.model.AssetCategory;
import com.studentledger.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {
    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> getAllAssets(String search, String category) {
        AssetCategory assetCategory = parseCategory(category);
        return assetRepository.findAll(search, assetCategory);
    }

    public Asset getAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset with id " + id + " was not found."));
    }

    public Asset createAsset(AssetRequest request) {
        Asset asset = buildAssetFromRequest(request);
        return assetRepository.save(asset);
    }

    public Asset updateAsset(Long id, AssetRequest request) {
        Asset updatedAsset = buildAssetFromRequest(request);
        return assetRepository.update(id, updatedAsset);
    }

    public void deleteAsset(Long id) {
        assetRepository.delete(id);
    }

    private Asset buildAssetFromRequest(AssetRequest request) {
        validateRequest(request);
        Asset asset = new Asset();
        asset.setAssetName(request.getAssetName().trim());
        asset.setTickerSymbol(request.getTickerSymbol().trim().toUpperCase());
        asset.setCategory(parseCategory(request.getCategory()));
        asset.setQuantity(request.getQuantity());
        asset.setBuyPrice(request.getBuyPrice());
        asset.setCurrentPrice(request.getCurrentPrice());
        asset.setRiskLevel(request.getRiskLevel().trim().toUpperCase());
        asset.setNotes(request.getNotes() == null ? "" : request.getNotes().trim());
        return asset;
    }

    private void validateRequest(AssetRequest request) {
        if (request.getAssetName() == null || request.getAssetName().isBlank()) {
            throw new InvalidRequestException("Asset name is required.");
        }
        if (request.getTickerSymbol() == null || request.getTickerSymbol().isBlank()) {
            throw new InvalidRequestException("Ticker symbol is required.");
        }
        if (request.getQuantity() <= 0) {
            throw new InvalidRequestException("Quantity must be greater than zero.");
        }
        if (request.getBuyPrice() <= 0 || request.getCurrentPrice() <= 0) {
            throw new InvalidRequestException("Buy price and current price must be greater than zero.");
        }
        if (request.getRiskLevel() == null || request.getRiskLevel().isBlank()) {
            throw new InvalidRequestException("Risk level is required.");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new InvalidRequestException("Category is required.");
        }
        parseCategory(request.getCategory());
    }

    private AssetCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return AssetCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid asset category: " + category);
        }
    }
}
