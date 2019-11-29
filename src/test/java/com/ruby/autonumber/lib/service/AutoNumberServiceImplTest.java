package com.ruby.autonumber.lib.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ruby.autonumber.lib.exception.AutoNumberException;
import com.ruby.autonumber.lib.mysql.AutoNumberJdbiService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;

@RunWith(MockitoJUnitRunner.class)
public class AutoNumberServiceImplTest {

    private AutoNumberServiceImpl autoNumberServiceImpl;
    private String tenantId;
    private String objectName;
    private String fieldName;
    private String sequenceName;
    private String fourDigitYear;
    private String twoDigitYear;
    private String month;
    private String day;

    @Mock
    AutoNumberJdbiService autoNumberJdbiService;

    @Before
    public void setup() {
        this.tenantId = "66a99d36-dfe7-11e9-8a34-2a2ae2dbcce4";
        this.objectName = "menu";
        this.fieldName = "TestAutoNumber__c";
        this.sequenceName = "SequenceName";
        this.fourDigitYear = getFourDigitYear();
        this.twoDigitYear = getTwoDigitYear();
        this.month = getMonth();
        this.day = getDayOfMonth();
        autoNumberServiceImpl = new AutoNumberServiceImpl(autoNumberJdbiService);
    }

    @Test
    public void createAutoNumberOneSequence() {
        long startingNumber = 1;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "{0}", startingNumber);
        Assert.assertEquals(Long.toString(startingNumber), autoNumber);
    }

    @Test
    public void createAutoNumberWithMonth() {
        long startingNumber = 1;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "{0}-{MM}", startingNumber);
        Assert.assertEquals(startingNumber + "-" + month, autoNumber);
    }

    @Test
    public void createAutoNumberMonthAndTwoDigitYear() {
        long startingNumber = 1;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "{0}-{YY}-{MM}", startingNumber);
        Assert.assertEquals(startingNumber + "-" + twoDigitYear + "-" + month, autoNumber);

    }

    @Test
    public void createAutoNumberMonthAndFourDigitYear() {
        long startingNumber = 1;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "{0}-{MM}-{YYYY}", startingNumber);
        Assert.assertEquals(startingNumber + "-" + month + "-" + fourDigitYear, autoNumber);
    }

    @Test
    public void createAutoNumberWithMonthYearDay() {
        long startingNumber = 1;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "{0}-{MM}-{YYYY}-{DD}", startingNumber);
        Assert.assertEquals(startingNumber + "-" + month + "-" + fourDigitYear + "-" + day, autoNumber);
    }

    @Test
    public void createAutoNumberWithMonthYearDayAndSuffix() {
        long startingNumber = 1;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "{0}-{MM}-{YYYY}-{DD}-B", startingNumber);
        Assert.assertEquals(startingNumber + "-" + month + "-" + fourDigitYear + "-" + day + "-B", autoNumber);
    }

    @Test
    public void createAutoNumberWithMonthYearDayAndPrefixAndSuffix() {
        long startingNumber = 1;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "A-{0}-{YY}-{MM}-{DD}-B", startingNumber);
        Assert.assertEquals("A-" + startingNumber + "-" + twoDigitYear + "-" + month + "-" + day + "-B", autoNumber);
    }

    @Test
    public void createAutoNumberWithPaddedZeros() {
        long startingNumber = 9;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "A-{0000}", startingNumber);
        Assert.assertEquals("A-000" + startingNumber, autoNumber);
    }

    @Test
    public void createAutoNumberWithNoPaddedZeros() {
        long startingNumber = 9999;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "A-{0000}", startingNumber);
        Assert.assertEquals("A-" + startingNumber, autoNumber);
    }

    @Test
    public void createAutoNumberMaxSequenceNumberSize() {
        long startingNumber = 999999999;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "{000000000}", startingNumber);
        Assert.assertEquals(Long.toString(startingNumber), autoNumber);
    }

    @Test
    public void createAutoNumberMoreNumbersThanSequence() {
        long startingNumber = 10;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "A-{0}", startingNumber);
        Assert.assertEquals("A-" + startingNumber, autoNumber);
    }
    
    @Test
    public void createMaxSequenceNumberValues() {
        // 20 prefix/suffix characters & 10 padded 0s
        long startingNumber = 999999999;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "aaaaaaaaaaaaaaaaaaaa{0000000000}", startingNumber);
        Assert.assertEquals("aaaaaaaaaaaaaaaaaaaa0" + startingNumber, autoNumber);
    }

    @Test
    public void createAutoNumberNoDisplayFormat() {
        // 20 prefix/suffix characters & 10 padded 0s
        long startingNumber = 999999999;
        mockJdbiService(startingNumber);
        String autoNumber = autoNumberServiceImpl.generateAutoNumber(tenantId, objectName, fieldName, "", startingNumber);
        Assert.assertEquals(Long.toString(startingNumber), autoNumber);
    }

    private void mockJdbiService(long sequenceNumber) {
        when(autoNumberJdbiService.createSequence(anyString(), anyLong())).thenReturn(sequenceName);
        when(autoNumberJdbiService.getNextSequenceValue(anyString())).thenReturn(sequenceNumber);
    }

    private String getMonth() {
        return String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    private String getFourDigitYear() {
        return String.format("%04d", Calendar.getInstance().get(Calendar.YEAR));
    }

    private String getTwoDigitYear() {
        return String.format("%02d", Calendar.getInstance().get(Calendar.YEAR) % 100);
    }

    private String getDayOfMonth() {
        return String.format("%02d", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }
}
