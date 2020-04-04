package com.ruby.autonumber.lib.service;

public interface AutoNumberService {

    String generateAutoNumber(String tenantId, String objectName, String fieldName, String displayFormat, long startingNumber);

}
