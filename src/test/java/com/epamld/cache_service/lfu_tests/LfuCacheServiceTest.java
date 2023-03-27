package com.epamld.cache_service.lfu_tests;

import com.epam.ld.cache_service.Entity;
import com.epam.ld.cache_service.lfu.LfuCacheMap;
import com.epam.ld.cache_service.lfu.LfuCacheService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class LfuCacheServiceTest {

    @Test
    public void testLoadFactor_ShouldWorkCorrectly() {
        //Given
        LfuCacheMap<Integer, Entity> cacheMap = new LfuCacheMap<Integer, Entity>(3, 0.1f, "log.txt");
        LfuCacheService<Integer, Entity> service = new LfuCacheService<>(cacheMap);

        //When
        service.put(1, new Entity("one"));
        service.get(1);
        service.get(1);
        service.put(2, new Entity("two"));
        service.put(3, new Entity("three"));
        service.put(4, new Entity("four"));

        //Then
        assertFalse(cacheMap.containsKey(2));
        assertTrue(cacheMap.containsKey(1));
        assertEquals(3, cacheMap.size());
        assertEquals(3, cacheMap.frequencyOf(1));
    }

    @Test
    public void testEnvisionFactor_ShouldWorkCorrectly() {
        //Given
        LfuCacheMap<Integer, Entity> cacheMap = new LfuCacheMap<Integer, Entity>(4, 0.9f, "log.txt");
        LfuCacheService<Integer, Entity> service = new LfuCacheService<>(cacheMap);

        //When
        service.put(1, new Entity("one"));
        service.put(2, new Entity("two"));
        service.put(3, new Entity("three"));
        service.put(4, new Entity("four"));
        service.put(5, new Entity("five"));
        service.put(6, new Entity("six"));

        //Then
        assertEquals(2, cacheMap.size());
        assertEquals("five", service.get(5).getStr());
        assertEquals("six", service.get(6).getStr());
        assertFalse(cacheMap.containsKey(4));
    }

    @Test
    public void testEnvisionRemoving_ShouldWorkCorrectly() {
        //Given
        LfuCacheMap<Integer, Entity> cacheMap = new LfuCacheMap<Integer, Entity>(4, 0.1f, "log.txt");
        LfuCacheMap<Integer, Entity> cacheMapMaxEnvision = new LfuCacheMap<Integer, Entity>(4, 0.9f, "log.txt");

        //When
        cacheMap.put(1, new Entity("one"));
        cacheMap.put(2, new Entity("two"));
        cacheMap.put(3, new Entity("three"));
        cacheMap.put(4, new Entity("four"));
        cacheMap.put(5, new Entity("five"));

        cacheMapMaxEnvision.put(1, new Entity("one"));
        cacheMapMaxEnvision.put(2, new Entity("two"));
        cacheMapMaxEnvision.put(3, new Entity("three"));
        cacheMapMaxEnvision.put(4, new Entity("four"));
        cacheMapMaxEnvision.put(5, new Entity("five"));

        //Then
        assertEquals(4, cacheMap.size());
        assertEquals(1, cacheMapMaxEnvision.size());
    }

    @Test
    public void testPuttingConcurrency_ShouldWorkCorrectly() {
        //Given
        LfuCacheMap<Integer, Entity> cacheMap = new LfuCacheMap<Integer, Entity>(4, 0.1f, "log.txt");

        Runnable runnableTask = () -> {
            try {
                int i = 1;
                while (i < 5) {
                    cacheMap.put(i, new Entity("" + i));
                    TimeUnit.MILLISECONDS.sleep(300);
                    i++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Runnable runnableTask2 = () -> {
            try {
                int i = 1;
                while (i < 5) {
                    cacheMap.put(i + 10, new Entity("" + i));
                    TimeUnit.MILLISECONDS.sleep(300);
                    i++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread thread1 = new Thread(runnableTask);
        Thread thread2 = new Thread(runnableTask2);

        //When
        try {
            thread1.start();
            thread2.start();
            thread2.join(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Then
        System.out.println(cacheMap.size());
        assertFalse(cacheMap.containsKey(1));
        assertFalse(cacheMap.containsKey(11));
        assertEquals(4, cacheMap.size());
    }
}
