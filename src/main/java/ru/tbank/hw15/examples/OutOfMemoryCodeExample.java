package ru.tbank.hw15.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OutOfMemoryCodeExample {
    public static void main(String[] args) {
        Map<Integer, String> map = new HashMap<>();
        Random random = new Random();

        while (true) {
            map.put(random.nextInt(), "randomValue");
        }
    }
}
