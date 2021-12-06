# FinalCS1

This repository contains the final project for Donovan Pate for the intro to Computer Science 1 class at Olympic College as taught by Dr. Adrian Veliz.

The project is a GUI created for editing and creating rows of a csv file based on the CAWPILE v2 spreadsheet by book youtube channel Book Roast.

Information on the CAWPILE v2 spreadsheet can be found in their video: https://www.youtube.com/watch?v=73pcvjMcUuI

## Before Using:

The application will require at least one csv saved from the cawpile spreadsheet to work. A demo.csv file has been included in case the desire to use the software on its own is present.

### Dependencies

In order to run the application, you must have Java and JavaFX installed, and the CAWPILEGUI must be run with the java command with the javafx module added.

## Usage

Upon opening the application, use the File-> Open menu option to open a csv. New books can be created before loading a csv, but saving to csv before loading a csv with the column headers may cause data loss upon reopening the saved csvs.

After opening a csv with column headers, all functions should operate as named.

## Code Description:

The java code is mainly contained in two files. A short Book object class which represents a single line of the csv, and the CAWPILEGUI.java class

The CAWPILEGUI class is mostly a view controller for the 2 fxml views contained within this project, though it also maintains the booklist which functions as the main data model for the project.

Two FXML views are used:

### StartMenu that contains a menu bar, list view, and 3 buttons

		The menu bar contains options for exiting, opening a csv file to import, 
  			and a help menu with a link to the github and an about alert.
  		The ListView is empty by default, and is set up to contain all the books 
  			loaded via csv. The list is not cleared when importing a new csv to
  			allow for merging csvs, though there is no protection against duplicates.
  		The 3 buttons:
  			Edit selected book: Opens the bookEditor and preloads the selected books info
  			New Book: Opens the bookEditor without preloading data, sets up to add the new book to the list
  			Save to CSV: Opens a save dialog to save the list as a csv file.
  			
### BookEditor that contains a title bar, an accordion with 3 panes, and 3 buttons.

  		The title bar gives the title of the current book
  		The accordion has 3 titled sections:
  			Book information (information about the book itself)
  			Ratings (the user's ratings)
  			Read-through information (information about this read-through of the book)
  		the 3 buttons:
  			Save: Saves the currently filled information to the list (index selected if selected, otherwise adds to list)
  			Copy to Clipboard: Copies the book information to the windows clipboard
  				in a format that can be pasted then split within the google sheet
  			Close: closes the book editor. Does not Save or prompt to save.

## Citations:

The majority of this project was completed referencing only the oracle.java.docs pages for fxml components and the gui demo code written by Dr. Adrian Veliz.

However, there were still times that I became stuck and turned to google. Most googling didn't lead to results, but there are a few pages outside the oracle docs that helped immensely.

For copying to clipboard https://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java

Using for DAYS.between() method to get the days elapsed between two date objects https://stackoverflow.com/questions/27005861/calculate-days-between-two-dates-in-java-8

The lookup method used to associate fx objects in the demo code was not working for me, nor was the @FXML notation. Instead I found a namespace solution: 
https://stackoverflow.com/questions/36769899/javafx-node-lookup-returning-null-only-for-some-elements-in-parent-loaded-with
