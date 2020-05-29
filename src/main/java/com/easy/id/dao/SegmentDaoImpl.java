package com.easy.id.dao;

import com.easy.id.entity.Segment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@Repository
public class SegmentDaoImpl implements SegmentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Segment selectByBusinessType(String businessType) {
        return jdbcTemplate.queryForObject("select * from segment where business_type=?", new SegmentMapper());
    }

    public static class SegmentMapper implements RowMapper<Segment> {
        @Override
        public Segment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Segment segment = new Segment();
            segment.setId(rs.getLong("id"));
            segment.setVersion(rs.getLong("version"));
            segment.setBusinessType(rs.getString("business_type"));
            segment.setMaxId(rs.getLong("max_id"));
            segment.setIncrement(rs.getInt("increment"));
            segment.setRemainder(rs.getInt("remainder"));
            segment.setStep(rs.getInt("step"));
            segment.setUpdatedAt(rs.getLong("created_at"));
            segment.setUpdatedAt(rs.getLong("updated_at"));
            return segment;
        }
    }
}
