package com.ruby.autonumber.lib.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceIterator implements Iterator<Long> {

    private AtomicLong nextVal;

    private long maxVal;

    public SequenceIterator(long nextVal, long maxVal) {
        checkArgument(nextVal >= 0);
        checkArgument(maxVal > nextVal);
        this.nextVal = new AtomicLong(nextVal);
        this.maxVal = maxVal;
    }

    @Override
    public boolean hasNext() {
        return nextVal.get() < maxVal;
    }

    @Override
    public synchronized Long next() {
        long result = this.nextVal.incrementAndGet();
        if (result > maxVal) {
            result = -1;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequenceIterator that = (SequenceIterator) o;
        return maxVal == that.maxVal &&
                Objects.equals(nextVal, that.nextVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextVal, maxVal);
    }
}
