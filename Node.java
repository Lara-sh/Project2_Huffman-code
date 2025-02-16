package Application;

public class Node {
	// The character associated with this node (null for non-leaf nodes in Huffman
	// tree)
	private Byte charc;

	// Frequency of the character in the data
	private int freq;

	// Pointers to the left and right child nodes in the Huffman tree
	private Node left, right;

	// The Huffman code for the character represented by this node
	private String hufman;

	// Length of the Huffman code (number of bits)
	private byte huffmanLength;

	// Constructor for creating a node with a frequency (used for internal nodes)
	public Node(int freq) {
		this.freq = freq;
	}

	// Constructor for creating a node with a character and its frequency (used for
	// leaf nodes)
	public Node(byte charc, int freq) {
		this.charc = charc;
		this.freq = freq;
	}

	// Getter for the character
	public Byte getCharc() {
		return charc;
	}

	// Setter for the character
	public void setCharc(Byte charc) {
		this.charc = charc;
	}

	// Getter for the frequency
	public int getFreq() {
		return freq;
	}

	// Setter for the frequency
	public void setFreq(int freq) {
		this.freq = freq;
	}

	// Getter for the left child
	public Node getLeft() {
		return left;
	}

	// Setter for the left child
	public void setLeft(Node left) {
		this.left = left;
	}

	// Getter for the right child
	public Node getRight() {
		return right;
	}

	// Setter for the right child
	public void setRight(Node right) {
		this.right = right;
	}

	// Getter for the Huffman code
	public String getHufman() {
		return hufman;
	}

	// Setter for the Huffman code
	public void setHufman(String hufman) {
		this.hufman = hufman;
	}

	// Getter for the length of the Huffman code
	public byte getHuffmanLength() {
		return huffmanLength;
	}

	// Setter for the length of the Huffman code
	public void setHuffmanLength(byte huffmanLength) {
		this.huffmanLength = huffmanLength;
	}

}
