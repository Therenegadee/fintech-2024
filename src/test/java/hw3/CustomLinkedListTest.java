package hw3;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import ru.tbank.datastructure.CustomLinkedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Testable
public class CustomLinkedListTest {

    @Test
    public void testAddElement() {
        // Given
        CustomLinkedList<Integer> linkedList = new CustomLinkedList<>();
        Integer number = 1;
        linkedList.add(number);

        // When
        boolean result = linkedList.contains(1);

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    public void testNotContainsElement() {
        // Given
        CustomLinkedList<Integer> linkedList = new CustomLinkedList<>();
        linkedList.add(1);

        // When
        boolean result = linkedList.contains(2);

        // Then
        Assertions.assertFalse(result);
    }

    @Test
    public void testAddAll() {
        // Given
        CustomLinkedList<Integer> linkedList = new CustomLinkedList<>();
        List<Integer> numbers = new ArrayList<>(IntStream.range(0, 50).boxed().toList());
        linkedList.addAll(numbers);
        Collections.shuffle(numbers);

        // When
        boolean result = true;
        for (Integer number : numbers) {
            if (!linkedList.contains(number)) {
                result = false;
                break;
            }
        }

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    public void testGetElement() {
        // Given
        CustomLinkedList<Integer> linkedList = new CustomLinkedList<>();
        linkedList.addAll(IntStream.range(0, 50).boxed().toList());
        linkedList.addAll(IntStream.range(52, 100).boxed().toList());
        Integer numberToSeek = 51;
        linkedList.add(numberToSeek);

        // When
        boolean result = linkedList.contains(numberToSeek);

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    public void testRemoveElement() {
        // Given
        CustomLinkedList<Integer> linkedList = new CustomLinkedList<>();
        int lowerBound = 0;
        int upperBound = 50;
        linkedList.addAll(IntStream.range(lowerBound, upperBound).boxed().toList());
        Integer numberToSeek = new Random().nextInt(lowerBound, upperBound);

        // When
        boolean containsBeforeRemove = linkedList.contains(numberToSeek);
        linkedList.remove(numberToSeek);
        boolean containsAfterRemove = linkedList.contains(numberToSeek);

        // Then
        Assertions.assertTrue(containsBeforeRemove);
        Assertions.assertFalse(containsAfterRemove);
    }

    @Test
    public void testReduceCollectionIntoList() {
        // Given
        List<Integer> numbers = IntStream.range(0, 100).boxed().toList();

        // When
        CustomLinkedList<Integer> linkedList = numbers.stream()
                .reduce(new CustomLinkedList<>(),
                        (acc, element) -> {
                            acc.add(element);
                            return acc;
                        },
                        (list1, list2) -> {
                            list1.addAll(list2);
                            return list1;
                        }
                );

        // Then
        Assertions.assertEquals(numbers.size(), linkedList.size());
    }
}
