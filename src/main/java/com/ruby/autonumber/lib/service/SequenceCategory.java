package com.ruby.autonumber.lib.service;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;

import java.util.Objects;

public final class SequenceCategory {

    private final String keyName;

    private final String namespace;

    private final long startValue;

    public SequenceCategory(String keyName, String namespace, long startValue) {
        checkArgument(!Strings.isNullOrEmpty(keyName));
        checkArgument(!Strings.isNullOrEmpty(namespace));
        checkArgument(startValue >= 0);
        this.startValue = startValue;
        this.keyName = keyName;
        this.namespace = namespace;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequenceCategory that = (SequenceCategory) o;
        return startValue == that.startValue &&
                Objects.equals(keyName, that.keyName) &&
                Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyName, namespace, startValue);
    }

    public long getStartValue() {
        return startValue;
    }

    @Override
    public String toString() {
        return "SequenceCategory{" +
                "keyName='" + keyName + '\'' +
                ", namespace='" + namespace + '\'' +
                ", startValue=" + startValue +
                '}';
    }

}
