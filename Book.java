/* Author Donovan Pate "Nyefari" */

/*  This is the object class for Books. Currently it just contains a single
 *  field which is a String array of all the values in the book. In future
 *  versions the column labels will likely be passed as well so that the
 *  fields can be encapsulated better and individual values of the String
 *  Array can be gotten/set based on the column. I will likely do this by
 *  having the values array be 2 dimensional with values[0] being the column labels.
 *  The getter will function by running through values[0] until the desired label is found,
 *  then the equivalent index from values[1] will be returned
 */
 
import java.util.*;

public class Book {
	private String[] values;
	
	// takes a line from the csv and turns it into a Book object
	public Book(String bookString){
		this.values = bookString.split(",");
	}
	
	// takes in a string array. This is only used to create a dummy new book.
	public Book(String[] bookValues){
		this.values = bookValues;
	}
	
	// get method for returning the value of the string array at a given position
	public String getValue(int i){
		return values[i];
	}
	
	// method that determines how the book would be printed.
	// benefit: determines how the book shows in the ListView
	public String toString(){
		return "\"" + values[0] + "\" by " + values[2];
	}
	
	// turns the book's values array back into the equivalent line of a csv
	public String toCSVLine(){
		String csvLine = values[0];
		for(int i = 1; i < values.length; i++){
			System.out.println(i + ": " + values[i]);
			csvLine += "," + values[i];
		}
		return csvLine;
	}
}

