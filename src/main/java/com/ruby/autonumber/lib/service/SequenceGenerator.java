package com.ruby.autonumber.lib.service;

public interface SequenceGenerator {

    long getNextKey(SequenceCategory sequenceCategory);
}
