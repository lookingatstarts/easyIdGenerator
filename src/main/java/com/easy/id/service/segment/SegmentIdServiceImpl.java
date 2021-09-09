package com.easy.id.service.segment;

import com.easy.id.config.DataSourceConfig;
import com.easy.id.config.Module;
import com.easy.id.entity.Segment;
import com.easy.id.entity.SegmentId;
import com.easy.id.exception.FetchSegmentFailException;
import com.easy.id.exception.SegmentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 默认启用号段的方式
 */
@Service
@Slf4j
@Module("segment.enable")
public class SegmentIdServiceImpl implements SegmentIdService {

    private final static String SELECT_BY_BUSINESS_TYPE_SQL = "select * from segment where business_type=?";
    private final static String UPDATE_SEGMENT_MAX_ID = "update segment set max_id= ?,version=?,updated_at=? where id =? and version=?";
    @Value("${easy-id-generator.segment.fetch-segment-retry-times:2}")
    private int retry;
    @Autowired
    private DataSourceConfig.DynamicDataSource dynamicDataSource;

    @Override
    public SegmentId fetchNextSegmentId(String businessType) {
        // 获取segment的时候，有可能存在version冲突，需要重试
        Connection connection;
        try {
            connection = dynamicDataSource.getConnection();
            connection.setAutoCommit(false);
            for (int i = 0; i < retry; i++) {
                PreparedStatement statement = connection.prepareStatement(SELECT_BY_BUSINESS_TYPE_SQL);
                statement.setObject(1, businessType);
                final ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new SegmentNotFoundException("can not find segment of " + businessType);
                }
                final Segment segment = SegmentMapperUtil.mapRow(resultSet);
                statement = connection.prepareStatement(UPDATE_SEGMENT_MAX_ID);
                statement.setObject(1, segment.getMaxId() + segment.getStep());
                statement.setObject(2, segment.getVersion() + 1);
                statement.setObject(3, System.currentTimeMillis());
                statement.setObject(4, segment.getId());
                statement.setObject(5, segment.getVersion());
                try {
                    // 更新成功
                    if (statement.executeUpdate() == 1) {
                        connection.commit();
                        log.debug("fetch {} next segment {} success", businessType, segment);
                        return new SegmentId(segment);
                    }
                    // 乐观锁冲突，重试
                    log.debug("fetch {} next segment {} conflict,retry", businessType, segment);
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
            // 在有限重试机会下，没有获取到segment
            throw new FetchSegmentFailException("fetch " + businessType + " next segment fail after retry " + retry + " times");
        } catch (Exception e) {
            throw new FetchSegmentFailException(e);
        } finally {
            try {
                dynamicDataSource.releaseConnection();
            } catch (SQLException e) {
                log.error("release connection error", e);
            }
        }
    }

    public static class SegmentMapperUtil {
        public static Segment mapRow(ResultSet rs) throws SQLException {
            Segment segment = new Segment();
            segment.setId(rs.getLong("id"));
            segment.setVersion(rs.getLong("version"));
            segment.setBusinessType(rs.getString("business_type"));
            segment.setMaxId(rs.getLong("max_id"));
            segment.setIncrement(rs.getInt("increment"));
            segment.setRemainder(rs.getInt("remainder"));
            segment.setStep(rs.getInt("step"));
            segment.setCreatedAt(rs.getLong("created_at"));
            segment.setUpdatedAt(rs.getLong("updated_at"));
            return segment;
        }
    }
}
