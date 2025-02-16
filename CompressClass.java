package Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
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

public class CompressClass extends Scene {

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

	// Class compress
	public CompressClass(Stage stage, Scene scene, File file) {
		super(new BorderPane(), 1200, 600);
		this.stage = stage;
		this.scene = scene;

		this.borderP = ((BorderPane) this.getRoot());

		this.file = file;

		this.lengBefore = this.file.length();

		calculateFreq();
		MinHeap();
		rootNode = heap.remove();
		if (rootNode.getLeft() == null && rootNode.getRight() == null) {
			rootNode.setHufman("1");
			rootNode.setHuffmanLength((byte) 1);
			Byte charCode = rootNode.getCharc();
			if (charCode < 0)
				nod[rootNode.getCharc() + 256] = rootNode;
			else
				nod[rootNode.getCharc()] = rootNode;

		} else
			assignHuffman(rootNode, "", (byte) 0);

		this.fHeader = createHeader();

		writeToFile();

		SceneMethod();
	}

	/*
	 * Reads the input file and calculates the frequency of each byte. and This
	 * information is used for building the Huffman tree.
	 */
	private void calculateFreq() {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			byte[] buffer = new byte[8]; // Temporary buffer to read chunks of the file
			int bytesRead;

			// Read the file in chunks until the end
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				// Process each byte in the current chunk
				for (int i = 0; i < bytesRead; i++) {
					if (buffer[i] < 0) {
						// Adjust for negative byte values (unsigned byte)
						freqn[buffer[i] + 256]++;
					} else {
						// Count the occurrence of the byte
						freqn[buffer[i]]++;
					}
				}
			}
		} catch (Exception e) {
			// Print an error message if something goes wrong
			e.printStackTrace();
		}
	}

	/*
	 * Initializes the heap with nodes based on character frequencies and constructs
	 * the Huffman tree from these nodes.
	 */
	private void MinHeap() {
		heap = new Heap(256); // Create a heap with capacity for 256 nodes

		// Add a node to the heap for each character with a non-zero frequency
		for (int i = 0; i < freqn.length; i++) {
			if (freqn[i] != 0) {
				Node node = new Node((byte) i, freqn[i]); // Create a node for the character
				heap.insert(node); // Insert the node into the heap
			}
		}

		Leafs = (byte) heap.getSize(); // Store the number of leaf nodes

		// If there is only one unique character, no need to build the tree further
		if (Leafs == 1)
			return;

		// Build the Huffman tree by combining the two smallest nodes repeatedly
		while (heap.getSize() != 1) {
			Node node1 = heap.remove(); // Remove the node with the smallest frequency
			Node node2 = heap.remove(); // Remove the next smallest node

			// Create a new parent node with the combined frequency of the two nodes
			Node node = new Node(node1.getFreq() + node2.getFreq());
			node.setLeft(node1); // Set the first node as the left child
			node.setRight(node2); // Set the second node as the right child

			heap.insert(node); // Insert the new parent node back into the heap
		}
	}

	/*
	 * Recursively generates Huffman codes for each leaf node in the Huffman tree.
	 * These codes are used for encoding the input file.
	 */
	public void assignHuffman(Node node, String code, byte length) {
		if (node != null) {
			// If the node is a leaf (it represents a character)
			if (node.getLeft() == null && node.getRight() == null) {
				node.setHufman(code); // Assign the generated Huffman code to the node
				node.setHuffmanLength(length); // Store the length of the Huffman code

				Byte charCode = node.getCharc(); // Get the character represented by this node
				if (charCode < 0) {
					// Handle negative byte values (unsigned byte adjustment)
					nod[node.getCharc() + 256] = node;
				} else {
					nod[node.getCharc()] = node; // Store the node in the array
				}
			} else {
				// Traverse the left subtree, appending "0" to the code
				assignHuffman(node.getLeft(), code + "0", (byte) (length + 1));
				// Traverse the right subtree, appending "1" to the code
				assignHuffman(node.getRight(), code + "1", (byte) (length + 1));
			}
		}
	}

	// The header includes the file extension, its length, and the structure of the
	// Huffman tree.
	private String createHeader() {
		StringBuilder header = new StringBuilder();

		// Split the file name to extract the name and extension
		String[] name1 = file.getName().split("\\.");
		String extention = name1[1]; // File extension
		this.extString = extention;
		byte extLength = (byte) extention.length(); // Length of the file extension
		this.extLength = extLength;
		String fileName = name1[0]; // Base name of the file
		this.fileName = fileName;

		// Add the length of the file extension and the extension itself to the header
		header.append(convertByteToBinaryString(extLength));
		for (int i = 0; i < extLength; i++) {
			header.append(convertByteToBinaryString((byte) extention.charAt(i)));
		}

		// Build the Huffman tree representation
		StringBuilder treeStructure = buildHuffmanTreeHeader(rootNode);
		this.headerLength = treeStructure.length();

		// Ensure the header length is a multiple of 8 by padding with zeros
		if (headerLength % 8 != 0) {
			for (int i = 0; i < 8 - headerLength % 8; i++) {
				treeStructure.append("0");
			}
		}

		this.header = treeStructure.toString();

		// Add the header length in 4 bytes to the header
		header.append(convertByteToBinaryString((byte) (headerLength >> 24)));
		header.append(convertByteToBinaryString((byte) (headerLength >> 16)));
		header.append(convertByteToBinaryString((byte) (headerLength >> 8)));
		header.append(convertByteToBinaryString((byte) (headerLength)));
		header.append(this.header);

		return header.toString();
	}

	// Builds a binary representation of the Huffman tree using post-order
	// traversal.
	public StringBuilder buildHuffmanTreeHeader(Node root) {
		StringBuilder builder = new StringBuilder();
		traverseTreePostOrder(root, builder);
		return builder;
	}

	// Traverses the Huffman tree in post-order and appends its structure to the
	// builder.
	private void traverseTreePostOrder(Node node, StringBuilder builder) {
		if (node == null) {
			return;
		}

		// Traverse the left and right subtrees
		traverseTreePostOrder(node.getLeft(), builder);
		traverseTreePostOrder(node.getRight(), builder);

		if (node.getLeft() == null && node.getRight() == null) {
			// Leaf node: append '1' followed by the binary representation of the character
			byte charCode = node.getCharc();
			builder.append("1").append(convertByteToBinaryString(charCode));
		} else {
			// Internal node: append '0'
			builder.append("0");
		}
	}

	// Converts a byte into its 8-bit binary string representation.
	public static String convertByteToBinaryString(byte byt) {
		StringBuilder binaryString = new StringBuilder();

		// Iterate through each bit in the byte (from most significant to least
		// significant)
		for (int i = 7; i >= 0; i--) {
			int bit = (byt >> i) & 1; // Extract the bit at position 'i'
			binaryString.append(bit); // Append the bit to the binary string
		}

		return binaryString.toString(); // Return the binary representation as a string
	}

	// Writes the compressed data, including the header and the Huffman-encoded
	private void writeToFile() {
		// Create output filename with ".huff" extension and ensure it's unique
		StringBuilder outFileName = new StringBuilder(fileName + ".huff");
		UniqueFile(outFileName);
		File outFile = new File(outFileName.toString());

		this.FileNOut = outFileName.toString();

		try (FileOutputStream out = new FileOutputStream(outFile)) {
			byte[] bufferIn = new byte[8]; // Buffer to read the original file in chunks
			byte[] bufferOut = new byte[8]; // Buffer to write compressed data in chunks
			byte[] headerBytes = new byte[8]; // Buffer to store header information

			int byteNumber = 0;

			// Convert the header binary string into bytes and write to the output file
			for (byteNumber = 0; byteNumber < fHeader.length() / 8; byteNumber++) {
				String byteString = fHeader.substring(8 * byteNumber, 8 * byteNumber + 8);
				headerBytes[byteNumber % 8] = (byte) Integer.parseInt(byteString, 2);

				// Write every 8 bytes from the header buffer to the file
				if ((byteNumber + 1) % 8 == 0)
					out.write(headerBytes);
			}

			// Write remaining header bytes if any
			if (byteNumber % 8 != 0)
				out.write(headerBytes, 0, byteNumber % 8);

			// Open input stream to process the original file's data
			try (FileInputStream inputStream = new FileInputStream(file)) {
				int bytesRead;
				StringBuilder builder = new StringBuilder();

				// Read original file byte by byte, compressing and appending Huffman codes
				while ((bytesRead = inputStream.read(bufferIn)) != -1) {
					for (int i = 0; i < bytesRead; i++) {
						byte currByte = bufferIn[i];
						// Retrieve and append Huffman code for the current byte
						if (currByte < 0)
							builder.append(nod[currByte + 256].getHufman());
						else
							builder.append(nod[currByte].getHufman());

						// Write compressed data in 64-bit chunks
						if (builder.length() >= 64) {
							for (int j = 0; j < 8; j++) {
								bufferOut[j] = (byte) Integer.parseInt(builder.substring(0, 8), 2);
								builder.delete(0, 8);
							}
							out.write(bufferOut);
						}
					}
				}

				// Write any remaining compressed bits to the output file
				byte length = (byte) builder.length();
				for (int i = 0; i < length / 8; i++) {
					bufferOut[i] = (byte) Integer.parseInt(builder.substring(0, 8), 2);
					builder.delete(0, 8);
				}
				out.write(bufferOut, 0, length / 8);

				// Handle remaining bits by padding with zeros and write to the output file
				byte lengthOfRemainingBits = (byte) builder.length();
				byte[] remain = new byte[2];
				if (lengthOfRemainingBits != 0) {
					for (int i = 0; i < 8 - lengthOfRemainingBits; i++)
						builder.append("0");

					try {
						remain[0] = (byte) Integer.parseInt(builder.substring(0, 8), 2);
						remain[1] = (byte) (8 - lengthOfRemainingBits);
					} catch (Exception e) {
						// Handle exception if necessary
					}
				} else {
					try {
						remain[0] = (byte) Integer.parseInt(builder.substring(0, 8), 2);
						remain[1] = (byte) 0;
					} catch (Exception e) {
						// Handle exception if necessary
					}
				}
				out.write(remain, 0, 2);

				// Close the output stream after writing
				out.close();

				// Store the final size of the compressed file
				lengAfter = outFile.length();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void UniqueFile(StringBuilder fileName) {
		// Create a File object using the input file name
		File file = new File(fileName.toString());

		int number = 1; // Counter to append to the file name
		int flag = 0; // Flag to track if the file name has been modified

		// Loop to check if the file already exists and modify the name accordingly
		while (file.exists()) {
			int lastDotIndex;

			if (flag == 0) {
				// Find the last occurrence of the dot (.) in the file name to locate the
				// extension
				lastDotIndex = fileName.lastIndexOf(".");
				// For the first time, insert a number before the extension
				fileName.insert(lastDotIndex, "(" + (number++) + ")");
			} else {
				// For subsequent iterations, remove the old number from the name
				int startIndex = fileName.lastIndexOf("(");
				int endIndex = fileName.lastIndexOf(")") + 1;
				fileName.delete(startIndex, endIndex); // Remove previous "(number)" part
				// Insert the new number before the extension
				lastDotIndex = fileName.lastIndexOf(".");
				fileName.insert(lastDotIndex, "(" + (number++) + ")");
			}

			// Create a new file object with the updated file name
			file = new File(fileName.toString());

			// Set the flag to 1, indicating the name has been modified
			flag = 1;
		}
	}

	// Creates and returns a TableView populated with Huffman coding data for each
	private TableView<NodeTable> getTable() {
		TableView<NodeTable> table = new TableView<>();
		ObservableList<NodeTable> data = FXCollections.observableArrayList();

		// Populate the observable list with node data for display in the table
		for (Node node : nod) {
			if (node != null) {
				data.add(new NodeTable(node.getCharc(), node.getFreq(), node.getHufman(), node.getHuffmanLength()));
			}
		}

		// Set up table columns for character, frequency, Huffman code, code length, and
		// ASCII value
		TableColumn<NodeTable, String> charColumn = new TableColumn<>("Character");
		charColumn.setCellValueFactory(new PropertyValueFactory<>("charDisplay"));
		charColumn.setPrefWidth(120);

		// Add tooltip to character column
		charColumn.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
					setTooltip(null);
				} else {
					setText(item);
					Tooltip tooltip = new Tooltip("Character: " + item);
					setTooltip(tooltip);
				}
			}
		});

		TableColumn<NodeTable, Number> freqColumn = new TableColumn<>("Frequency");
		freqColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
		freqColumn.setPrefWidth(120);

		TableColumn<NodeTable, String> codeColumn = new TableColumn<>("Huffman Code");
		codeColumn.setCellValueFactory(new PropertyValueFactory<>("huffCode"));
		codeColumn.setPrefWidth(150);

		TableColumn<NodeTable, Number> lengthColumn = new TableColumn<>("Code Length");
		lengthColumn.setCellValueFactory(new PropertyValueFactory<>("huffLength"));
		lengthColumn.setPrefWidth(120);

		TableColumn<NodeTable, Number> asciiColumn = new TableColumn<>("ASCII Value");
		asciiColumn.setCellValueFactory(new PropertyValueFactory<>("asciiValue"));
		asciiColumn.setPrefWidth(120);

		// Customize the header styling
		for (TableColumn<?, ?> column : List.of(charColumn, freqColumn, codeColumn, lengthColumn, asciiColumn)) {
			column.setStyle(
					"-fx-alignment: CENTER; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #f9f9f9; -fx-border-color: black;");
		}

		// Add context menu to the table rows
		table.setRowFactory(tv -> {
			TableRow<NodeTable> row = new TableRow<>();
			ContextMenu contextMenu = new ContextMenu();

			MenuItem infoItem = new MenuItem("View Details");
			infoItem.setOnAction(e -> {
				NodeTable selectedItem = row.getItem();
				if (selectedItem != null) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Node Details");
					alert.setHeaderText("Details for Character: " + selectedItem.getCharDisplay());
					alert.setContentText("Frequency: " + selectedItem.getFrequency() + "\n" + "Huffman Code: "
							+ selectedItem.getHuffCode() + "\n" + "Code Length: " + selectedItem.getHuffLength() + "\n"
							+ "ASCII Value: " + selectedItem.getAsciiValue());
					alert.showAndWait();
				}
			});

			contextMenu.getItems().add(infoItem);
			row.setContextMenu(contextMenu);

			return row;
		});

		// Add all columns to the table
		table.getColumns().addAll(charColumn, asciiColumn, freqColumn, codeColumn, lengthColumn);
		table.setItems(data);
		table.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");

		return table;
	}

//FX for compress scene
	private void SceneMethod() {
		BorderPane border = new BorderPane();

		// Title at the top
		Text title = new Text("Compression Details");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		title.setFill(Color.DARKSLATEBLUE);
		border.setTop(title);

		// Right side table
		border.setRight(getTable());

		// Button to view header details
		Button viewHeader = new Button("View Header Details");
		viewHeader.setStyle(
				"-fx-background-color: #3F72AF; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10; -fx-border-radius: 5;");
		viewHeader.setOnAction(e -> {
			Stage headerStage = new Stage();
			headerStage.setScene(getHeaderScene(headerStage));
			headerStage.show();
		});

		// Labels for sizes
		Label beforeLabel = new Label("Size Before : " + lengAfter + " Byte");
		beforeLabel.setStyle(
				"-fx-text-fill: #141E46; -fx-background-color: white; -fx-padding: 10; -fx-border-color: #41B06E; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 16px;");
		
		Label afterLabel = new Label("Size After : " + lengBefore + " Byte");
		afterLabel.setStyle(
				"-fx-text-fill: #141E46; -fx-background-color: white; -fx-padding: 10; -fx-border-color: #41B06E; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 16px;");

		double percentage = ((double) lengAfter / lengBefore);
		String percentageText = String.format("%.4f%%", percentage * 100);
		;
		if (percentage > 1) {
			percentage = 1;
			percentageText = "percentage: " + String.format("%.4f%%", percentage * 100);
			Label percentageLabel = new Label("percentage : " + percentageText);
			percentageLabel.setStyle(
					"-fx-text-fill: #141E46; -fx-background-color: white; -fx-padding: 10; -fx-border-color: #41B06E; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 16px;");

			// Back button
			Button backButton = new Button("Back");
			backButton.setStyle(
					"-fx-background-color: #3F72AF; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10; -fx-border-radius: 5;");
			backButton.setOnAction(e -> stage.setScene(scene));

			// Layout for left section
			VBox buttonLayout = new VBox(15, viewHeader, beforeLabel, afterLabel, percentageLabel);
			buttonLayout.setAlignment(Pos.CENTER);

			// Set components in the BorderPane
			border.setLeft(buttonLayout);
			border.setAlignment(backButton, Pos.BOTTOM_LEFT);
			border.setBottom(backButton);
			border.setPadding(new Insets(20));

			// Assign this scene to scene12 for reference in other methods
			scene1 = new Scene(border, 1200, 600);
			stage.setScene(scene1);
			borderP.setCenter(border);
			borderP.setStyle("-fx-background-color: #DFF6F0; -fx-padding: 20;");
		}
	}

	// Fx for Header scene
	private Scene getHeaderScene(Stage headerStage) {
		GridPane grid = new GridPane();
		grid.setHgap(20);
		grid.setVgap(20);
		grid.setPadding(new Insets(30));

		Label extensionLengthLabel = new Label("Extension Length:");
		extensionLengthLabel.setStyle("-fx-text-fill: #1F1F1F; -fx-font-size: 18px; -fx-font-weight: bold;");

		Rectangle extensionLengthRect = new Rectangle(200, 40);
		extensionLengthRect.setArcWidth(10);
		extensionLengthRect.setArcHeight(10);
		extensionLengthRect.setFill(Color.web("#FF6F61"));
		extensionLengthRect.setStroke(Color.web("#FF6F61"));
		Text extensionLengthValue = new Text("" + extLength);
		extensionLengthValue.setFill(Color.WHITE);
		extensionLengthValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		extensionLengthRect.setOnMouseEntered(e -> extensionLengthRect.setFill(Color.web("#FF8C75")));
		extensionLengthRect.setOnMouseExited(e -> extensionLengthRect.setFill(Color.web("#FF6F61")));

		Group extensionLengthGroup = new Group(extensionLengthRect, extensionLengthValue);
		extensionLengthValue.setX(50);
		extensionLengthValue.setY(25);

		Label fileExtensionLabel = new Label("File Extension:");
		fileExtensionLabel.setStyle("-fx-text-fill: #1F1F1F; -fx-font-size: 18px; -fx-font-weight: bold;");

		Rectangle fileExtensionRect = new Rectangle(200, 40);
		fileExtensionRect.setArcWidth(10);
		fileExtensionRect.setArcHeight(10);
		fileExtensionRect.setFill(Color.web("#FF6F61"));
		fileExtensionRect.setStroke(Color.web("#FF6F61"));
		Text fileExtensionValue = new Text(fileName);
		fileExtensionValue.setFill(Color.WHITE);
		fileExtensionValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		fileExtensionRect.setOnMouseEntered(e -> fileExtensionRect.setFill(Color.web("#FF8C75")));
		fileExtensionRect.setOnMouseExited(e -> fileExtensionRect.setFill(Color.web("#FF6F61")));

		Group fileExtensionGroup = new Group(fileExtensionRect, fileExtensionValue);
		fileExtensionValue.setX(50);
		fileExtensionValue.setY(25);

		Label headerLengthLabel = new Label("Header Length:");
		headerLengthLabel.setStyle("-fx-text-fill: #1F1F1F; -fx-font-size: 18px; -fx-font-weight: bold;");

		Rectangle headerLengthRect = new Rectangle(200, 40);
		headerLengthRect.setArcWidth(10);
		headerLengthRect.setArcHeight(10);
		headerLengthRect.setFill(Color.web("#FF6F61"));
		headerLengthRect.setStroke(Color.web("#FF6F61"));
		Text headerLengthValue = new Text(headerLength + " Bits");
		headerLengthValue.setFill(Color.WHITE);
		headerLengthValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		headerLengthRect.setOnMouseEntered(e -> headerLengthRect.setFill(Color.web("#FF8C75")));
		headerLengthRect.setOnMouseExited(e -> headerLengthRect.setFill(Color.web("#FF6F61")));

		Group headerLengthGroup = new Group(headerLengthRect, headerLengthValue);
		headerLengthValue.setX(50);
		headerLengthValue.setY(25);

		Label headerLabel = new Label("Header:");
		headerLabel.setStyle("-fx-text-fill: #1F1F1F; -fx-font-size: 18px; -fx-font-weight: bold;");

		// Use TextArea instead of Rectangle to display the header
		TextArea headerTextArea = new TextArea(header);
		headerTextArea.setPrefSize(600, 200);
		headerTextArea.setWrapText(true);
		headerTextArea.setStyle(
				"-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #FF6F61; -fx-control-inner-background: #FF6F61;");

		grid.add(extensionLengthLabel, 0, 0);
		grid.add(extensionLengthGroup, 1, 0);
		grid.add(fileExtensionLabel, 0, 1);
		grid.add(fileExtensionGroup, 1, 1);
		grid.add(headerLengthLabel, 0, 2);
		grid.add(headerLengthGroup, 1, 2);
		grid.add(headerLabel, 0, 3);
		grid.add(headerTextArea, 1, 3);

		VBox layout = new VBox(30, grid);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(30));
		layout.setStyle("-fx-background-color: #F2F2F2;");

		Scene headerScene = new Scene(layout, 1000, 500);
		headerStage.setScene(headerScene);
		return headerScene;
	}

}