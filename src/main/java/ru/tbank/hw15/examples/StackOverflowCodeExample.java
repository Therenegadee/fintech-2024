package ru.tbank.hw15.examples;

public class StackOverflowCodeExample {

    public static void main(String[] args) {
        printIncrementedNumbers(1);
    }

    public static int printIncrementedNumbers(int startNumber) {
        System.out.println(startNumber);
        return startNumber + printIncrementedNumbers(startNumber + 1);
    }
}
