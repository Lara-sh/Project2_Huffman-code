package Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DecompressClass extends Scene {

    // Class attributes
	int[] freqn = new int[256]; // Array to store character frequencies
	Node[] nod = new Node[256]; // Array to store nodes for each character
	BorderPane borderP = new BorderPane(); // Layout container for FX
	Stage stage; // The main window where scenes are displayed
	Scene scene; // The previous scene to allow navigation back
	Scene scene1; // The current or alternate scene
	File file; // Input file to be compressed
	String fileName; // Name of the input file
	String FileNOut; // Name of the output file after processing
	Heap heap; // Min-heap used to build the Huffman tree
	byte Leafs; // Number of leaf nodes in the Huffman tree
	String fHeader; // Encoded header information for the file
	Node rootNode; // Root node of the Huffman tree
	long lengBefore; // Size of the file before compression
	long lengAfter; // Size of the file after compression
	byte extLength; // Length of the file extension
	String extString; // File extension as a string
	int headerLength; // Length of the header in bits
	String header; // Binary string representing the header data

   //Decompress class
    public DecompressClass(Stage stage, Scene scene, File file) {
        super(new BorderPane(), 1200, 600);
        this.stage = stage;
        this.scene = scene;

        this.borderP = ((BorderPane) this.getRoot());

        this.file = file;

        this.lengBefore = this.file.length();

        getHeader();
        
    }
    
    
    private void getHeader() {
        // Buffers for reading the input file
        byte[] bufferIn = new byte[8];
        byte[] extBuffer;

        try (FileInputStream inputStream = new FileInputStream(file)) {

            // Read the length of the file extension and the extension itself
            this.extLength = (byte) inputStream.read();
            extBuffer = new byte[this.extLength];
            byte length = (byte) inputStream.read(extBuffer);

            // Convert the extension bytes to a string
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append((char) extBuffer[i]);
            }
            this.extString = builder.toString();

            // Read the header length (stored in 4 bytes)
            inputStream.read(bufferIn, 0, 4);
            this.headerLength = bufferIn[3] & 0xFF |
                                (bufferIn[2] & 0xFF) << 8 |
                                (bufferIn[1] & 0xFF) << 16 |
                                (bufferIn[0] & 0xFF) << 24;

            // Initialize variables for reading the header and serialized data
            int bytesRead;
            int numberOfBytesForHeaderCounter = 0;
            int numberOfBytesForHeader = (this.headerLength % 8 == 0) ? this.headerLength / 8 : (this.headerLength / 8) + 1;
            StringBuilder header = new StringBuilder();
            StringBuilder serialData = new StringBuilder();

            // Read the rest of the file, separating header bits and serialized data
            while ((bytesRead = inputStream.read(bufferIn)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (numberOfBytesForHeaderCounter < numberOfBytesForHeader) {
                        header.append(convertByteToBinary(bufferIn[i]));
                        numberOfBytesForHeaderCounter++;
                    } else {
                        serialData.append(convertByteToBinary(bufferIn[i]));
                    }
                }
            }

            this.header = header.toString();
            inputStream.close();

            // Reconstruct the Huffman tree from the header
            Stack stack = new Stack(256);
            int counter = 0;
            while (counter < this.headerLength) {
                if (header.charAt(counter) == '1') {
                    counter++;
                    stack.push(new Node((byte) Integer.parseInt(header.substring(counter, counter + 8), 2), 0));
                    counter += 8;
                } else {
                    counter++;
                    Node node = new Node(0);
                    node.setRight(stack.pop());
                    node.setLeft(stack.pop());
                    stack.push(node);
                }
            }

            this.rootNode = stack.peek();

            // Generate Huffman codes based on the reconstructed tree
            generateHuffmanCodes(this.rootNode, "", (byte) 0);

            // Reconstruct the original file name using the extracted extension
            String[] nameInfo = file.getName().split("\\.");
            this.fileName = nameInfo[0] + "." + extString;

            // Ensure the output file name is unique
            StringBuilder outFileName = new StringBuilder(fileName);
            getUniquName(outFileName);
            File outFile = new File(outFileName.toString());

            // Write the decompressed data to the output file
            FileOutputStream out = new FileOutputStream(outFile);
            int startIndex = serialData.length() - 8;
            int addedBits = Integer.parseInt(serialData.substring(startIndex), 2);
            serialData.delete(startIndex - addedBits, serialData.length());

            byte[] bufferOut = new byte[8];
            int counterForBufferSerialData = 0, counterForBufferOut = 0;

            while (counterForBufferSerialData < serialData.length()) {
                Node curr = rootNode;

                // Traverse the Huffman tree to decode the next byte
                while (curr != null && counterForBufferSerialData < serialData.length()) {
                    if (serialData.charAt(counterForBufferSerialData) == '0' && curr.getLeft() != null) {
                        curr = curr.getLeft();
                    } else if (curr.getRight() != null) {
                        curr = curr.getRight();
                    } else if (rootNode.getLeft() == null && rootNode.getRight() == null) {
                        counterForBufferSerialData++;
                        break;
                    } else {
                        break;
                    }

                    counterForBufferSerialData++;
                }

                // Write the decoded byte to the output buffer
                bufferOut[counterForBufferOut++] = curr.getCharc();
                if (counterForBufferOut == 8) {
                    out.write(bufferOut);
                    counterForBufferOut = 0;
                }
            }

            // Write any remaining bytes to the file
            if (counterForBufferOut > 0) {
                out.write(bufferOut, 0, counterForBufferOut);
            }

            out.close();
            lengAfter = outFile.length();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void generateHuffmanCodes(Node node, String code, byte length) {
        if (node != null) {
            if (node.getLeft() == null && node.getRight() == null) { // Check if the current node is a leaf node
                // Assign the generated Huffman code and its length to the leaf node
                node.setHufman(code);
                node.setHuffmanLength(length);

                // Retrieve the character code associated with the node
                Byte charCode = node.getCharc();

                // Handle negative byte values by mapping them to the appropriate index
                if (charCode < 0) {
                    nod[node.getCharc() + 256] = node; // Adjust index for negative byte values
                } else {
                    nod[node.getCharc()] = node; // Use the character code as the index for positive values
                }
            } else {
                // Recursively generate Huffman codes for the left child, appending "0" to the current code
                generateHuffmanCodes(node.getLeft(), code + "0", (byte) (length + 1));
                // Recursively generate Huffman codes for the right child, appending "1" to the current code
                generateHuffmanCodes(node.getRight(), code + "1", (byte) (length + 1));
            }
        }
    }


	public StringBuilder getHeader(Node root) { // post order
		StringBuilder builder = new StringBuilder();
		getHelper(root, builder);
		return builder;
	}

	private void getHelper(Node node, StringBuilder builder) {
		if (node == null)
			return;
		getHelper(node.getLeft(), builder);
		getHelper(node.getRight(), builder);

		if (node.getLeft() == null && node.getRight() == null) {
			byte charCode = node.getCharc();
			builder.append("1" + convertByteToBinary(charCode));
		} else {
			builder.append("0");
		}
	}
	
    
	public static String convertByteToBinary(byte b) {
		StringBuilder binaryString = new StringBuilder();
		for (int i = 7; i >= 0; i--) {
			int bit = (b >> i) & 1;
			binaryString.append(bit);
		}
		return binaryString.toString();
	}

	public void getUniquName(StringBuilder fileName) {
		// Create a File object based on the input file name.
		File file = new File(fileName.toString());
		// Initialize a counter and a flag for the while loop.
		int number = 1, flag = 0;
		// Loop to check if the file exists and modify the file name accordingly.
		while (file.exists()) {
			int lastDotIndex;
			if (flag == 0) {
				// Find the last dot (.) position to locate the extension.
				lastDotIndex = fileName.lastIndexOf(".");
				// Insert a number before the extension for the first time.
				fileName.insert(lastDotIndex, "(" + (number++) + ")");
			} else {
				// For subsequent iterations, remove the old number and add a new one.
				int startIndex = fileName.lastIndexOf("(");
				int endIndex = fileName.lastIndexOf(")") + 1;
				fileName.delete(startIndex, endIndex);
				lastDotIndex = fileName.lastIndexOf(".");
				fileName.insert(lastDotIndex, "(" + (number++) + ")");
			}
			// Update the file object with the new file name.
			file = new File(fileName.toString());
			// Set flag to 1 to indicate that the file name has been modified at least once.
			flag = 1;
		}
	}
}
