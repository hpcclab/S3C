/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticsearchcloud.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A class full of static methods meant to write various performance and
 * overhead metrics throughout.
 * 
 * Measures the following statistics:
 *  Number of entries in the inverted index (i.e. number of distint terms)
 *  Time to construct the index file.
 * 
 * NOTE: This could all probably be condensed to like one function
 * 
 * @author Jason
 */
public class CloudMetrics {
    
    /**
     * Write the time it took to construct all of the utility files, inverted
     * index etcetera.
     * 
     * Appends to the file, as these will be averaged in the python file.
     * 
     * Writes to the file in the following format:
     *  ModelName-Index Construction Time-TimeValue
     * @param milliseconds 
     */
    public static void writeIndexTime(long milliseconds) {
        System.out.println("Writing Index Construction Time to the metrics file");
        File file = new File(Constants.metricsFileName);
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(Constants.modelName + "-Index Construction Time-" + milliseconds);
        } catch(IOException e) {
            System.err.println("Error writing to metrics file");
        }
    }
    
    /**
     * Write the number of entries there are in the inverted index file.
     * 
     * Appends to the file, but it should not ever change.
     * 
     * Writes to the file in the following format:
     *  ModelName-Entries in Index-Num
     * @param num 
     */
    public static void writeNumberEntries(long num) {
        System.out.println("Writing Number of Entries in the Inverted Index to the metrics file");
        File file = new File(Constants.metricsFileName);
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(Constants.modelName + "-Entries in File-" + num);
        } catch(IOException e) {
            System.err.println("Error writing to metrics file");
        }
    }
    
    /**
     * Write the amount of time the search operation took just on the cloud side.
     * 
     * Appends to the file.
     * Writes to the file in the following format:
     *  ModelName-Cloud Search Time-Query-Milliseconds
     * @param ms
     * @param query 
     */
    public static void writeCloudSearchTime(long ms, String query) {
        System.out.println("Writing amount of time to search on the cloud to the metrics file");
        File file = new File(Constants.metricsFileName);
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(Constants.modelName + "-Cloud Search Time-" + query + "-" +  ms);
        } catch(IOException e) {
            System.err.println("Error writing to metrics file");
        }
    }
}
