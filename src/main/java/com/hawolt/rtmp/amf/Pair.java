package com.hawolt.rtmp.amf;

import java.util.Objects;


/**
 * Key Value pair
 *
 * @param <K>
 * @param <V>
 * @author Hawolt
 */

public class Pair<K, V> {
    private final K k;
    private final V v;

    public static <K, V> Pair<K, V> from(K k, V v) {
        return new Pair<>(k, v);
    }

    private Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getKey() {
        return k;
    }

    public V getValue() {
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(k, pair.k) && Objects.equals(v, pair.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k, v);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "k=" + k +
                ", v=" + v +
                '}';
    }
}
