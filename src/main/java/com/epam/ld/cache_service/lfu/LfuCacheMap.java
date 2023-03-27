package com.epam.ld.cache_service.lfu;

import com.epam.ld.cache_service.CacheListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LfuCacheMap<K, V> implements Map<K, V> {

    private CacheListener cacheListener;
    private final Map<K, CacheNode<K, V>> cache;
    private final LinkedHashSet[] frequencyList;
    private int lowestFrequency;
    private final int maxFrequency;
    private final int maxCacheSize;
    private final float evictionFactor;

    private static int evictionCount = 0;
    private static long averagePuttingTime = 0;

    public LfuCacheMap(int maxCacheSize, float evictionFactor) {
        if (maxCacheSize <= 0 || maxCacheSize > 100000) {
            throw new IllegalArgumentException("Max cache size must be between 0 and 100 000 or equal");
        }
        if (evictionFactor <= 0 || evictionFactor >= 1) {
            throw new IllegalArgumentException("Eviction factor must be between 0 and 1");
        }
        this.cacheListener = new CacheListener();
        this.cache = new HashMap<K, CacheNode<K, V>>(maxCacheSize);
        this.frequencyList = new LinkedHashSet[maxCacheSize];
        this.lowestFrequency = 0;
        this.maxFrequency = maxCacheSize - 1;
        this.maxCacheSize = maxCacheSize;
        this.evictionFactor = evictionFactor;
        initFrequencyList();
    }

    public LfuCacheMap(int maxCacheSize, float evictionFactor, String logFileName) {
        this(maxCacheSize, evictionFactor);
        this.cacheListener = new CacheListener(logFileName);
    }

    public synchronized V put(K k, V v) {
        long timeBegin = System.nanoTime();
        V oldValue = null;
        CacheNode<K, V> currentNode = cache.get(k);
        if (currentNode == null) {
            if (cache.size() == maxCacheSize) {
                doEviction();
            }
            LinkedHashSet<CacheNode<K, V>> nodes = frequencyList[0];
            currentNode = new CacheNode(k, v, 0);
            nodes.add(currentNode);
            cache.put(k, currentNode);
            lowestFrequency = 0;
            long timeEnd = System.nanoTime();
            calculateAveragePuttingTime(timeBegin, timeEnd);
        } else {
            oldValue = currentNode.v;
            currentNode.v = v;
        }
        return oldValue;
    }

    public synchronized V get(Object k) {
        CacheNode<K, V> currentNode = cache.get(k);
        if (currentNode != null) {
            int currentFrequency = currentNode.frequency;
            if (currentFrequency < maxFrequency) {
                int nextFrequency = currentFrequency + 1;
                LinkedHashSet<CacheNode<K, V>> currentNodes = frequencyList[currentFrequency];
                LinkedHashSet<CacheNode<K, V>> newNodes = frequencyList[nextFrequency];
                moveToNextFrequency(currentNode, nextFrequency, currentNodes, newNodes);
                cache.put((K) k, currentNode);
                if (lowestFrequency == currentFrequency && currentNodes.isEmpty()) {
                    lowestFrequency = nextFrequency;
                }
            } else {
                //Recently accessed put ahead of others
                LinkedHashSet<CacheNode<K, V>> nodes = frequencyList[currentFrequency];
                nodes.remove(currentNode);
                nodes.add(currentNode);
            }
            return currentNode.v;
        } else {
            return null;
        }
    }

    public V remove(Object k) {
        CacheNode<K, V> currentNode = cache.remove(k);
        if (currentNode != null) {
            LinkedHashSet<CacheNode<K, V>> nodes = frequencyList[currentNode.frequency];
            nodes.remove(currentNode);
            if (lowestFrequency == currentNode.frequency) {
                findNextLowestFrequency();
            }
            cacheListener.writeLog("The element with key =" + currentNode.k + " and value =" + currentNode.v + " was removed.");
            return currentNode.v;
        } else {
            return null;
        }
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> me : m.entrySet()) {
            put(me.getKey(), me.getValue());
        }
    }

    public int frequencyOf(K k) {
        CacheNode<K, V> node = cache.get(k);
        if (node != null) {
            return node.frequency + 1;
        } else {
            return 0;
        }
    }

    public void clear() {
        for (int i = 0; i <= maxFrequency; i++) {
            frequencyList[i].clear();
        }
        cache.clear();
        lowestFrequency = 0;
    }

    public Set<K> keySet() {
        return this.cache.keySet();
    }

    public Collection<V> values() {
        return (Collection<V>) cache.values();
    }

    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new LinkedHashSet<>();
        for (Entry<K, CacheNode<K, V>> entry : cache.entrySet()) {
            entrySet.add((Entry<K, V>) entry);
        }
        return entrySet;
    }

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    public boolean containsKey(Object o) {
        return this.cache.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return this.cache.containsValue(o);
    }


    private void initFrequencyList() {
        for (int i = 0; i <= maxFrequency; i++) {
            frequencyList[i] = new LinkedHashSet<CacheNode<K, V>>();
        }
    }

    private void calculateAveragePuttingTime(long begin, long end){
        long time = end - begin;
        if(averagePuttingTime == 0){
            averagePuttingTime = time;
        }
        else{
            averagePuttingTime = (averagePuttingTime + time)/2;
        }
    }

    public String getStatisticForUser() {
        String message = "Number of cache evictions = " + evictionCount +
                "; Average time spent for putting new values into the cache = " + averagePuttingTime + " nano seconds";
        cacheListener.writeLog(message);
        return message;
    }

    private void doEviction() {
        int currentlyDeleted = 0;
        float target = maxCacheSize * evictionFactor;
        while (currentlyDeleted < target) {
            LinkedHashSet<CacheNode<K, V>> nodes = frequencyList[lowestFrequency];
            if (nodes.isEmpty()) {
                throw new IllegalStateException("Lowest frequency constraint violated!");
            } else {
                Iterator<CacheNode<K, V>> it = nodes.iterator();
                while (it.hasNext() && currentlyDeleted++ < target) {
                    CacheNode<K, V> node = it.next();
                    it.remove();
                    cache.remove(node.k);
                    cacheListener.writeLog("The element with key =" + node.k + " and value =" + node.v + " was removed.");
                    evictionCount++;
                }
                if (!it.hasNext()) {
                    findNextLowestFrequency();
                }
            }
        }
    }

    private void moveToNextFrequency(CacheNode<K, V> currentNode, int nextFrequency, LinkedHashSet<CacheNode<K, V>> currentNodes, LinkedHashSet<CacheNode<K, V>> newNodes) {
        currentNodes.remove(currentNode);
        newNodes.add(currentNode);
        currentNode.frequency = nextFrequency;
    }

    private void findNextLowestFrequency() {
        while (lowestFrequency <= maxFrequency && frequencyList[lowestFrequency].isEmpty()) {
            lowestFrequency++;
        }
        if (lowestFrequency > maxFrequency) {
            lowestFrequency = 0;
        }
    }

    private static class CacheNode<Key, Value> {

        public final Key k;
        public Value v;
        public int frequency;

        public CacheNode(Key k, Value v, int frequency) {
            this.k = k;
            this.v = v;
            this.frequency = frequency;
        }

    }
}
