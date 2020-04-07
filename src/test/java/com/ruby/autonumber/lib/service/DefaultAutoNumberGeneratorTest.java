package com.ruby.autonumber.lib.service;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ruby.commons.UserSessionContext;
import com.ruby.commons.UserSessionRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAutoNumberGeneratorTest {

    @Mock
    private SequenceGenerator sequenceGenerator;

    @Test
    public void itShouldGenerateAutoNumber() {
        DefaultAutoNumberGenerator defaultAutoNumberGenerator = new DefaultAutoNumberGenerator(sequenceGenerator);

        when(sequenceGenerator.getNextKey(any(SequenceCategory.class))).thenReturn(10L);

        String autoNumber = UserSessionRunner.runWith(UserSessionContext.newDummyUserSession("test-tenant"), () -> {
            AutoNumberConfig autoNumberConfig = new AutoNumberConfig("testObj", "testField", "PK-{YYYY}-{MM}-{dd}-{0000000}", 0);
            return defaultAutoNumberGenerator.generate(autoNumberConfig);
        });

        assertThat(autoNumber, endsWith("0000010"));
        assertFalse(autoNumber.contains("{YYYY}-{MM}-{dd}"));
    }
}