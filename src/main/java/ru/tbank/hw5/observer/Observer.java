package ru.tbank.hw5.observer;

public interface Observer<T> {
    void update(T entity);
}
