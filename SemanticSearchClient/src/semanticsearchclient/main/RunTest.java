/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.main;

import java.util.ArrayList;
import java.util.Scanner;
import semanticsearchclient.utilities.Constants;

/**
 * Class to run many tests at a time.
 * Run this to get a number of tests done on this system
 * @author Jason
 */
public class RunTest {
    public static void main(String[] args) {
        Scanner scan;
        scan = new Scanner(System.in);
        
        int numTests;
        System.out.println("How many times do you want to run for each query?");
        
        numTests = scan.nextInt();
        
        ArrayList<String> queries = new ArrayList<>();
        System.out.println("Enter your queries.  Enter ';' to stop");
        
        System.out.print("Query: ");
        String q = scan.nextLine();
        
        while (!q.equals(";")) {
            queries.add(q);
            System.out.print("Query: ");
            q = scan.nextLine();
        }
        
        System.out.println(queries);
        
        for (String query : queries) {
            if (query.isEmpty())
                continue;
            for (int i = 0; i < numTests; i++) {
                String [] params = {"-s", query};
                SemanticSearchClient.main(params);
                Constants.mauiKeyOptions = new String[] {"-l", "data/tmp/", "-m", "keyphrextr", "-t", "PorterStemmer", "-v", "none"};
                
                System.out.print("\n\n\n");
            }
        }
    }
    
}
