package manager;

import java.util.ArrayList;
import java.util.List;


public class CustomLinkedList<T> {
    private final Node<T> root;
    private int size;
    private final int capacity;

    public CustomLinkedList(int capacity) {
        this.root = new Node<>(null);
        this.root.left = root;
        this.root.right = root;
        this.size = 0;
        this.capacity = capacity;
    }

    public Node<T> linkLast(T value) {
        if (size >= capacity) {
            removeNode(root.left);
        }
        Node<T> first = root.right;
        Node<T> newNode = new Node<>(value);
        root.right = newNode;
        newNode.left = root;
        newNode.right = first;
        first.left = newNode;
        size++;
        return newNode;
    }

    public List<T> getList() {
        List<T> ans = new ArrayList<>();
        Node<T> ptr = root.right;
        while (ptr != root) {
            ans.add(ptr.value);
            ptr = ptr.right;
        }
        return ans;
    }

    public void removeNode(Node<T> node) {
        node.left.right = node.right;
        node.right.left = node.left;
        this.size--;
    }

    public static class Node<T> {
        public T value;
        public Node<T> left;
        public Node<T> right;

        public Node(T value) {
            this.value = value;
        }
    }
}
