package com.ruby.autonumber.lib.service;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import com.ruby.commons.NoSessionException;
import com.ruby.commons.UserSessionContext;
import com.ruby.commons.UserSessionContextHolder;

import com.google.common.base.Strings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultAutoNumberGenerator implements AutoNumberGenerator {

    private static final DateTimeFormatter ISO_YEAR_FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendValue(YEAR, 4).toFormatter();

    private static final DateTimeFormatter ISO_MONTH_FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendValue(MONTH_OF_YEAR, 2).toFormatter();

    private static final DateTimeFormatter ISO_DAY_FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendValue(DAY_OF_MONTH, 2).toFormatter();

    private final SequenceGenerator sequenceGenerator;

    public DefaultAutoNumberGenerator(SequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public String generate(AutoNumberConfig config) {
        UserSessionContext sessionContext = UserSessionContextHolder.getContext().orElseThrow(() -> new NoSessionException("No user session available"));
        long nextSequenceValue = sequenceGenerator.getNextKey(
                new SequenceCategory(config.getFieldName(), sessionContext.getTenantId(), config.getStartingNumber()));
        String nextSquenceValueStr = String.valueOf(nextSequenceValue);

        String result = nextSquenceValueStr;
        if (!Strings.isNullOrEmpty(config.getDisplayFormat())) {
            result = createAutoNumberString(config.getDisplayFormat(), nextSquenceValueStr, sessionContext);
        }
        return result;
    }

    private String createAutoNumberString(String displayFormat, String nextSquenceValueStr, UserSessionContext sessionContext) {
        LocalDate today = LocalDate.now(sessionContext.getZoneId());
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
            } else if (section.equals("{YYYY}") || section.equals("{yyyy}")) {
                displayFormat = displayFormat.replace(section, today.format(ISO_YEAR_FORMATTER));
            } else if (section.equals("{MM}") || section.equals("{mm}")) {
                displayFormat = displayFormat.replace(section, today.format(ISO_MONTH_FORMATTER));
            } else if (section.equals("{DD}") || section.equals("{dd}")) {
                displayFormat = displayFormat.replace(section, today.format(ISO_DAY_FORMATTER));
            }
        }
        return displayFormat;
    }
}
