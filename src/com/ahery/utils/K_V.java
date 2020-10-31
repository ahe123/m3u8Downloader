package com.ahery.utils;

import java.io.Serializable;

public class K_V<T,S> implements Serializable {

    private static final long serialVersionUID = 3128389474253023269L;
    private T key;
    private S value;
    public K_V(T key, S value){
        this.key=key;
        this.value=value;
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public S getValue() {
        return value;
    }

    public void setValue(S value) {
        this.value = value;
    }
}
