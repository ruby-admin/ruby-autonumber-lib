package com.ruby.autonumber.lib.service;

import com.ruby.autonumber.lib.exception.AutoNumberException;
import com.ruby.autonumber.lib.mysql.AutoNumberJdbiService;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoNumberServiceImpl implements AutoNumberService {

    private AutoNumberJdbiService autoNumberJdbiService;

    public AutoNumberServiceImpl(AutoNumberJdbiService autoNumberJdbiService) {
        this.autoNumberJdbiService = autoNumberJdbiService;
    }

    @Override
    public String generateAutoNumber(String tenantId, String objectName, String fieldName, String displayFormat, long startingNumber) {
        String sequenceName = createSequenceName(tenantId, objectName, fieldName);
        String createdSequence = autoNumberJdbiService.createSequence(sequenceName, startingNumber);
        if (StringUtils.isEmpty(createdSequence)) {
            throw new AutoNumberException("There was an error creating an AutoNumber.");
        }
        // the value returned is the first value from the sequence (i.e. startingNumber) and also increments the sequence itself
        // use this value as the starting number
        long nextSequenceValue = autoNumberJdbiService.getNextSequenceValue(sequenceName);
        String nextSquenceValueStr = String.valueOf(nextSequenceValue);
        if (!displayFormat.isEmpty()) {
            displayFormat = createAutoNumberString(displayFormat, nextSquenceValueStr);
        } else {
            displayFormat = nextSquenceValueStr;
        }
        return displayFormat;
    }

    private String createAutoNumberString(String displayFormat, String nextSquenceValueStr) {
        // this regex expression gets all of the values between the curly braces {}
        final Matcher matcher = Pattern.compile("\\{.*?}").matcher(displayFormat);
        // now for every regex expression, we replace the existing display format string with the substitution variables
        while (matcher.find()) {
            String section = matcher.group().subSequence(0, matcher.group().length()).toString();
            // grab sequence section
            if (section.matches("^\\{[0]+}$")) {
                String sequenceSection = "";
                // if the number of 0's in the sequence section equals to the length of the starting number digits, the startingNumber starts the sequence
                if (nextSquenceValueStr.length() >= section.length() - 2) {
                    sequenceSection = nextSquenceValueStr;
                } else {
                    // the section with 0's can be padded with leading 0's.
                    // subtract 2 because the {} are counted as characters
                    sequenceSection = section.substring(1, section.length() - (nextSquenceValueStr.length() + 1)) + nextSquenceValueStr;
                }
                displayFormat = displayFormat.replace(section, sequenceSection);
            } else if (section.equals("{YY}") || section.equals("{yy}")) {
                displayFormat = displayFormat.replace(section, new SimpleDateFormat("yy").format(new Date()));
            } else if (section.equals("{YYYY}") || section.equals("{yyyy}")) {
                displayFormat = displayFormat.replace(section, new SimpleDateFormat("yyyy").format(new Date()));
            } else if (section.equals("{MM}") || section.equals("{mm}")) {
                displayFormat = displayFormat.replace(section, new SimpleDateFormat("MM").format(new Date()));
            } else if (section.equals("{DD}") || section.equals("{dd}")) {
                displayFormat = displayFormat.replace(section, new SimpleDateFormat("dd").format(new Date()));
            }
        }
        return displayFormat;
    }


    /**
     * Create the name of a sequence. We need to specifically create it so that future autonumbers can be incremented
     * Sequence names can only be up to 64 characters max so we need to limit the name of the auto number field.
     * @param tenantId
     * @param objectName
     * @param fieldName
     */
    private String createSequenceName(String tenantId, String objectName, String fieldName) {
        try {
            String combinedName = tenantId + objectName + fieldName;
            return generateSequenceName(combinedName);
        } catch (NoSuchAlgorithmException e) {
            throw new AutoNumberException(e);
        }
    }

    /**
     * Generate a unique, 64-character string for the sequence table name.
     * @return
     * @throws NoSuchAlgorithmException
     */
    private String generateSequenceName(String combinedName) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(
                combinedName.getBytes(StandardCharsets.UTF_8));
        return convertToHex(encodedhash);
    }

    private String convertToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return "seq_" + hexString.toString().substring(0, 60);
    }
}