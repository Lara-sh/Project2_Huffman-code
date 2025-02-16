package Application;

// Custom stack implementation for storing Node objects.
public class Stack {
    private int maxSize;       // Maximum number of elements the stack can hold.
    private Node[] stackArray; // Array to store stack elements.
    private int top;           // Index of the top element in the stack.

    // Constructor to initialize the stack with a given size.
    public Stack(int size) {
        maxSize = size;               // Set the stack's maximum capacity.
        stackArray = new Node[maxSize]; // Create an array to hold Node objects.
        top = -1;                     // Initialize the stack as empty.
    }

    // Pushes a Node onto the stack. Prints a warning if the stack is full.
    public void push(Node value) {
        if (isFull()) {
            System.out.println("Stack is full. Cannot push " + value);
        } else {
            stackArray[++top] = value; // Increment top and add the Node.
        }
    }

    // Removes and returns the top Node from the stack. Returns null if empty.
    public Node pop() {
        return isEmpty() ? null : stackArray[top--]; // Decrement top after returning the Node.
    }

    // Returns the top Node without removing it. Prints a message if empty.
    public Node peek() {
        if (isEmpty()) {
            System.out.println("Stack is empty. Cannot peek.");
            return null;
        }
        return stackArray[top]; // Return the top element.
    }

    // Checks if the stack is empty.
    public boolean isEmpty() {
        return top == -1; // True if no elements in the stack.
    }

    // Checks if the stack is full.
    public boolean isFull() {
        return top == maxSize - 1; // True if stack is at capacity.
    }
}
