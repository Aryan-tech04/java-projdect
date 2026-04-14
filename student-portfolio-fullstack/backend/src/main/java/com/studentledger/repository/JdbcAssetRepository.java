package com.studentledger.repository;

import com.studentledger.exception.ResourceNotFoundException;
import com.studentledger.model.Asset;
import com.studentledger.model.AssetCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAssetRepository implements AssetRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Asset> assetRowMapper = (rs, rowNum) -> {
        Asset asset = new Asset();
        asset.setId(rs.getLong("id"));
        asset.setAssetName(rs.getString("asset_name"));
        asset.setTickerSymbol(rs.getString("ticker_symbol"));
        asset.setCategory(AssetCategory.valueOf(rs.getString("category")));
        asset.setQuantity(rs.getInt("quantity"));
        asset.setBuyPrice(rs.getDouble("buy_price"));
        asset.setCurrentPrice(rs.getDouble("current_price"));
        asset.setRiskLevel(rs.getString("risk_level"));
        asset.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        asset.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        asset.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return asset;
    };

    @Override
    public List<Asset> findAll(String search, AssetCategory category) {
        StringBuilder sql = new StringBuilder("select * from assets where 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" and (lower(asset_name) like ? or lower(ticker_symbol) like ?)");
            String searchValue = "%" + search.toLowerCase() + "%";
            params.add(searchValue);
            params.add(searchValue);
        }

        if (category != null) {
            sql.append(" and category = ?");
            params.add(category.name());
        }

        sql.append(" order by updated_at desc, id desc");
        return jdbcTemplate.query(sql.toString(), assetRowMapper, params.toArray());
    }

    @Override
    public Optional<Asset> findById(Long id) {
        List<Asset> assets = jdbcTemplate.query("select * from assets where id = ?", assetRowMapper, id);
        return assets.stream().findFirst();
    }

    @Override
    public Asset save(Asset asset) {
        String sql = "insert into assets (asset_name, ticker_symbol, category, quantity, buy_price, current_price, risk_level, notes, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, asset.getAssetName());
            ps.setString(2, asset.getTickerSymbol());
            ps.setString(3, asset.getCategory().name());
            ps.setInt(4, asset.getQuantity());
            ps.setDouble(5, asset.getBuyPrice());
            ps.setDouble(6, asset.getCurrentPrice());
            ps.setString(7, asset.getRiskLevel());
            ps.setString(8, asset.getNotes());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            return findById(key.longValue()).orElseThrow(() -> new ResourceNotFoundException("Asset was created but could not be loaded."));
        }
        throw new ResourceNotFoundException("Asset could not be created.");
    }

    @Override
    public Asset update(Long id, Asset asset) {
        String sql = "update assets set asset_name = ?, ticker_symbol = ?, category = ?, quantity = ?, buy_price = ?, current_price = ?, risk_level = ?, notes = ?, updated_at = current_timestamp where id = ?";
        int updated = jdbcTemplate.update(sql,
                asset.getAssetName(),
                asset.getTickerSymbol(),
                asset.getCategory().name(),
                asset.getQuantity(),
                asset.getBuyPrice(),
                asset.getCurrentPrice(),
                asset.getRiskLevel(),
                asset.getNotes(),
                id);

        if (updated == 0) {
            throw new ResourceNotFoundException("Asset with id " + id + " was not found.");
        }
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset with id " + id + " was not found."));
    }

    @Override
    public void delete(Long id) {
        int deleted = jdbcTemplate.update("delete from assets where id = ?", id);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Asset with id " + id + " was not found.");
        }
    }
}
