package com.epamld.cache_service.lru_tests;

import com.epam.ld.cache_service.Entity;
import com.epam.ld.cache_service.lru.LruGuavaCacheService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LruGuavaCacheServiceTest {

    @Test
    public void guavaPuttingTest_ShouldWorkCorrectly() {
        //Given
        LruGuavaCacheService cacheService = new LruGuavaCacheService();

        //When
        for(int i = 1; i<=100001; i++){
            cacheService.put(i, new Entity(""+ i));
        }

        //Then
        assertNull(cacheService.get(1));
        assertEquals(new Entity("2"), cacheService.get(2));
        assertEquals(new Entity("100001"), cacheService.get(100001));
    }

    @Test
    public void testingTimeBasedRemoval_ShouldWorkCorrectly() throws InterruptedException {
        //Given
        LruGuavaCacheService cacheService = new LruGuavaCacheService();

        //When
        cacheService.put(1, new Entity("one"));
        Thread.sleep(5000);

        //Then
        assertNull(cacheService.get(1));
    }
}
