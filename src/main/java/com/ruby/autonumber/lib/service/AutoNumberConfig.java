package com.ruby.autonumber.lib.service;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;

public class AutoNumberConfig {

    private String objectName;

    private String fieldName;

    private String displayFormat;

    private long startingNumber;

    public AutoNumberConfig(String objectName, String fieldName, String displayFormat, long startingNumber) {
        checkArgument(!Strings.isNullOrEmpty(objectName));
        checkArgument(!Strings.isNullOrEmpty(fieldName));
        checkArgument(startingNumber >= 0);
        this.objectName = objectName;
        this.fieldName = fieldName;
        this.displayFormat = displayFormat;
        this.startingNumber = startingNumber;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayFormat() {
        return displayFormat;
    }

    public long getStartingNumber() {
        return startingNumber;
    }
}
