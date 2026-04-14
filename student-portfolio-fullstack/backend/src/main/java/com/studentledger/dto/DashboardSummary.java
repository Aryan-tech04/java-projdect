package com.studentledger.dto;

public class DashboardSummary {
    private double totalInvestment;
    private double marketValue;
    private double totalProfit;
    private double averageReturnPercentage;
    private long totalAssets;
    private String topCategory;

    public DashboardSummary() {
    }

    public DashboardSummary(double totalInvestment, double marketValue, double totalProfit,
                            double averageReturnPercentage, long totalAssets, String topCategory) {
        this.totalInvestment = totalInvestment;
        this.marketValue = marketValue;
        this.totalProfit = totalProfit;
        this.averageReturnPercentage = averageReturnPercentage;
        this.totalAssets = totalAssets;
        this.topCategory = topCategory;
    }

    public double getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(double totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(double marketValue) {
        this.marketValue = marketValue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public double getAverageReturnPercentage() {
        return averageReturnPercentage;
    }

    public void setAverageReturnPercentage(double averageReturnPercentage) {
        this.averageReturnPercentage = averageReturnPercentage;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }
}
