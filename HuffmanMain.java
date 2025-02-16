package Application;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class HuffmanMain extends Application {
	File file;

	@Override
	public void start(Stage primaryStage) {
		// Create the BorderPane for the main scene
		BorderPane borderPane = new BorderPane();
		Scene initialScene = new Scene(borderPane, 800, 600);
		borderPane.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 30;");
		Label beLabel = new Label();
		Image cursorImage = new Image("file:C:\\Users\\ASUS\\Desktop\\Algo\\TestProj\\src\\ProjectTwo\\cursor.png");
		Cursor cursor = new ImageCursor(cursorImage);

		// Top Section: Title and Description
		Label topLabel = new Label("Welcome to the Huffman Compression");
		topLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 26; -fx-text-fill: #333;");
		topLabel.setAlignment(Pos.CENTER);

		Label topLabel2 = new Label("Choose an option to proceed:");
		topLabel2.setStyle("-fx-font-weight: normal; -fx-font-size: 18; -fx-text-fill: #555;");
		topLabel2.setAlignment(Pos.CENTER);

		// Image on the left side
		Image image = new Image("file:/C:/Users/ASUS/Desktop/Algo/Pro2/src/photo7-removebg-preview.png");
		ImageView imageView = new ImageView(image);
		imageView.setFitWidth(100);
		imageView.setFitHeight(100);
		borderPane.setRight(imageView);

		Image image2 = new Image("file:/C:/Users/ASUS/Desktop/Algo/Pro2/src/photo7-removebg-preview.png");
		ImageView imageView2 = new ImageView(image2);
		imageView2.setFitWidth(100);
		imageView2.setFitHeight(100);
		borderPane.setLeft(imageView2);

		// Compress Button with Image (Pink Color)
		ImageView compressImage = new ImageView(
				new Image("file:/C:/Users/ASUS/Desktop/Algo/Pro2/src/photo4-removebg-preview.png"));
		compressImage.setFitHeight(80);
		compressImage.setFitWidth(80);

		Button compressButton = new Button("Compress", compressImage);
		compressButton.setPrefSize(120, 50);
		compressButton.setContentDisplay(ContentDisplay.TOP);
		compressButton.setStyle(
				"-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
		compressButton.setOnMouseEntered(e -> compressButton.setStyle("-fx-background-color: #D81B60;"));
		compressButton.setOnMouseExited(e -> compressButton.setStyle("-fx-background-color: #E91E63;"));

		// Decompress Button with Image (Blue Color)
		ImageView decompressImage = new ImageView(
				new Image("file:/C:/Users/ASUS/Desktop/Algo/Pro2/src/photo3-removebg-preview.png"));
		decompressImage.setFitHeight(80);
		decompressImage.setFitWidth(80);

		Button decompressButton = new Button("Decompress", decompressImage);
		decompressButton.setPrefSize(120, 50);
		decompressButton.setContentDisplay(ContentDisplay.TOP);
		decompressButton.setStyle(
				"-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
		decompressButton.setOnMouseEntered(e -> decompressButton.setStyle("-fx-background-color: #1976D2;"));
		decompressButton.setOnMouseExited(e -> decompressButton.setStyle("-fx-background-color: #2196F3;"));

		// Middle Section Layout (buttons)
		HBox middleSection = new HBox(50, compressButton, decompressButton);
		middleSection.setAlignment(Pos.CENTER);

		// VBox to hold the title and buttons
		VBox centerVBox = new VBox(20, topLabel, topLabel2, middleSection);
		centerVBox.setAlignment(Pos.CENTER);
		borderPane.setCenter(centerVBox);

		// File chooser setup
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"), // This filter shows all files
				new ExtensionFilter("Text Files", "*.txt"),
				new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
				new ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi"));

		compressButton.setOnAction(e -> {
			// Define extension filters for text, images, and videos
			ExtensionFilter filterAll = new ExtensionFilter("All Files", "*.*");
			ExtensionFilter filterTXT = new ExtensionFilter("Text Files (*.txt)", "*.txt");
			ExtensionFilter filterIMG = new ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg",
					"*.jpeg");
			ExtensionFilter filterVID = new ExtensionFilter("Video Files (*.mp4, *.mkv, *.avi)", "*.mp4", "*.mkv",
					"*.avi");
			ExtensionFilter filterTXT1 = new ExtensionFilter("Text Files (*.pdf)", "*.pdf");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().addAll(filterAll, filterTXT, filterIMG, filterVID,filterTXT1);
			
			// Open file chooser dialog
			File file = fileChooser.showOpenDialog(primaryStage);

			if (file == null) {
				// Handle case where no file was selected
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("No File Selected");
				alert.setHeaderText("Please select a valid file to proceed.");
				alert.showAndWait();
				return;
			}

			try {
				if (!file.exists() || file.length() == 0) {
					throw new IOException("File is empty or does not exist.");
				}

				// Proceed with compression scene setup
				CompressClass compressScene = new CompressClass(primaryStage, initialScene, file);
				compressScene.setCursor(cursor);
				primaryStage.setScene(compressScene);
			} catch (IOException ex) {
				// Show error alert if the file is invalid
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Invalid File");
				alert.setContentText("The selected file is empty or could not be read. Please choose a valid file.");
				alert.showAndWait();
			} catch (Exception ex) {
				// Handle other unexpected exceptions
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Unexpected Error");
				alert.setContentText("An unexpected error occurred: " + ex.getMessage());
				alert.showAndWait();
			}
		});

		decompressButton.setOnAction(e -> {
			// Define extension filters for text, images, and videos
			ExtensionFilter filterTXT = new ExtensionFilter("Text Files (*.txt)", "*.txt");
			ExtensionFilter filterIMG = new ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg",
					"*.jpeg");
			ExtensionFilter filterVID = new ExtensionFilter("Video Files (*.mp4, *.mkv, *.avi)", "*.mp4", "*.mkv",
					"*.avi");
			ExtensionFilter filterHUFF = new ExtensionFilter("Compressed Files (*.huff)", "*.huff");

			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().addAll(filterHUFF, filterTXT, filterIMG, filterVID);

			// Open file chooser dialog
			File file = fileChooser.showOpenDialog(primaryStage);

			if (file == null) {
				// Handle case where no file was selected
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("No File Selected");
				alert.setHeaderText("Please select a valid file to proceed.");
				alert.showAndWait();
				return;
			}

			try {
				if (!file.exists() || file.length() == 0) {
					throw new IOException("File is empty or does not exist.");
				}

				// Proceed with decompression scene setup
				DecompressClass decompressScene = new DecompressClass(primaryStage, initialScene, file);
				decompressScene.setCursor(cursor);
				// Print the name of the file
				beLabel.setText("Selected file: " + file.getName() + "\n" + "File decompressed successfully");
				borderPane.setBottom(beLabel);
			} catch (IOException ex) {
				// Show error alert if the file is invalid
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Invalid File");
				alert.setContentText("The selected file is empty or could not be read. Please choose a valid file.");
				alert.showAndWait();
			} catch (Exception ex) {
				// Handle other unexpected exceptions
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Unexpected Error");
				alert.setContentText("An unexpected error occurred: " + ex.getMessage());
				alert.showAndWait();
			}
		});

		// Initial Scene Setup
		primaryStage.setTitle("Huffman Compression");
		primaryStage.setScene(initialScene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
