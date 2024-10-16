package ru.tbank.hw5.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

@Slf4j
public class ThreadUtils {

    public static void acquireSemaphoreSafely(Semaphore semaphore) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.error("В процессе выполнения метода acquire() у Semaphore было выброшено исключение {}.",
                    e.getClass().getSimpleName());
            Thread.currentThread().interrupt();
        }
    }
}
