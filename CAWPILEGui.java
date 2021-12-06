// Author: Donovan Pate "Nyefari"

/* This program is the view controller for a GUI written as a way of modifying a saved csv
 * from the CAWPILE V2 spreadsheet as developed by the youtube channel Book Roast.
 * Their video on the spreadsheet can be found: https://www.youtube.com/watch?v=73pcvjMcUuI
 * 
 * Two views are used:
 * 1. StartMenu that contains a menu bar, list view, and 3 buttons
 * 		The menu bar contains options for exiting, opening a csv file to import, 
 * 			and a help menu with a link to the github and an about alert.
 * 		The ListView is empty by default, and is set up to contain all the books 
 * 			loaded via csv. The list is not cleared when importing a new csv to
 * 			allow for merging csvs, though there is no protection against duplicates.
 * 		The 3 buttons:
 * 			Edit selected book: Opens the bookEditor and preloads the selected books info
 * 			New Book: Opens the bookEditor without preloading data, sets up to add the new book to the list
 * 			Save to CSV: Opens a save dialog to save the list as a csv file.
 * 2. BookEditor that contains a title bar, an accordion with 3 panes, and 3 buttons.
 * 		The title bar gives the title of the current book
 * 		The accordion has 3 titled sections:
 * 			Book information (information about the book itself)
 * 			Ratings (the user's ratings)
 * 			Read-through information (information about this read-through of the book)
 * 		the 3 buttons:
 * 			Save: Saves the currently filled information to the list (index selected if selected, otherwise adds to list)
 * 			Copy to Clipboard: Copies the book information to the windows clipboard
 * 				in a format that can be pasted then split within the google sheet
 * 			Close: closes the book editor. Does not Save or prompt to save.
 */
import java.awt.Desktop;
import java.awt.Desktop.*;
import java.awt.datatransfer.StringSelection; // For copying to clipboard https://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit; //Using for DAYS.between() method per https://stackoverflow.com/questions/27005861/calculate-days-between-two-dates-in-java-8
import javafx.collections.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.stage.FileChooser.*;
import javafx.util.*;


public class CAWPILEGui extends Application{
	// Variables relating to mainly the Start Menu
	private static FileChooser fileChooser;
	private static String csvFilePath;
	private static Stage startStage;
	private static Scene startScene;
	private static ListView bookListView;
	private static ObservableList<Book> bookList;
	private static String columnString;
	
	// Values relating to mainly the bookEditor
	private static Stage editorStage;
	private static Scene editorScene;
	private static int currentBookIndex;
	private static Label bookEditorTitle;
	private static TextField title;
	private static TextField author;
	private static TextField publicationYear;
	private static TextField subGenre;
	private static TextField nationality;
	private static TextField lgbtSpecifics;
	private static TextField language;
	private static TextField dnfReason;
	private static TextField pagesRead;
	private static ChoiceBox mainGenre;
	private static ChoiceBox length;
	private static ChoiceBox ageCategory;
	private static ChoiceBox series;
	private static ChoiceBox writtenBy;
	private static ChoiceBox characters;
	private static ChoiceBox atmosphere;
	private static ChoiceBox writing;
	private static ChoiceBox plot;
	private static ChoiceBox intrigue;
	private static ChoiceBox logic;
	private static ChoiceBox enjoyment;
	private static ChoiceBox format;
	private static ChoiceBox ownership;
	private static CheckBox lastInSeries;
	private static CheckBox poc;
	private static CheckBox lgbtq;
	private static CheckBox ownVoices;
	private static CheckBox reRead;
	private static CheckBox dnf;
	private static DatePicker startDate;
	private static DatePicker finishDate;
	private static Label rating;
	private static DateTimeFormatter ukFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// load startMenu gui from fxml
		FXMLLoader startLoader = new FXMLLoader(getClass().getResource("StartMenu.fxml"));
		VBox startVbox = startLoader.<VBox>load();
		Scene primaryScene = new Scene(startVbox);
		startScene = primaryScene;
		
		// load bookEditor gui from fxml
		FXMLLoader editorLoader = new FXMLLoader(getClass().getResource("BookEditor.fxml"));
		VBox editorVbox = editorLoader.<VBox>load();
		Map<String, Object> editorNamespace = editorLoader.getNamespace();
		Scene testScene = new Scene(editorVbox);
		editorScene = testScene;
		editorStage = new Stage();
		
		// setup some global vars. Preset the FileChooser to look for .csv files
		startStage = primaryStage;
		fileChooser = new FileChooser();
		fileChooser.setTitle("Open File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
		
		// setup the TextFields in the editor
		// the lookup method used in the demo wasn't working. I couldn't figure out why, but I did find another solution:
		// https://stackoverflow.com/questions/36769899/javafx-node-lookup-returning-null-only-for-some-elements-in-parent-loaded-with
		bookEditorTitle = (Label)editorNamespace.get("bookEditorTitle");
		title = (TextField)editorNamespace.get("title");
		author = (TextField)editorNamespace.get("author");
		publicationYear = (TextField)editorNamespace.get("publicationYear");
		subGenre = (TextField)editorNamespace.get("subGenre");
		nationality = (TextField)editorNamespace.get("nationality");
		lgbtSpecifics = (TextField)editorNamespace.get("lgbtSpecifics");
		language = (TextField)editorNamespace.get("language");
		dnfReason = (TextField)editorNamespace.get("dnfReason");
		pagesRead = (TextField)editorNamespace.get("pagesRead");
		
		// setup the ChoiceBoxes in the editor with the choices they need
		// each setup to contain the values they should based on the spreadsheet
		mainGenre = (ChoiceBox)editorNamespace.get("mainGenre");
		mainGenre.getItems().addAll("Adventure","Biography","Classic","Comic","Contemporary","Crime","Fantasy","Graphic Novel","Historical Fiction","Horror","Dystopia","Magical Realism","Manga","Mystery","Mythology","Non-fiction","Paranormal","Poetry","Romance","Science fiction","Short story","Thriller");
		length = (ChoiceBox)editorNamespace.get("length");
		length.getItems().addAll("Under 100","100-150","150-200","200-250","250-300","300-350","350-400","400-450","450-500","500-550","550-600","600-650","650-700","700-750","750-800","800-850","850-900","900-950","950-1000","1000 and more");
		ageCategory = (ChoiceBox)editorNamespace.get("ageCategory");
		ageCategory.getItems().addAll("Children's","Middle Grade","Young adult","New adult","Adult");
		series = (ChoiceBox)editorNamespace.get("series");
		series.getItems().addAll("Standalone","Series");
		writtenBy = (ChoiceBox)editorNamespace.get("writtenBy");
		writtenBy.getItems().addAll("Non-Binary","Woman","Man","Multiple Authors","Not Specified");
		characters = (ChoiceBox)editorNamespace.get("characters");
		characters.getItems().addAll("1","2","3","4","5","6","7","8","9","10");
		atmosphere = (ChoiceBox)editorNamespace.get("atmosphere");
		atmosphere.getItems().addAll("1","2","3","4","5","6","7","8","9","10");
		writing = (ChoiceBox)editorNamespace.get("writing");
		writing.getItems().addAll("1","2","3","4","5","6","7","8","9","10");
		plot = (ChoiceBox)editorNamespace.get("plot");
		plot.getItems().addAll("1","2","3","4","5","6","7","8","9","10");
		intrigue = (ChoiceBox)editorNamespace.get("intrigue");
		intrigue.getItems().addAll("1","2","3","4","5","6","7","8","9","10");
		logic = (ChoiceBox)editorNamespace.get("logic");
		logic.getItems().addAll("1","2","3","4","5","6","7","8","9","10");
		enjoyment = (ChoiceBox)editorNamespace.get("enjoyment");
		enjoyment.getItems().addAll("1","2","3","4","5","6","7","8","9","10");
		format = (ChoiceBox)editorNamespace.get("format");
		format.getItems().addAll("Physical","E-book","Audiobook","Mixed media");
		ownership = (ChoiceBox)editorNamespace.get("ownership");
		ownership.getItems().addAll("Owned","Borrowed","Gifted");
		
		// setup the CheckBoxes in the editor
		lastInSeries = (CheckBox)editorNamespace.get("lastInSeries");
		lastInSeries.setIndeterminate(false);
		poc = (CheckBox)editorNamespace.get("poc");
		poc.setIndeterminate(false);
		ownVoices = (CheckBox)editorNamespace.get("ownVoices");
		ownVoices.setIndeterminate(false);
		lgbtq = (CheckBox)editorNamespace.get("lgbtq");
		lgbtq.setIndeterminate(false);
		reRead = (CheckBox)editorNamespace.get("reRead");
		reRead.setIndeterminate(false);
		dnf = (CheckBox)editorNamespace.get("dnf");
		dnf.setIndeterminate(false);
		
		// setup the DatePickers in the editor
		startDate = (DatePicker)editorNamespace.get("startDate");
		finishDate = (DatePicker)editorNamespace.get("finishDate");
		
		// setup the rating Label in the editor
		rating = (Label)editorNamespace.get("rating");
		
		// Sets up the bookList tableView
		bookListView = (ListView)startScene.getRoot().lookup("#bookListView");
		bookList = FXCollections.observableArrayList();
		bookListView.setItems(bookList);
		
		// actually show the gui
		primaryStage.setTitle("CAWPILE GUI v1");
		primaryStage.setScene(primaryScene);
		primaryStage.show();
	}
	
	// this method takes in the Scanner object from the opened csv and splits it into lines 
	// which are used to make the books in the bookList
	private void fillBookList(Scanner csvScan){
		// The first line in a CSV is the column headers, which we want to keep for when we save our list.
		if(csvScan.hasNextLine()){
			columnString = csvScan.nextLine();
			String[] columns = columnString.split(",");
		} else {
			throw new IllegalArgumentException("The passed csv Scanner has no rows");
		}
		while(csvScan.hasNextLine()){
			String bookCandidate = csvScan.nextLine();
			if(bookCandidate.charAt(0) != ','){ // This ignores any empty lines
				Book newBook = new Book(bookCandidate);
				bookList.addAll(newBook);
			}
		}
	}
	
	@FXML
	public void exit(Event e){
		System.exit(0);
	}
	
	// Opens an open dialog for selecting a csv file. filters for csvs.
	@FXML
	public void open(Event e){
		try{
			File csvFile = fileChooser.showOpenDialog(startStage);
			Scanner csvScan = new Scanner(csvFile);
			fillBookList(csvScan);
		} catch(IOException ioe){
			ioe.printStackTrace();
		} catch(NullPointerException npe){
			// cancel
		}
	}
	
	// the help menu option opens up the project on github
	@FXML
	public void browser(Event e){
		String url = "https://github.com/cs-olympic/finalcs1-Nyefari";
		getHostServices().showDocument(url);
	}
	
	// this is the hyperlink at the bottom of the startmenu to go to the 
	// Cawpile v2 spreadsheet video by Book Roast
	@FXML
	public void hyper(Event e){
		String url = "https://www.youtube.com/watch?v=73pcvjMcUuI";
		getHostServices().showDocument(url);
	}
	
	// this is the about button within the menu
	@FXML
	public void info(Event e){
		Alert alert = new Alert(AlertType.INFORMATION, """
		This GUI was written by Donovan Pate
		It is designed to use csv saves from the CAWPILE spreadsheet
		as developed by the youtube channel Book Roast.
		This software was written as a final project for CS141
		at Olympic College as taught be Dr. Adrian Veliz in Fall 2021.
		Further planned improvements can be followed on github.""");
		alert.setTitle("About");
		alert.setHeaderText("CAWPILE GUI ABOUT");
		alert.show();
	}
	
	// This method uses the toCSVLine method of each book in the booklist to build out the new csv file
	// a save dialog is used to select the file to save to.
	@FXML
	public void saveToCSV(Event e){
		File saveFile = fileChooser.showSaveDialog(startStage);
		try{
			PrintStream saveStream = new PrintStream(saveFile);
			saveStream.println(columnString);
			for(Book book: bookList){
			saveStream.println(book.toCSVLine());
			}
		}catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
		}
	}
	
	// create an empty book, set the index negative to indicate a specific
	// book is not being edited, and open the editor with the blank book
	@FXML
	public void newBook(Event e){
		String[] emptyBookValues = new String[44];
		Book currentBook = new Book(emptyBookValues);
		currentBookIndex = -1;
		initializeEditor(currentBook);
	}
	
	// if a book is selected, get it and open the editor with it
	// if not, do nothing.
	@FXML
	public void editBook(Event e){
		if(bookListView.getSelectionModel().getSelectedItem() == null){
			return;
		}
		Book currentBook = (Book)bookListView.getSelectionModel().getSelectedItem();
		// I could update this everytime a different book is selected, but that would be excessive
		// since the startmenu is about to be hidden anyway
		currentBookIndex = bookListView.getSelectionModel().getSelectedIndex();
		initializeEditor(currentBook);
	}
	
	private void initializeEditor(Book currentBook){
		// set the TextFields in the editor to match the selected book
		bookEditorTitle.setText("Editing " + currentBook.toString());
		title.setText(currentBook.getValue(0));
		author.setText(currentBook.getValue(2));
		publicationYear.setText(currentBook.getValue(9));
		subGenre.setText(currentBook.getValue(7));
		nationality.setText(currentBook.getValue(41));
		lgbtSpecifics.setText(currentBook.getValue(37));
		language.setText(currentBook.getValue(35));
		dnfReason.setText(currentBook.getValue(31));
		pagesRead.setText(currentBook.getValue(4));
		
		// set the ChoiceBoxes in the editor to match the selected book
		mainGenre.setValue(currentBook.getValue(6));
		length.setValue(currentBook.getValue(5));
		ageCategory.setValue(currentBook.getValue(8));
		series.setValue(currentBook.getValue(12));
		writtenBy.setValue(currentBook.getValue(40));
		characters.setValue(currentBook.getValue(14));
		atmosphere.setValue(currentBook.getValue(15));
		writing.setValue(currentBook.getValue(16));
		plot.setValue(currentBook.getValue(17));
		intrigue.setValue(currentBook.getValue(18));
		logic.setValue(currentBook.getValue(19));
		enjoyment.setValue(currentBook.getValue(20));
		format.setValue(currentBook.getValue(3));
		ownership.setValue(currentBook.getValue(10));
		
		// set the Rating field up
		rating.setText("Rating: " + currentBook.getValue(21));
		
		// set the CheckBoxes in the editor to match the selected book
		lastInSeries.setSelected(currentBook.getValue(13).equals("TRUE"));
		poc.setSelected(currentBook.getValue(38).equals("Yes"));
		lgbtq.setSelected(currentBook.getValue(36).equals("Present"));
		ownVoices.setSelected(currentBook.getValue(42).equals("Yes"));
		reRead.setSelected(currentBook.getValue(11).equals("Re-read"));
		dnf.setSelected(currentBook.getValue(28).equals("TRUE"));
		
		// set the DatePickers in the editor to match the selected book
		if(currentBook.getValue(32) == ""){
			startDate.setValue(LocalDate.now());
		} else {
			startDate.setValue(LocalDate.parse(currentBook.getValue(32),ukFormatter));
		}
		if(currentBook.getValue(33) == ""){
			finishDate.setValue(LocalDate.now());
		} else {
			finishDate.setValue(LocalDate.parse(currentBook.getValue(33),ukFormatter));
		}
		
		// actually show the gui
		editorStage.setTitle("CAWPILE GUI v1");
		editorStage.setScene(editorScene);
		editorStage.show();
		startStage.hide();
	}
	
	// gets the current editor values as a book and saves it to the selected index if a book was being edited
	// if not adds the book to the list instead of replacing one.
	// replacing the book is done rather than updating the current book so
	// that the ListView gets updated thanks to the listener on the ObservableList.
	@FXML
	public void saveBook(Event e){
		System.out.println("Current Book Saved!");
		Book newBook = new Book(getCurrentEditorBookString());
		if(currentBookIndex < 0){
			bookList.addAll(newBook);
		} else {
			bookList.set(currentBookIndex, newBook);
		}
		initializeEditor(newBook);
	}
	
	// this event gets the csv string for the current information in the editor and saves it to clipboard
	// https://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java
	@FXML
	public void copy(Event e){
		System.out.println("Book Copied to Clipboard");
		StringSelection stringSelection = new StringSelection(getCurrentEditorBookString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	@FXML
	public void cancelEditor(Event e){
		System.out.println("Editor Closed!");
		startStage.show();
		editorStage.close();
	}
	
	// The rating is the average of individual ratings
	private void updateRating(){
		double ratingDouble = (Integer.parseInt((String)characters.getValue()) +
			Integer.parseInt((String)atmosphere.getValue()) +
			Integer.parseInt((String)writing.getValue()) +
			Integer.parseInt((String)plot.getValue()) +
			Integer.parseInt((String)intrigue.getValue()) +
			Integer.parseInt((String)logic.getValue()) +
			Integer.parseInt((String)enjoyment.getValue())) / 7.0;
		rating.setText("Rating: " + String.format("%.2f", ratingDouble));
	}
	
	// this method gets the current information in the editor view and creates
	// a string that represents the line the book would be in a csv file
	private String getCurrentEditorBookString(){
		// First update the rating value
		updateRating();
		
		// Each checkbox needs to be converted to a string
		String reReadString = reRead.isSelected() ? "Re-read" : "First time";
		String lastString = lastInSeries.isSelected() ? "TRUE" : "FALSE";
		String dnfString = dnf.isSelected() ? "TRUE" : "FALSE";
		String lgbtqString = lgbtq.isSelected() ? "Present" : "Absent";
		String pocString = poc.isSelected() ? "Yes" : "No";
		String ownVoicesString = ownVoices.isSelected() ? "Yes" : "N/A";
		
		// The month must be formatted to match the spreadsheet
		String monthString = finishDate.getValue().getMonth().toString();
		monthString = monthString.charAt(0) + monthString.substring(1).toLowerCase();
		
		// this return statement builds the csv line value by value in the
		// order that they are in the CAWPILE spreadsheet
		return (title.getText() + "," +
			monthString + "," +
			author.getText() + "," +
			format.getValue() + "," +
			pagesRead.getText() + "," +
			length.getValue() + "," +
			mainGenre.getValue() + "," +
			subGenre.getText() + "," +
			ageCategory.getValue() + "," +
			publicationYear.getText() + "," +
			ownership.getValue() + "," +
			reReadString + "," +
			series.getValue() + "," +
			lastString + "," +
			characters.getValue() + "," +
			atmosphere.getValue() + "," +
			writing.getValue() + "," +
			plot.getValue() + "," +
			intrigue.getValue() + "," +
			logic.getValue() + "," +
			enjoyment.getValue() + "," +
			rating.getText().substring(8) + ",,,,,," + // 5 extra columns I don't want to set directly
			(int)(Double.valueOf(rating.getText().substring(8)) / 14) + "," +
			dnfString + ",," + // an empty column exists here in the spreadsheet
			(int)(Double.valueOf(rating.getText().substring(8)) / 14) + "," + // The stars column has the same value as the earlier column most of the time
			dnfReason.getText() + "," +
			startDate.getValue().format(ukFormatter) + "," +
			finishDate.getValue().format(ukFormatter) + "," +
			ChronoUnit.DAYS.between(startDate.getValue(),finishDate.getValue()) + "," + //https://stackoverflow.com/questions/36769899/javafx-node-lookup-returning-null-only-for-some-elements-in-parent-loaded-with
			language.getText() + "," +
			lgbtqString + "," +
			lgbtSpecifics.getText() + "," +
			pocString + ",," + // Extra , for column not included in Editor (main subject does not use)
			writtenBy.getValue() + "," +
			nationality.getText() + "," +
			ownVoicesString + ",created in GUI" // Extra column for Notes (not in this version of Editor) with note that it was created in the Gui
			);
	}
}
