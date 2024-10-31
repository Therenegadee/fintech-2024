package ru.tbank.datastructure;

import lombok.Getter;
import lombok.Setter;
import ru.tbank.hw3.datastructure.CustomIterator;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CustomLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public CustomLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    public void add(T element) {
        Node<T> newNode = new Node<>(element);
        Node<T> prev = tail;
        tail = newNode;
        if (Objects.isNull(prev)) {
            head = newNode;
        } else {
            prev.setNext(newNode);
            newNode.setPrevious(prev);
        }
        size++;
    }

    public void remove(T element) {
        if (head == null) {
            return;
        }
        if (head.getElement().equals(element)) {
            head = head.getNext();
            if (head != null) {
                head.setPrevious(null);
            }
            size--;
            return;
        }

        Node<T> current = head;
        while (current != null && !current.getElement().equals(element)) {
            current = current.getNext();
        }

        if (current != null) {
            Node<T> prev = current.getPrevious();
            Node<T> next = current.getNext();
            if (prev != null) {
                prev.setNext(next);
            }
            if (next != null) {
                next.setPrevious(prev);
            }
            if (current == tail) {
                tail = prev;
            }
            size--;
        }
    }

    public void addAll(List<T> elementsList) {
        for (T element : elementsList) {
            add(element);
        }
    }

    public void addAll(CustomLinkedList<T> elementsList) {
        Node<T> currentElement = elementsList.head;
        if (Objects.isNull(currentElement)) {
            return;
        }
        while (Objects.nonNull(currentElement.getNext())) {
            add(currentElement.getElement());
            currentElement = currentElement.getNext();
        }
    }

    public boolean contains(T element) {
        Node<T> current = head;
        while (current != null) {
            if (current.getElement().equals(element)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    public int size() {
        return size;
    }

    public CustomIterator<T> iterator() {
        return new CustomLinkedListIterator();
    }

    private class CustomLinkedListIterator implements CustomIterator<T> {
        private Node<T> current = head;
        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            T element = current.element;
            current = current.next;
            return element;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (hasNext()) {
                action.accept(next());
            }
        }
    }

    @Getter
    @Setter
    static class Node<T> {
        private T element;
        private Node<T> next;
        private Node<T> previous;

        Node(T element) {
            this.element = element;
            this.next = null;
            this.previous = null;
        }

        Node(T element, Node<T> previous, Node<T> next) {
            this.element = element;
            this.previous = previous;
            this.next = next;
        }
    }
}
