
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;



public class ReadCSV {
 

  public Hashtable<String, String> parse() {
 
	String current_dir = System.getProperty("user.dir");
	String csvFile = current_dir + "/src/Records.csv";
	BufferedReader br = null;
	String line = "";
	String csvSplitBy = " ";
	Hashtable<String, String> entries = new Hashtable<String, String>();
	
	try {
 
		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
			
		    // use comma and space as separator
			String[] entry = line.split(csvSplitBy);
			
			entries.put(entry[0], entry[1]);
			
		}
 
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	return entries;
  }
 
}