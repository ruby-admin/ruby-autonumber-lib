package com.ruby.autonumber.lib.service;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;

import java.util.Objects;

public class SequenceConfig {

    private final String sequenceName;

    private final String columnName;

    private final int cacheSize;

    public SequenceConfig(String sequenceName, String columnName) {
        this(sequenceName, columnName, 1);
    }

    public SequenceConfig(String sequenceName, String columnName, int cacheSize) {
        checkArgument(!Strings.isNullOrEmpty(sequenceName));
        checkArgument(!Strings.isNullOrEmpty(columnName));
        checkArgument(cacheSize >= 1);
        this.sequenceName = sequenceName;
        this.columnName = columnName;
        this.cacheSize = cacheSize;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    @Override
    public String toString() {
        return "SequenceConfig{" +
                "sequenceName='" + sequenceName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", cacheSize=" + cacheSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequenceConfig that = (SequenceConfig) o;
        return cacheSize == that.cacheSize &&
                Objects.equals(sequenceName, that.sequenceName) &&
                Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceName, columnName, cacheSize);
    }
}
