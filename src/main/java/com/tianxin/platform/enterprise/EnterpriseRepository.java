package com.tianxin.platform.enterprise;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mysql")
public class EnterpriseRepository {

    private final JdbcTemplate jdbcTemplate;

    public EnterpriseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Enterprise> list(EnterpriseCategory category) {
        String sql = "SELECT id, company_code, company_name, category, category_name, business_scope, sort_order, enabled "
                + "FROM enterprise_company WHERE enabled = 1";
        if (category == null) {
            return jdbcTemplate.query(sql + " ORDER BY category, sort_order", this::mapEnterprise);
        }
        return jdbcTemplate.query(sql + " AND category = ? ORDER BY sort_order", this::mapEnterprise, category.name());
    }

    public List<EnterpriseCategorySummary> summarizeByCategory() {
        return jdbcTemplate.query("SELECT category, category_name, COUNT(*) AS total FROM enterprise_company "
                        + "WHERE enabled = 1 GROUP BY category, category_name ORDER BY MIN(sort_order)",
                (resultSet, rowNumber) -> new EnterpriseCategorySummary(
                        EnterpriseCategory.valueOf(resultSet.getString("category")), resultSet.getString("category_name"),
                        resultSet.getLong("total")));
    }

    private Enterprise mapEnterprise(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Enterprise(java.util.UUID.fromString(resultSet.getString("id")), resultSet.getString("company_code"),
                resultSet.getString("company_name"), EnterpriseCategory.valueOf(resultSet.getString("category")),
                resultSet.getString("category_name"), resultSet.getString("business_scope"),
                resultSet.getInt("sort_order"), resultSet.getBoolean("enabled"));
    }
}
