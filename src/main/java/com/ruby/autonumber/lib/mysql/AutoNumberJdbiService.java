package com.ruby.autonumber.lib.mysql;

public interface AutoNumberJdbiService {

    String createSequence(String sequenceName, long startNumber);

    long getNextSequenceValue(String sequenceName);
}
