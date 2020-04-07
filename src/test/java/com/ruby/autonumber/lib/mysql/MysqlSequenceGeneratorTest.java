package com.ruby.autonumber.lib.mysql;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ruby.autonumber.lib.service.SequenceCategory;
import com.ruby.autonumber.lib.service.SequenceConfig;
import com.ruby.autonumber.lib.service.SequenceIterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

@RunWith(MockitoJUnitRunner.class)
public class MysqlSequenceGeneratorTest {

    @Mock
    private DataSource dataSource;

    @Test
    public void testSingleThread() {
        MysqlSequenceGenerator sequenceGenerator = Mockito.spy(new MysqlSequenceGenerator(dataSource, new SequenceConfig("test_seq", "seq_num", 2)));
        SequenceCategory sequenceCategory = new SequenceCategory("testKey", "testTenant", 0);
        Mockito.doReturn(new SequenceIterator(0, 10), new SequenceIterator(10, 20)).when(sequenceGenerator).getSequenceIterator(sequenceCategory);

        for (int i = 1; i <= 20; i++) {
            long nextKey = sequenceGenerator.getNextKey(sequenceCategory);
            assertEquals(i, nextKey);
        }

        verify(sequenceGenerator, times(2)).getSequenceIterator(any(SequenceCategory.class));
    }

    @Test
    public void testMultipleThread() {
        MysqlSequenceGenerator sequenceGenerator = Mockito.spy(new MysqlSequenceGenerator(dataSource, new SequenceConfig("test_seq", "seq_num", 2)));
        SequenceCategory sequenceCategory = new SequenceCategory("testKey", "testTenant", 0);

        doReturn(new SequenceIterator(0, 5), new SequenceIterator(5, 10),
                new SequenceIterator(10, 15), new SequenceIterator(15, 20), new SequenceIterator(20, 25),
                new SequenceIterator(25, 30), new SequenceIterator(30, 35),
                new SequenceIterator(35, 40), new SequenceIterator(40, 45),
                new SequenceIterator(45, 50)).when(sequenceGenerator).getSequenceIterator(sequenceCategory);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Long> sequences = new ArrayList<>();
        try {
            List<Future<List<Long>>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Future<List<Long>> seqFuture = executorService.submit(() -> {
                    Thread.currentThread().sleep((long) (Math.random() * 1000));
                    List<Long> seq = new ArrayList<>();
                    for (int j = 1; j <= 5; j++) {
                        long nextKey = sequenceGenerator.getNextKey(sequenceCategory);
                        seq.add(nextKey);
                    }
                    return seq;
                });
                futures.add(seqFuture);
            }

            for (Future<List<Long>> future : futures) {
                sequences.addAll(future.get());
            }
            Collections.sort(sequences);
            for (int i = 0; i < 50; i++) {
                assertEquals(i + 1, sequences.get(i).intValue());
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            executorService.shutdownNow();
        } finally {
            executorService.shutdown();
        }
    }

}