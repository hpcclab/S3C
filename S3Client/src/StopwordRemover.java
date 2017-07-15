import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/*
 * Remove redundant, "stopword" from the given data
 * List of word defined as stopword is stored in a file named in config.properties
 */
public class StopwordRemover {
	private String[] queries;
	private HashSet<String> stopSet;
	private BufferedReader br;
	private ArrayList<String> removedlist;
	
	//Constructor
	public StopwordRemover(){
		String location = Config.stopWordsLocation;
		File stopWordFile = new File(location);
		stopSet = new HashSet<>();
		removedlist = new ArrayList<>();
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(stopWordFile)));
			String line;
			while ((line = br.readLine()) != null){
				stopSet.add(line.toLowerCase());
			}
			br.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Cant found the file");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("IOException");
		}
				
	}
	
	/*
	 * Remove stop word from a string
	 */
	public String[] truncate(String query) {
		// TODO Auto-generated method stub
		queries = query.split(" ");
		
		for (String i : queries)
			if(!stopSet.contains(i) && i != null) 
				removedlist.add((String) i);
		
		queries = removedlist.toArray(new String[removedlist.size()]);
		
		return queries;
	}
	
	/*
	 * Remove stop word from a collection
	 */
	public ArrayList<String> truncate(ArrayList<String> collection){
		for (String i : collection)
			if(!stopSet.contains(i) && i != null) 
				removedlist.add((String) i);
		
		return removedlist;
	}

}
