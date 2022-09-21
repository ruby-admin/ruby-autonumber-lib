package com.ruby.autonumber.lib.mysql;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.ruby.autonumber.lib.exception.SequenceGenerationException;
import com.ruby.autonumber.lib.service.SequenceCategory;
import com.ruby.autonumber.lib.service.SequenceConfig;
import com.ruby.autonumber.lib.service.SequenceGenerator;
import com.ruby.autonumber.lib.service.SequenceIterator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

public class MysqlSequenceGenerator implements SequenceGenerator {

    private Jdbi jdbi;

    private Map<SequenceCategory, SequenceIterator> sequenceIteratorMap;

    private final SequenceConfig sequenceConfig;

    public MysqlSequenceGenerator(DataSource dataSource, SequenceConfig config) {
        requireNonNull(dataSource);
        this.sequenceConfig = config;
        //to use spring transactional aware datasource, use TransactionAwareDataSourceProxy
        this.jdbi = Jdbi.create(dataSource);
        this.jdbi.registerArgument(new BinaryUUIDArgumentFactory());
        this.sequenceIteratorMap = new ConcurrentHashMap<>();
    }

    @Override
    public long getNextKey(SequenceCategory sequenceCategory) {
        checkArgument(sequenceCategory != null);
        SequenceIterator sequenceIterator = this.sequenceIteratorMap.computeIfAbsent(sequenceCategory, sc -> getSequenceIterator(sc));
        if (sequenceIterator.hasNext()) {
            long nextKey = sequenceIterator.next();
            if (nextKey < 0) { //cached sequence exhausted
                this.sequenceIteratorMap.remove(sequenceCategory, sequenceIterator);
                return getNextKey(sequenceCategory);
            } else {
                return nextKey;
            }
        } else {
            this.sequenceIteratorMap.remove(sequenceCategory, sequenceIterator);
            return getNextKey(sequenceCategory);
        }
    }

    @VisibleForTesting
    protected SequenceIterator getSequenceIterator(SequenceCategory sequenceCategory) {
        try {
            String tenantId = sequenceCategory.getNamespace();
            String keyName = sequenceCategory.getKeyName();
            long startValue = sequenceCategory.getStartValue();
            return jdbi.withHandle(handle -> {
                int rowCount = handle.createUpdate(
                        String.format("update %s set %s = (@newval := (@oldval := %s) + %d) where field_name = :key and tenant_id = :tenantId",
                                sequenceConfig.getSequenceName(),
                                sequenceConfig.getColumnName(), sequenceConfig.getColumnName(), sequenceConfig.getCacheSize()))
                        .bindMap(ImmutableMap.of("key", keyName, "tenantId", UUID.fromString(tenantId)))
                        .execute();
                if (rowCount <= 0) {
                    //create sequence
                    int insertedRow = handle.createUpdate(
                            String.format("insert into %s (id, tenant_id, field_name, %s) values (:id, :tenantId, :key, :startValue)",
                                    sequenceConfig.getSequenceName(),
                                    sequenceConfig.getColumnName()))
                            .bindMap(ImmutableMap.of("id", UUID.randomUUID(),
                                    "tenantId", UUID.fromString(tenantId),
                                    "key", keyName,
                                    "startValue", startValue + sequenceConfig.getCacheSize()))
                            .execute();
                    checkState(insertedRow == 1, "Cannot create sequence, " + sequenceConfig);
                    return new SequenceIterator(0, sequenceConfig.getCacheSize());
                } else {
                    Optional<Map<String, Object>> result = handle.createQuery("select @newval, @oldval").mapToMap().findOne();
                    checkState(result.isPresent(), "No updated sequence available, tid=" + tenantId + ", key=" + keyName);
                    Map<String, Object> sequenceMap = result.get();

                    return new SequenceIterator((Long) sequenceMap.get("@oldval"), (Long) sequenceMap.get("@newval"));
                }

            });
        } catch (Exception ex) {
            throw new SequenceGenerationException("Cannot generate sequence with " + sequenceCategory, ex);
        }

    }

}
