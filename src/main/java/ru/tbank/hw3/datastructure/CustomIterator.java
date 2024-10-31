package ru.tbank.hw3.datastructure;

import java.util.function.Consumer;

public interface CustomIterator<T> {
    boolean hasNext();
    T next();
    void forEachRemaining(Consumer<? super T> action);
}
