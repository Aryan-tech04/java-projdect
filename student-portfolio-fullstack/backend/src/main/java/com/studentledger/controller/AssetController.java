package com.studentledger.controller;

import com.studentledger.dto.ApiResponse;
import com.studentledger.dto.AssetRequest;
import com.studentledger.model.Asset;
import com.studentledger.service.AssetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public ApiResponse<List<Asset>> getAllAssets(@RequestParam(required = false) String search,
                                                 @RequestParam(required = false) String category) {
        return ApiResponse.success("Assets loaded successfully.", assetService.getAllAssets(search, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Asset> getAssetById(@PathVariable Long id) {
        return ApiResponse.success("Asset loaded successfully.", assetService.getAsset(id));
    }

    @PostMapping
    public ApiResponse<Asset> createAsset(@RequestBody AssetRequest request) {
        return ApiResponse.success("Asset created successfully.", assetService.createAsset(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Asset> updateAsset(@PathVariable Long id, @RequestBody AssetRequest request) {
        return ApiResponse.success("Asset updated successfully.", assetService.updateAsset(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ApiResponse.success("Asset deleted successfully.", null);
    }
}
