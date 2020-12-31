package com.evancolewright.factionsscoreboard.utils;

import java.util.*;

/**
 * A class for sorting Map implementations.
 * Updated to Java8 lambda's (mostly) :D
 *
 * @param <K> The key
 * @param <T> The value (Must be comparable)
 */

public class MapSorter<K, T extends Comparable<T>>
{
    private final Map<K, T> map;
    private Order order;

    public MapSorter(Map<K, T> map)
    {
        this.map = map;
        this.order = Order.GREATEST_TO_LEAST;
    }

    public Map<K, T> getSortedMap()
    {
        final List<Map.Entry<K, T>> entries = new LinkedList<>(this.map.entrySet());
        entries.sort(this.comp());
        final Map<K, T> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, T> entry : entries)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private Comparator<Map.Entry<K, T>> comp()
    {
        return (o1, o2) ->
        {
            if (order == Order.GREATEST_TO_LEAST)
            {
                return o2.getValue().compareTo(o1.getValue());
            }
            return o1.getValue().compareTo(o2.getValue());
        };
    }

    public enum Order
    {
        GREATEST_TO_LEAST,
        LEAST_TO_GREATEST;
    }
}
