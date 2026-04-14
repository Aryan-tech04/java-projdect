package com.studentledger.service;

import com.studentledger.dto.DashboardSummary;
import com.studentledger.model.Asset;
import com.studentledger.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final AssetRepository assetRepository;

    public DashboardService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public DashboardSummary buildSummary() {
        List<Asset> assets = assetRepository.findAll(null, null);
        double totalInvestment = assets.stream()
                .mapToDouble(asset -> asset.getBuyPrice() * asset.getQuantity())
                .sum();

        double marketValue = assets.stream()
                .mapToDouble(asset -> asset.getCurrentPrice() * asset.getQuantity())
                .sum();

        double totalProfit = marketValue - totalInvestment;
        double averageReturn = totalInvestment == 0 ? 0 : (totalProfit / totalInvestment) * 100;

        Map<String, Double> categoryValueMap = assets.stream()
                .collect(Collectors.groupingBy(asset -> asset.getCategory().name(),
                        Collectors.summingDouble(asset -> asset.getCurrentPrice() * asset.getQuantity())));

        String topCategory = categoryValueMap.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("NONE");

        return new DashboardSummary(totalInvestment, marketValue, totalProfit, averageReturn, assets.size(), topCategory);
    }
}
