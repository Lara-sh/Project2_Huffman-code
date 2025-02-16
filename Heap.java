package Application;

// A custom implementation of a Min-Heap for storing Node objects based on their frequency.
class Heap {
    private Node[] Heap; // Array to hold the elements of the heap.
    private int size;    // Current number of elements in the heap.
    private int maxSize; // Maximum allowed size of the heap.

    // Constructor to initialize the heap with a given maximum size.
    public Heap(int maxSize) {
        this.maxSize = maxSize;       // Set the maximum capacity of the heap.
        this.size = 0;               // Initially, the heap is empty.
        Heap = new Node[maxSize + 1]; // Allocate memory for the heap array.
        Node dummy = new Node(0);    // Create a dummy node with minimal frequency as a sentinel.
        Heap[0] = dummy;             // Place the dummy node at index 0.
    }

    // Maintains the min-heap property by adjusting elements from the given position downward.
    private void minHeapify(int pos) {
        if (!isLeaf(pos)) { // Check if the current node is not a leaf node.
            boolean hasLeftChild = leftChild(pos) <= size && Heap[leftChild(pos)] != null;
            boolean hasRightChild = rightChild(pos) <= size && Heap[rightChild(pos)] != null;

            if (hasLeftChild) {
                int smallestChildPos = leftChild(pos);
                // Check if the right child exists and is smaller than the left child.
                if (hasRightChild && Heap[rightChild(pos)].getFreq() < Heap[leftChild(pos)].getFreq()) {
                    smallestChildPos = rightChild(pos);
                }
                // If the current node is greater than the smaller child, swap them and continue.
                if (Heap[pos].getFreq() > Heap[smallestChildPos].getFreq()) {
                    swap(pos, smallestChildPos);
                    minHeapify(smallestChildPos); // Recursively heapify the affected subtree.
                }
            }
        }
    }

    // Inserts a new Node into the heap while maintaining the heap property.
    public void insert(Node element) {
        if (size >= maxSize) {
            return; // Stop if the heap is already full.
        }

        Heap[++size] = element; // Add the element at the end of the heap.
        int current = size;
        // Adjust the position of the new element by comparing it with its parent.
        while (Heap[current].getFreq() < Heap[parent(current)].getFreq()) {
            swap(current, parent(current)); // Swap if the new element is smaller than its parent.
            current = parent(current);     // Move up the heap.
        }
    }

    // Removes and returns the smallest (root) element from the heap.
    public Node remove() {
        Node popped = Heap[1];      // The root node is the smallest element in a min-heap.
        Heap[1] = Heap[size--];     // Replace the root with the last element and decrease the size.
        minHeapify(1);              // Restore the heap property starting from the root.
        return popped;              // Return the removed element.
    }

    // Getter to retrieve the current size of the heap.
    public int getSize() {
        return size;
    }

    // Helper function to calculate the index of a node's parent.
    private int parent(int pos) {
        return pos / 2;
    }

    // Helper function to calculate the index of a node's left child.
    private int leftChild(int pos) {
        return 2 * pos;
    }

    // Helper function to calculate the index of a node's right child.
    private int rightChild(int pos) {
        return (2 * pos) + 1;
    }

    // Helper function to check if a node is a leaf (it has no children).
    private boolean isLeaf(int pos) {
        // A node is a leaf if it's in the bottom half of the heap and has no children.
        return pos > (size / 2) && pos <= size;
    }

    // Helper function to swap two nodes in the heap.
    private void swap(int fpos, int spos) {
        Node tmp = Heap[fpos]; // Temporarily store one node.
        Heap[fpos] = Heap[spos]; // Replace it with the other node.
        Heap[spos] = tmp;        // Complete the swap.
    }
}
