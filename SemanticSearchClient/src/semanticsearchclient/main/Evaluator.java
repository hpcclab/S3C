/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchclient.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import semanticsearchclient.utilities.Constants;

/**
 *Evaluator.
 * Evaluates a system using our algorithm.  Builds a hash map of our pre-judged
 * relevance file and computes from that.
 */
public class Evaluator {
    //Internal representation of how relevant a query is to a file.
    //Maps a query string to another mapping of file name to relevance value.
    HashMap<String,HashMap<String,String>> relevanceMap;
    
    public String query;
    public float systemValue;
    
    /**
     * Default Constructor.
     * Build up the mapping from the internal file
     * Right now file must be named "relevances.txt" 
     * TODO: Change relevance file name to config based.
     * 
     * File is organized in the following fashion
     *  query-filename-value
     * Value is one of 3 characters:
     *  v - very relevant
     *  s - somewhat relevant
     *  n - not relevant
     */
    public Evaluator() {
        BufferedReader br;
        File file = new File(".." + File.separator + ".." + File.separator + "relevances.txt");
        String currentLine;
        relevanceMap = new HashMap<>();
        
        if (!file.exists()) {
            System.err.println("Could not find relevances file.");
            System.exit(0);
        }
        
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath())));
            
            //Put the data in the map
            while ((currentLine = br.readLine()) != null) {
                //Split into tokens:
                //0: query, 1: file name, 2: value
                String[] tokens = currentLine.split("-");
                
                //If the map currently contains data for the query, just add the additional data in
                if (relevanceMap.containsKey(tokens[0])) {
                    //get the map associated with that query, and put file name and value in its map
                    relevanceMap.get(tokens[0]).put(tokens[1], tokens[2]);
                } 
                //If not, create a new map there, and then put the data in
                else {
                    relevanceMap.put(tokens[0], new HashMap<>());
                    relevanceMap.get(tokens[0]).put(tokens[1], tokens[2]);
                }
            }
            
            br.close();
            
        } catch(IOException e) {
            System.err.println("Error reading relevances file");
        }
    }
    
    
    /**
     * Perform the evaluation using the given query and results based on the predetermined data.
     * Uses the algorithm:
     *  (Sum from i=0 to N of ri) / N
     * Where i is the rank.
     * Where ri is:
     *  1/i if the result is very relevant
     *  1/2i if the result is somewhat relevant
     *  0 if it the result is not relevant
     * Where N is the number of documents being evaluated (usually 10)
     * @param query
     * @param results
     * @return 
     */
    public float evaluate(String query, ArrayList<String> results) {
        float systemValue = 0f;
        ArrayList<String> unevaluatedFiles = new ArrayList<>();
        
        //Make sure the query has been evaluated by hand.  If not, exit.
        if (!relevanceMap.containsKey(query)) {
            System.out.println("This query has not yet been evaluated");
            return -1.0f;
        }
        
        HashMap<String, String> fileToValueMap = relevanceMap.get(query);
        
        int rank = 1;
        for (String fileName : results) {
            //If this file name is not in the map, we should just skip it, and mark that it is unevaluated
            if (!fileToValueMap.containsKey(fileName)) {
                unevaluatedFiles.add(fileName);
                continue;
            }
            
            //Add to the running sum based on the value for this file name
            switch(fileToValueMap.get(fileName)) {
                case "v": //Very relevant
                    systemValue += 1f/rank;
                    break;
                case "s": //Somewhat relevant
                    systemValue += 1f/(2f*rank);
                    break;
                case "n": //Not relevant
                    //Don't add anything.
            }
            
            rank++;
        }
        
        //Now normalize the value by dividing it by the total number of documents
        systemValue = systemValue / results.size();
        
        //If there were any that had not been evaluated in our document, print them out here.
        if (unevaluatedFiles.size() > 0) {
            System.out.println("\nThere were some unevaluated files: " + unevaluatedFiles);
            System.out.println("Please given them an evaluation in the relevances file.");
            systemValue = -1.0f;
        }
        
        this.query = query;
        this.systemValue = systemValue;
        
        return systemValue;
    }
    
    
    /**
     * Write the results of the last evaluation to file.
     * NOTE: evaluate() MUST be called before this if you want the numbers to mean anything.
     * Evaluation results are stored in the following format:
     *  Model Name-Query-Value
     * 
     * Duplications will be overwritten
     */
    public void writeToFile() {
        /*
        Read in the current file
        Put the info into a mapping of models to a mapping of queries to values
        */
        BufferedReader br; 
        File file = new File(Constants.evaluationFileName);
        String currentLine;
        HashMap<String, HashMap<String, String>> evaluationResults;
        evaluationResults = new HashMap<>();
        
        
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()))); 

            while ((currentLine = br.readLine()) != null) {
                String[] tokens = currentLine.split("-");
                /*
                [0] - Model name
                [1] - Query
                [2] - Value
                */
                //If the map currently contains data for the model, just add the additional data in
                if (evaluationResults.containsKey(tokens[0])) {
                    //get the map associated with that model, and put query and value in its map
                    evaluationResults.get(tokens[0]).put(tokens[1], tokens[2]);
                } 
                //If not, create a new map there, and then put the data in
                else {
                    evaluationResults.put(tokens[0], new HashMap<>());
                    evaluationResults.get(tokens[0]).put(tokens[1], tokens[2]);
                }
            }

            //Now the whole file is in that hash map.
            br.close();
        } catch(IOException e) {
            System.err.println("Error reading from the evaluation results file");
            return;
        }
        
        
        //Put the new data into the hashmap.  Have to make sure there's something there again.
        if (evaluationResults.containsKey(Constants.modelName)) {
            //get the map associated with that model, and put query and value in its map
            evaluationResults.get(Constants.modelName).put(query, String.valueOf(systemValue));
        } 
        //If not, create a new map there, and then put the data in
        else {
            evaluationResults.put(Constants.modelName, new HashMap<>());
            evaluationResults.get(Constants.modelName).put(query, String.valueOf(systemValue));
        }
        
        
        //Write the new data back.
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath())));
            
            for (String modelName : evaluationResults.keySet()) {
                HashMap<String, String> queryToValue = evaluationResults.get(modelName);
                for (String q : queryToValue.keySet()) {
                    String value = queryToValue.get(q);
                    
                    bw.write(modelName + "-" + q + "-" + value);
                    bw.newLine();
                    
                    System.out.println(modelName + "-" + q + "-" + value);
                }
            }
            
            bw.flush();
            bw.close();
        } catch(IOException e) {
            System.err.println("Error writing to evaluation results file.");
            return;
        }
        
        System.out.println("Results written to file: " + Constants.evaluationFileName);
    }
}